package com.mybook.mybook.user.relation.biz.consumer;

import com.google.common.util.concurrent.RateLimiter;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.util.DateUtils;
import com.mybook.framework.common.util.JsonUtils;
import com.mybook.mybook.user.relation.biz.constant.MQConstants;
import com.mybook.mybook.user.relation.biz.constant.RedisKeyConstants;
import com.mybook.mybook.user.relation.biz.domain.dataobject.FansDO;
import com.mybook.mybook.user.relation.biz.domain.dataobject.FollowingDO;
import com.mybook.mybook.user.relation.biz.domain.mapper.FansDOMapper;
import com.mybook.mybook.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.mybook.mybook.user.relation.biz.model.dto.FollowUserMqDTO;
import com.mybook.mybook.user.relation.biz.model.dto.UnfollowUserMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mybook_group",
        topic = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW
)
public class FollowUnfollowConsumer implements RocketMQListener<Message> {
    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private FollowingDOMapper followingDOMapper;

    @Resource
    private FansDOMapper fansDOMapper;

    @Resource
    private RateLimiter rateLimiter;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message) {
        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();

        String bodyJsonStr = new String(message.getBody());
        String tags = message.getTags();
        log.info("==> FollowUnfollowConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);

        if (Objects.equals(tags, MQConstants.TAG_FOLLOW)){ // 关注
            handleFollowTagMessage(bodyJsonStr);
        } else if (Objects.equals(tags, MQConstants.TAG_UNFOLLOW)) { // 取关
            handleUnfollowTagMessage(bodyJsonStr);
        }
    }

    /**
     * 取关
     * @param bodyJsonStr
     */
    private void handleUnfollowTagMessage(String bodyJsonStr) {
        // 将消息体 Json 字符串转为 DTO 对象
        UnfollowUserMqDTO unfollowUserMqDTO = JsonUtils.parseObject(bodyJsonStr, UnfollowUserMqDTO.class);
        // 判空
        if (Objects.isNull(unfollowUserMqDTO)){
            log.warn("=== unfollowUserMqDTO: {}", bodyJsonStr);
            return;
        }
        Long userId = unfollowUserMqDTO.getUserId();
        Long unfollowUserId = unfollowUserMqDTO.getUnfollowUserId();
        LocalDateTime createTime = unfollowUserMqDTO.getCreateTime();
        Boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try{
                int count = followingDOMapper.deleteByUserIdAndFollowingId(userId, unfollowUserId);
                if (count > 0){
                    fansDOMapper.deleteByUserIdAndFansUserId(unfollowUserId,userId);
                }
                return true;
            } catch (Exception e){
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("", e);
            }
            return false;
        }));
        // 若数据库删除成功，更新 Redis，将自己从被取关用户的 ZSet 粉丝列表删除
        if (isSuccess){
            String fansRedisKey = RedisKeyConstants.buildUserFansKey(unfollowUserId);
            redisTemplate.opsForZSet().remove(fansRedisKey, userId);
        }


    }

    /**
     * 关注
     * @param bodyJsonStr
     */
    private void handleFollowTagMessage(String bodyJsonStr) {
        // 将消息体 Json 字符串转为 DTO 对象
        FollowUserMqDTO followUserMqDTO = JsonUtils.parseObject(bodyJsonStr, FollowUserMqDTO.class);
        // 判空
        if (Objects.isNull(followUserMqDTO)){
            log.warn("=== followUserMqDTO: {}", bodyJsonStr);
            return;
        }
        // 幂等性：通过联合唯一索引保证
        Long userId = followUserMqDTO.getUserId();
        Long followId = followUserMqDTO.getFollowerId();
        LocalDateTime createTime = followUserMqDTO.getCreateTime();

        // 编程式提交事务
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try{
                // 关注成功需往数据库添加两条记录
                // 关注表：一条记录
                int count = followingDOMapper.insert(FollowingDO.builder()
                        .userId(userId)
                        .followingUserId(followId)
                        .createTime(createTime)
                        .build());
                // 粉丝表：一条记录
                if (count > 0){
                    fansDOMapper.insert(FansDO.builder()
                            .userId(followId)
                            .fansUserId(userId)
                            .createTime(createTime)
                            .build());
                }
                return true;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("", ex);
            }
            return false;
        }));

        log.info("## 数据库添加记录结果：{}", isSuccess);

        if (isSuccess) {
            // 更新 Redis 中被关注用户的 ZSet 粉丝列表
            // 执行脚本添加粉丝列表，若粉丝列表不存在则跳过，否则，若超5000则删除，然后再添加
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_update_fans_zset.lua")));
            script.setResultType(Long.class);

            String fansRedisKey = RedisKeyConstants.buildUserFansKey(followId);
            redisTemplate.execute(script, Collections.singletonList(fansRedisKey), userId, DateUtils.localDateTime2Timestamp(createTime));
        }
    }
}
