package com.mybook.mybook.user.relation.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.mybook.framework.biz.context.holder.LoginUserContextHolder;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.PageResponse;
import com.mybook.framework.common.response.Response;
import com.mybook.framework.common.util.DateUtils;
import com.mybook.framework.common.util.JsonUtils;
import com.mybook.mybook.user.dto.resp.FindUserByIdRspDTO;
import com.mybook.mybook.user.relation.biz.constant.MQConstants;
import com.mybook.mybook.user.relation.biz.constant.RedisKeyConstants;
import com.mybook.mybook.user.relation.biz.domain.dataobject.FollowingDO;
import com.mybook.mybook.user.relation.biz.domain.mapper.FansDOMapper;
import com.mybook.mybook.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.mybook.mybook.user.relation.biz.enums.LuaResultEnum;
import com.mybook.mybook.user.relation.biz.enums.ResponseCodeEnum;
import com.mybook.mybook.user.relation.biz.model.dto.FollowUserMqDTO;
import com.mybook.mybook.user.relation.biz.model.dto.UnfollowUserMqDTO;
import com.mybook.mybook.user.relation.biz.model.vo.*;
import com.mybook.mybook.user.relation.biz.rpc.UserRpcService;
import com.mybook.mybook.user.relation.biz.service.UserRelationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class UserRelationServiceImpl implements UserRelationService{

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserRpcService userRpcService;

    @Resource
    private FollowingDOMapper followingDOMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private FansDOMapper fansDOMapper;

    /**
     * 关注用户接口
     * 校验关注按的用户是否是自己，是否存在，关注的用户总数是否超过上限（1000）
     * 先写入 redis 然后通过 RocketMQ 消费者写入数据库
     * 写入 redis ： 通过 zset 记录关注用户 ID 和关注时间辍（方便排序）
     * 写入数据库：在 t_following 和 t_fans 中添加数据
     */
    @Override
    public Response<?> follow(FollowUserReqVO followUserReqVO) {
        Long userId = LoginUserContextHolder.getUserId();
        Long followerId = followUserReqVO.getId();

        // 检查用户是否登陆
        if (Objects.isNull(userId)){
            throw new BizException(ResponseCodeEnum.USER_NOT_LOGIN);
        }

        // 检查关注用户是否是自己
        if (Objects.equals(userId, followerId)){
            throw new BizException(ResponseCodeEnum.CANT_FOLLOW_YOUR_SELF);
        }

        // 检查用户是否存在
        FindUserByIdRspDTO findUserByIdRspDTO = userRpcService.findById(followerId);
        if (Objects.isNull(findUserByIdRspDTO)){
            throw new BizException(ResponseCodeEnum.FOLLOW_UNFOLLOW_USER_NOT_EXISTED);
        }

        // 检查关注用户总数是否超过上限
        String follwingKey = RedisKeyConstants.buildFollowingKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_add.lua")));
        script.setResultType(Long.class);
        LocalDateTime now = LocalDateTime.now();
        long timestamp = DateUtils.localDateTime2Timestamp(now);
        // 在 Redis 高并发场景下，通过 singletonList 避免创建多余数组
        Long result = redisTemplate.execute(script, Collections.singletonList(follwingKey), followerId, timestamp);

        checkLuaScriptResult(result);

        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) {
            List<FollowingDO> followingDOS = followingDOMapper.selectByUserId(userId);

            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

            // 若记录为空，直接 ZADD 关系数据, 并设置过期时间
            if (CollUtil.isEmpty(followingDOS)) {
                DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
                script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/follow_add_and_expire.lua")));
                script2.setResultType(Long.class);

                // TODO: 可以根据用户类型，设置不同的过期时间，若当前用户为大V, 则可以过期时间设置的长些或者不设置过期时间；如不是，则设置的短些
                // 如何判断呢？可以从计数服务获取用户的粉丝数，目前计数服务还没创建，则暂时采用统一的过期策略
                redisTemplate.execute(script2, Collections.singletonList(follwingKey), followerId, timestamp, expireSeconds);
            } else { // 若记录不为空，则将关注关系数据全量同步到 Redis 中，并设置过期时间；
                Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);

                // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
                DefaultRedisScript<Long> script3 = new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);

                redisTemplate.execute(script3, Collections.singletonList(follwingKey), luaArgs);
                // 再次调用上面的 Lua 脚本：follow_check_and_add.lua , 将最新的关注关系添加进去
                result = redisTemplate.execute(script, Collections.singletonList(follwingKey), followerId, timestamp);
                checkLuaScriptResult(result);
            }
        }


        // 发送 MQ
        // 构建消息体 DTO
        FollowUserMqDTO followUserMqDTO = FollowUserMqDTO.builder()
                .followerId(followerId)
                .userId(userId)
                .createTime(now)
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(followUserMqDTO)).build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" + MQConstants.TAG_FOLLOW;

        log.info("==> 开始发送关注操作 MQ, 消息体: {}", followUserMqDTO);

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.error("==> MQ 发送异常: ", e);
            }
        });


        return Response.success();
    }

    private Object[] buildLuaArgs(List<FollowingDO> followingDOS, long expireSeconds) {
        int argsLength = followingDOS.size() * 2 + 1; // 每个关注关系有 2 个参数（score 和 value），再加一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i=0;
        for (FollowingDO followingDO : followingDOS){
            luaArgs[i] = DateUtils.localDateTime2Timestamp(followingDO.getCreateTime()); // 关注时间作为 score
            luaArgs[i+1] = followingDO.getFollowingUserId(); // 关注的用户 ID 作为 ZSet value
            i+=2;
        }
        luaArgs[argsLength-1] = expireSeconds;
        return luaArgs;
    }

    private void checkLuaScriptResult(Long result) {
        LuaResultEnum luaResultEnum = LuaResultEnum.valueOf(result);
        if (Objects.isNull(luaResultEnum)){
            throw new RuntimeException("Lua 返回结果错误");
        }
        switch (luaResultEnum){
            case FOLLOW_LIMIT -> throw new BizException(ResponseCodeEnum.FOLLOWING_COUNT_LIMIT);
            case ALREADY_FOLLOWED -> throw new BizException(ResponseCodeEnum.ALREADY_FOLLOWED);
        }
    }

    /**
     * 取关用户
     * 校验用户是否是自己， 取关用户是否存在，校验取关用户是否已关注
     * 更新 redis 缓存中的关注列表，粉丝列表
     * 发送异步消息来更新数据库中的关注列表，粉丝列表
     * 其中：
     * 对于粉丝列表：redis的粉丝列表若未加载，则跳过；若存在，则删除对应的键。
     * 若 redis 的关注列表未加载，去查数据库，校验取关用户是否已关注
     * 性能角度：
     * 若用户频繁取关，则说明最近刚关注，数据在redis中，不涉及数据库查询
     * 若用户偶尔取关，则说明关注时间较长，数据在数据库中，需要查询数据库
     * @param unfollowUserReqVO
     * @return
     */
    @Override
    public Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO) {
        // 取关用户 ID
        Long unfollowUserId = unfollowUserReqVO.getId();
        // 当前登陆用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();
        if (Objects.isNull(currUserId)){
            throw new BizException(ResponseCodeEnum.USER_NOT_LOGIN);
        }

        // 校验用户是否是自己
        if (Objects.equals(currUserId, unfollowUserId)){
            throw new BizException(ResponseCodeEnum.CANT_UNFOLLOW_YOUR_SELF);
        }

        // 校验取关用户是否存在，查数据库
        FindUserByIdRspDTO findUserByIdRspDTO = userRpcService.findById(unfollowUserId);
        if (Objects.isNull(findUserByIdRspDTO)){
            throw new BizException(ResponseCodeEnum.FOLLOW_UNFOLLOW_USER_NOT_EXISTED);
        }

        // 校验取关用户是否已关注，先查 redis，没有再查数据库
        String followUserKey = RedisKeyConstants.buildFollowingKey(currUserId);
        Boolean exists = redisTemplate.hasKey(followUserKey);
        if (exists) { // 若 redis 中有关注列表的数据
            Double score = redisTemplate.opsForZSet().score(followUserKey, unfollowUserId);
            if (Objects.isNull(score)) {
                throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
            }
        } else {  // 查数据库中的关注列表
            int count = followingDOMapper.selectByUserIdAndFollowingId(currUserId, unfollowUserId);
            if (count == 0){  // 未关注该用户
                throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
            }
        }

        // 更新 redis 中的关注列表
        redisTemplate.opsForZSet().remove(followUserKey, unfollowUserId);

        // 发送MQ
        UnfollowUserMqDTO unfollowUserMqDTO = UnfollowUserMqDTO.builder()
                .unfollowUserId(unfollowUserId)
                .userId(currUserId)
                .createTime(LocalDateTime.now())
                .build();

        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(unfollowUserMqDTO)).build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" + MQConstants.TAG_UNFOLLOW;
        log.info("==> 开始发送取关操作 MQ, 消息体: {}", unfollowUserMqDTO);

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.error("==> MQ 发送异常: ", e);
            }
        });

        return Response.success();
    }


    /**
     * 逻辑：先查询到关注用户 id 集合，然后通过 userRpcService 查询到用户信息，最后整合为结果。
     * @param findFollowingListReqVO
     * @return
     */
    @Override
    public PageResponse<FindFollowingListRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO) {
        // 想要查询的用户 ID
        Long userId = findFollowingListReqVO.getId();
        // 页码
        Integer pageNo = findFollowingListReqVO.getPageNo();

        // 先从 Redis 中查询目标用户关注列表 ZSet 的总大小
        String followingListRedisKey = RedisKeyConstants.buildFollowingKey(userId);
        long total = redisTemplate.opsForZSet().zCard(followingListRedisKey);

        // 返参
        List<FindFollowingListRspVO> findFollowingListRspVOS = null;

        long limit = 10L;

        if (total > 0) { // 缓存中有数据

            long totalPage = PageResponse.getTotalPage(total, limit);

            // 请求的页码超出了总页数
            if (pageNo > totalPage) return PageResponse.success(null, pageNo, total);

            // 准备从 Redis 中查询 ZSet 分页数据
            // 每页 10 个元素，计算偏移量
            long offset = (pageNo - 1) * limit;

            // 使用 ZREVRANGEBYSCORE 命令按 score 降序获取元素，同时使用 LIMIT 子句实现分页
            // 注意：这里使用了 Double.POSITIVE_INFINITY 和 Double.NEGATIVE_INFINITY 作为分数范围
            // 因为关注列表最多有 1000 个元素，这样可以确保获取到所有的元素
            // 这里默认只要 redis 中有数据，则为全量数据
            Set<Object> followingUserIdsList = redisTemplate.opsForZSet()
                    .reverseRangeByScore(followingListRedisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, offset, limit);
            if (CollUtil.isNotEmpty(followingUserIdsList)){
                // 提取所有用户 ID 到集合中
                List<Long> userIds = followingUserIdsList.stream().map(obj -> Long.valueOf(obj.toString())).toList();

                // RPC: 批量查询用户信息
                List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);
                if (CollUtil.isNotEmpty(findUserByIdRspDTOS)){
                    findFollowingListRspVOS = findUserByIdRspDTOS.stream()
                            .map(dto -> FindFollowingListRspVO.builder()
                                    .userId(dto.getId())
                                    .nickName(dto.getNickName())
                                    .avatar(dto.getAvatar())
                                    .introduction(dto.getIntroduction())
                                    .build())
                            .toList();
                }
            }
        } else {
            // 若 Redis 中没有数据，则从数据库查询
            // 先查询记录总量
            long count = followingDOMapper.selectCountByUserId(userId);
            // 计算一共多少页
            long totalPage = PageResponse.getTotalPage(count, limit);

            if (pageNo > totalPage) return PageResponse.success(null, pageNo, count);

            // 偏移量
            long offset = PageResponse.getOffset(pageNo, limit);

            // 分页查询
            List<FollowingDO> followingDOS = followingDOMapper.selectPageListByUserId(userId, offset, limit);
            // 赋值真实的记录总数
            total = count;

            // 若记录不为空
            if (CollUtil.isNotEmpty(followingDOS)){
                List<Long> userIds = followingDOS.stream().map(FollowingDO::getFollowingUserId).toList();

                // RPC: 调用用户服务，并将 DTO 转换为 VO
                List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);
                findFollowingListRspVOS = followingListRspDTO2VO(findUserByIdRspDTOS);

                // TODO: 异步将关注列表全量同步到 Redis
                threadPoolTaskExecutor.submit(() -> syncFollowingList2Redis(userId));
            }
        }


        return PageResponse.success(findFollowingListRspVOS, pageNo, total);
    }

    private void syncFollowingList2Redis(Long userId) {
        // TODO
        List<FollowingDO> followingDOS = followingDOMapper.selectByUserIdWithLimit(userId);
        if (CollUtil.isNotEmpty(followingDOS)){
            String followingListRedisKey = RedisKeyConstants.buildFollowingKey(userId);
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(followingListRedisKey), luaArgs);
        }
    }

    private List<FindFollowingListRspVO> followingListRspDTO2VO(List<FindUserByIdRspDTO> findUserByIdRspDTOS){
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            return findUserByIdRspDTOS.stream()
                    .map(dto ->
                            FindFollowingListRspVO.builder()
                                    .userId(dto.getId())
                                    .nickName(dto.getNickName())
                                    .avatar(dto.getAvatar())
                                    .introduction(dto.getIntroduction())
                                    .build())
                    .toList();
        }
        return null;
    }


    /**
     * 粉丝列表接口 (分页查询)：需要展示 头像 昵称 粉丝数 笔记数 是否已关注
     * 头像：avatar（用户信息接口FindByIds），昵称：nickName
     * 粉丝数：followerCount（redis 用 pipeline + zCard， mysql用selectCountByUserIds）
     * 笔记数
     *
     *
     * @param findFansListReqVO
     * @return
     */
    @Override
    public PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO) {
        // 用户 ID 与 pageNo
        Long userId = findFansListReqVO.getId();
        Integer pageNo = findFansListReqVO.getPageNo();

        // 粉丝总数 total : 查 redis ，无则查数据库
        // 查redis
        String fansRedisKey = RedisKeyConstants.buildUserFansKey(userId);
        Long total = redisTemplate.opsForZSet().zCard(fansRedisKey);

        // 设置每页大小
        long limit = 10L;

        // 返参
        List<FindFansUserRspVO> findFansUserRspVOS = null;

        // 当前分页所有粉丝 ID
        List<Long> fansIds = Lists.newArrayList();

        if (Objects.nonNull(total) && total > 0) { // reids 中有数据
            // 计算 总页数
            long pageTotal = PageResponse.getTotalPage(total, limit);
            // 页码检查
            if (pageNo > pageTotal) return PageResponse.success(null, total, limit);
            // 获取当前分页所有粉丝的 ID
            Set<Object> fansIdsSet = redisTemplate.opsForZSet().reverseRangeByScore(fansRedisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, pageNo, limit);
            if (CollUtil.isNotEmpty(fansIdsSet)) {
                fansIds = fansIdsSet.stream().map(obj -> Long.valueOf(obj.toString())).toList();

            }

        } else { // 查数据库
            // 计算总页数

            // 页码检查

            // 获取当前分页所有粉丝的 ID

        }



        // 先查 redis ，若没有则查数据库


        // 查询所有用户信息，通过userRpcService，信息需要：用户昵称，用户头像


        // 通过 FansDOMapper 的 selectCountByUserId 得到用户的粉丝数

        // TODO： 获取用户的笔记总数

        // TODO： 获取用户的是否已关注信息

        return PageResponse.success(findFansUserRspVOS, pageNo, total);
    }
}
