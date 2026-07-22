package com.mybook.mybook.note.biz.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.mybook.framework.biz.context.holder.LoginUserContextHolder;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.framework.common.util.JsonUtils;
import com.mybook.mybook.note.biz.constant.MQConstants;
import com.mybook.mybook.note.biz.constant.RedisKeyConstants;
import com.mybook.mybook.note.biz.domain.dataobject.NoteDO;
import com.mybook.mybook.note.biz.domain.mapper.NoteDOMapper;
import com.mybook.mybook.note.biz.domain.mapper.TopicDOMapper;
import com.mybook.mybook.note.biz.enums.NoteStatusEnum;
import com.mybook.mybook.note.biz.enums.NoteTypeEnum;
import com.mybook.mybook.note.biz.enums.NoteVisibleEnum;
import com.mybook.mybook.note.biz.enums.ResponseCodeEnum;
import com.mybook.mybook.note.biz.model.vo.FindNoteDetailReqVO;
import com.mybook.mybook.note.biz.model.vo.FindNoteDetailRspVO;
import com.mybook.mybook.note.biz.model.vo.PublishNoteReqVO;
import com.mybook.mybook.note.biz.model.vo.UpdateNoteReqVO;
import com.mybook.mybook.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.mybook.mybook.note.biz.rpc.KeyValueRpcService;
import com.mybook.mybook.note.biz.rpc.UserRpcService;
import com.mybook.mybook.note.biz.service.NoteService;
import com.mybook.mybook.user.dto.resp.FindUserByIdRspDTO;

import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class NoteServiceImpl implements NoteService {
    @Resource(name = "taskExecutor")
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    
    @Resource
    DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;

    @Resource
    KeyValueRpcService keyValueRpcService;

    @Resource
    UserRpcService userRpcService;

    @Resource
    NoteDOMapper noteDOMapper;

    @Resource
    TopicDOMapper topicDOMapper;

    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Resource
    RocketMQTemplate rocketMQTemplate;

    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(1, TimeUnit.HOURS)// 设置缓存条目在写入后 1 小时过期
            .build();


    /**
     * 入参: 笔记id
     * 出参：笔记详情（用户信息，笔记信息）
     */
    @SneakyThrows
    @Override
    public Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO){
        // 笔记 ID
        Long noteId = findNoteDetailReqVO.getId();

        // 当前登录用户
        Long userId = LoginUserContextHolder.getUserId();

        // 从本地缓存中获取
        String findNoteDetailRspVOStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        if (StringUtils.isNotBlank(findNoteDetailRspVOStrLocalCache)){
            FindNoteDetailRspVO findNoteDetailRspVO = JsonUtils.parseObject(findNoteDetailRspVOStrLocalCache, FindNoteDetailRspVO.class);
            log.info("==> 命中了本地缓存；{}", findNoteDetailRspVOStrLocalCache);
            // 可见性校验
            if (Objects.nonNull(findNoteDetailRspVO)) {
                Integer visible = findNoteDetailRspVO.getVisible();
                checkNoteVisible(visible, userId, findNoteDetailRspVO.getCreatorId());
            }
            return Response.success(findNoteDetailRspVO);

        }

        // 从 redis 缓存中获取
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        String noteDetailJson = redisTemplate.opsForValue().get(noteDetailRedisKey);

        // 若缓存中有该笔记的数据，则直接返回
        if (StringUtils.isNotBlank(noteDetailJson)){
            FindNoteDetailRspVO findNoteDetailRspVO = JsonUtils.parseObject(noteDetailJson, FindNoteDetailRspVO.class);
            threadPoolTaskExecutor.submit(() -> {
                LOCAL_CACHE.put(noteId, Objects.isNull(findNoteDetailRspVO) ? "null" : JsonUtils.toJsonString(findNoteDetailRspVO));
            });
            // 可见性检验
            if (Objects.nonNull(findNoteDetailRspVO)){
                Integer visible = findNoteDetailRspVO.getVisible();
                checkNoteVisible(visible, userId, findNoteDetailRspVO.getCreatorId());
            }
            return Response.success(findNoteDetailRspVO);
        }
        
        // 查询笔记
        NoteDO noteDO = noteDOMapper.selectByPrimaryKey(noteId);
        // 若笔记不存在，则抛出异常
        if (Objects.isNull(noteDO)){
            threadPoolTaskExecutor.execute(()->{
                // 防止缓存穿透，将空数据存入 Redis 缓存 (过期时间不宜设置过长)
                // 保底1分钟 + 随机秒数
                long expireSeconds = 60 + RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(noteDetailRedisKey, "null", expireSeconds, TimeUnit.SECONDS);
            });
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 可见性检验
        Integer visible = Integer.valueOf(noteDO.getVisible());
        checkNoteVisible(visible, userId, noteDO.getCreatorId());

        // 并发查询优化
        // RPC: 调用用户服务
        Long creatorId = noteDO.getCreatorId();
        CompletableFuture<FindUserByIdRspDTO> userResultFuture = CompletableFuture
                .supplyAsync(() -> userRpcService.findById(creatorId), threadPoolTaskExecutor);

        // RPC: 调用 K-V 存储服务获取内容
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        String contentUuid = noteDO.getContentUuid();
        if (Objects.equals(noteDO.getIsContentEmpty(), Boolean.FALSE)){
            contentResultFuture = CompletableFuture
                    .supplyAsync(() -> keyValueRpcService.findNoteContent(contentUuid), threadPoolTaskExecutor);
        }

        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<FindNoteDetailRspVO> resultFuture = CompletableFuture
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s -> {
                    FindUserByIdRspDTO findUserByIdRspDTO = userResultFuture.join();
                    String content = finalContentResultFuture.join();

                    // 笔记类型
                    Integer noteType = Integer.valueOf(noteDO.getType());
                    // 图文笔记图片链接（字符串）
                    String imgUrisStr = noteDO.getImgUris();
                    // 图文笔记图片链接（集合）
                    List<String> imgUris = null;
                    // 如果查询的是图文笔记，需要将图片链接的逗号分隔开，转换成集合
                    if (Objects.equals(noteType, NoteTypeEnum.IMAGE_TEXT.getCode()) && StringUtils.isNoneBlank(imgUrisStr)){
                        imgUris = List.of(imgUrisStr.split( ","));
                    }

                    // avatar
                    String avatar = null;
                    // nickName
                    String nickName = null;
                    // 若用户信息非空，则填写avatar和nickName信息
                    if (Objects.nonNull(findUserByIdRspDTO)){
                        avatar = findUserByIdRspDTO.getAvatar();
                        nickName = findUserByIdRspDTO.getNickName();
                    }

                    // 构造返参
                    return FindNoteDetailRspVO.builder()
                            .avatar(avatar)
                            .creatorId(noteDO.getCreatorId())
                            .creatorName(nickName)
                            .type(Integer.valueOf(noteDO.getType()))
                            .visible(Integer.valueOf(noteDO.getVisible()))
                            .imgUris(imgUris)
                            .videoUri(noteDO.getVideoUris())
                            .content(content)
                            .id(noteId)
                            .title(noteDO.getTitle())
                            .topicId(noteDO.getTopicId())
                            .topicName(noteDO.getTopicName())
                            .updateTime(noteDO.getUpdateTime())
                            .build();
                });

        // 获取笔记信息
        FindNoteDetailRspVO findNoteDetailRspVO = resultFuture.get();

        // 异步线程中将笔记详情存入 Redis
        threadPoolTaskExecutor.submit(() -> {
            String noteDetailJson1 = JsonUtils.toJsonString(findNoteDetailRspVO);
            // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            redisTemplate.opsForValue().set(noteDetailRedisKey, noteDetailJson1, expireSeconds, TimeUnit.SECONDS);
        });

        return Response.success(findNoteDetailRspVO);   
    }

    /**
     * 入参: 笔记id
     * 出参：笔记详情（用户信息，笔记信息）
     */
    @SneakyThrows
    @Override
    public Response<FindNoteDetailRspVO> findNoteDetailWithSync(FindNoteDetailReqVO findNoteDetailReqVO){
        // 笔记 ID
        Long noteId = findNoteDetailReqVO.getId();

        // 当前登录用户
        Long userId = LoginUserContextHolder.getUserId();

        // 查询笔记
        NoteDO noteDO = noteDOMapper.selectByPrimaryKey(noteId);
        // 若笔记不存在，则抛出异常
        if (Objects.isNull(noteDO)){
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 可见性检验
        Integer visible = Integer.valueOf(noteDO.getVisible());
        if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                && !Objects.equals(userId, noteDO.getCreatorId())){ // 仅自己可见, 并且访问用户为笔记创建者才能访问，非本人则抛出异常
            throw new BizException(ResponseCodeEnum.NOTE_PRIVATE);
        }

        // RPC: 调用用户服务
        FindUserByIdRspDTO findUserByIdRspDTO = userRpcService.findById(userId);

        // RPC: 调用 K-V 存储服务获取内容
        String content = null;
        if (Objects.equals(noteDO.getIsContentEmpty(), Boolean.FALSE)){
            content = keyValueRpcService.findNoteContent(noteDO.getContentUuid());
        }

        // 笔记类型
        Integer noteType = Integer.valueOf(noteDO.getType());
        // 图文笔记图片链接（字符串）
        String imgUrisStr = noteDO.getImgUris();
        // 图文笔记图片链接（集合）
        List<String> imgUris = null;
        // 如果查询的是图文笔记，需要将图片链接的逗号分隔开，转换成集合
        if (Objects.equals(noteType, NoteTypeEnum.IMAGE_TEXT.getCode()) && StringUtils.isNoneBlank(imgUrisStr)){
            imgUris = List.of(imgUrisStr.split( ","));
        }

        // avatar
        String avatar = null;
        // nickName
        String nickName = null;
        // 若用户信息非空，则填写avatar和nickName信息
        if (Objects.nonNull(findUserByIdRspDTO)){
            avatar = findUserByIdRspDTO.getAvatar();
            nickName = findUserByIdRspDTO.getNickName();
        }

        // 构造返参
        FindNoteDetailRspVO findNoteDetailRspVO = FindNoteDetailRspVO.builder()
                .avatar(avatar)
                .creatorId(noteDO.getCreatorId())
                .creatorName(nickName)
                .type(Integer.valueOf(noteDO.getType()))
                .visible(Integer.valueOf(noteDO.getVisible()))
                .imgUris(imgUris)
                .videoUri(noteDO.getVideoUris())
                .content(content)
                .id(noteId)
                .title(noteDO.getTitle())
                .topicId(noteDO.getTopicId())
                .topicName(noteDO.getTopicName())
                .updateTime(noteDO.getUpdateTime())
                .build();

        return Response.success(findNoteDetailRspVO);
    }

    


    /**
     * 笔记有图文类型和视频类型，图文类型不要求笔记一定有内容。
     * 先调用 kv 服务将笔记内容存入，其中笔记 UUID 由 UUID 生成
     * 然后将所有信息存入 mysql
     * tips: 笔记 ID 由 snowflake 生成
     */
    public Response<?> publishNote(PublishNoteReqVO publishNoteReqVO){
        // 校验笔记类型
        Integer type = publishNoteReqVO.getType();
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);
        if (Objects.isNull(noteTypeEnum)){
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        // 笔记内容允许为空
        boolean isContentEmpty = true;
        String videoUri = null;
        switch (noteTypeEnum){
            case IMAGE_TEXT:
                List<String> imgUriList = publishNoteReqVO.getImgUris();
                // 是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                Preconditions.checkArgument(imgUriList.size() < 8, "笔记图片不能多于 8 张");
                imgUris = StringUtils.join(imgUriList, ",");

                break;
            case VIDEO:
                videoUri = publishNoteReqVO.getVideoUri();
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri));

                break;
            default:
                break;
        }
        
        String snowflakeId = distributedIdGeneratorRpcService.getSnowflakeId();

        String noteContent = publishNoteReqVO.getContent();
        String contentUuid = null;
        if (StringUtils.isNotBlank(noteContent)){
            isContentEmpty = false;
            contentUuid = UUID.randomUUID().toString();
            boolean isSavedSuccess = keyValueRpcService.saveNoteContent(contentUuid, noteContent);
            if (!isSavedSuccess){
                throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
            }
        }

        // 获取话题名称
        Long topicId = publishNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)){
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);
        }

        Long creatorId = LoginUserContextHolder.getUserId();
        if (Objects.isNull(creatorId)){
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        NoteDO noteDO = NoteDO.builder()
            .id(Long.valueOf(snowflakeId))
            .contentUuid(contentUuid)
            .isContentEmpty(isContentEmpty)
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .imgUris(imgUris)
            .videoUris(videoUri)
            .title(publishNoteReqVO.getTitle())
            .creatorId(creatorId)
            .topicId(topicId)
            .isTop(Boolean.FALSE)
            .status(Byte.valueOf(NoteStatusEnum.NORMAL.getCode().toString()))
            .visible(Byte.valueOf(NoteVisibleEnum.PUBLIC.getCode().toString()))
            .topicName(topicName)
            .type(Byte.valueOf(type.toString()))
            .build();
        
        try{
            noteDOMapper.insertSelective(noteDO);
        } catch (Exception e){
            log.error("==> 笔记存储失败", e);
            
            // 笔记保存失败，则删除笔记内容
            if (StringUtils.isNotBlank(contentUuid)){
                keyValueRpcService.deleteNoteContent(contentUuid);
            }
        }
        return Response.success();
    }

    /**
     * 更新笔记内容
     * 校验笔记类型，笔记图文视频链接，查询话题名称
     * 更新笔记内容 selectByPrimaryKeySelective
     * 删除redis缓存，本地缓存
     * 更新笔记内容：若新内容为空则删除内容kv存储；若无旧内容则生成新的uuid，若有旧内容则覆盖。
     * @param updateNoteReqVO
     * @return
     *
     * 存在问题：若笔记内容uuid需要更新或删除，不能更新数据库中的uuid；且若在后面直接添加数据库更新操作，总的数据库访问次数为3次，太多。
     */

    /**
     * 新的更新笔记内容逻辑：
     * 查询数据库中笔记内容
     * 若笔记为空则直接插入，否则校验是否需要更新
     * 校验笔记类型，笔记图文视频链接，查询话题名称
     * 更新笔记内容：若新内容为空则删除内容kv存储；若无旧内容则生成新的uuid，若有旧内容则覆盖。
     * 更新笔记信息 selectByPrimaryKeySelective
     * 删除 redis 缓存，本地缓存
     * @param updateNoteReqVO
     * @return
     */
    @Override
    public Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO) {
        Long noteId = updateNoteReqVO.getId();

        // 查询数据库中的笔记信息
        NoteDO noteDO1 = noteDOMapper.selectByPrimaryKey(noteId);
        boolean needUpdate = false;
        if (Objects.isNull(noteDO1)){
            needUpdate = true;
        }

        // 笔记类型
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(updateNoteReqVO.getType());
        if (Objects.isNull(noteTypeEnum)){
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }
        if (needUpdate || !Objects.equals(noteTypeEnum, NoteTypeEnum.valueOf(Integer.valueOf(noteDO1.getType())))){
            needUpdate = true;
        }

        String imgUris = null;
        String videoUri = null;
        switch (noteTypeEnum) {
            case IMAGE_TEXT: // 笔记图片链接
                List<String> imgUrisList = updateNoteReqVO.getImgUris();
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUrisList), "笔记不能为空");
                Preconditions.checkArgument(imgUrisList.size() < 8, "笔记图片不能多于 8 张");

                imgUris = StringUtils.join(imgUrisList, ",");
                if (needUpdate || !StringUtils.equals(imgUris, noteDO1.getImgUris())){
                    needUpdate = true;
                }
                break;
            case VIDEO: // 笔记视频链接
                videoUri = updateNoteReqVO.getVideoUri();
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                if (needUpdate || !Objects.equals(videoUri, noteDO1.getVideoUris())){
                    needUpdate = true;
                }
                break;
            default:
                break;
        }

        // 话题名称
        Long topicId = updateNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)){
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);
            if (StringUtils.isBlank(topicName)) throw new BizException(ResponseCodeEnum.TOPIC_NOT_FOUND);
        }
        if (needUpdate || !Objects.equals(topicId, noteDO1.getTopicId())){
            needUpdate = true;
        }

        // title
        String title = updateNoteReqVO.getTitle();
        if (needUpdate || !StringUtils.equals(title, noteDO1.getTitle())){
            needUpdate = true;
        }

        // 更新笔记内容, 不校验是否一样
        String content = updateNoteReqVO.getContent();
        String contentUuid = noteDO1.getContentUuid();
        boolean isUpdateContentSuccess = false;
        if (StringUtils.isBlank(content)){
            // 若笔记内容为空，则删除 K-V 存储
            isUpdateContentSuccess = keyValueRpcService.deleteNoteContent(contentUuid);
            contentUuid = "";
            needUpdate = true;
        } else {
            // 若将无内容的笔记，更新为了有内容的笔记，需要重新生成 UUID
            if (StringUtils.isBlank(contentUuid)){
                contentUuid = UUID.randomUUID().toString();
                needUpdate = true;
            }
            isUpdateContentSuccess = keyValueRpcService.saveNoteContent(contentUuid, content);
        }

        if (!isUpdateContentSuccess){
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }

        // 更新笔记
        log.info("==== needUpdate: {}", needUpdate);
        if (needUpdate){
            NoteDO noteDO = NoteDO.builder()
                    .id(noteId)
                    .title(updateNoteReqVO.getTitle())
                    .imgUris(imgUris)
                    .contentUuid(contentUuid)
                    .isContentEmpty(StringUtils.isBlank(content))
                    .topicId(topicId)
                    .topicName(topicName)
                    .updateTime(LocalDateTime.now())
                    .type(Byte.valueOf(noteTypeEnum.getCode().toString()))
                    .videoUris(videoUri)
                    .build();
            log.info("==== 更新笔记信息： {}", noteDO.toString());
            noteDOMapper.updateByPrimaryKeySelective(noteDO);
        }

        // 删除 redis 缓存
        String noteDetailKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        log.info("==== 删除redis缓存: {}", noteDetailKey);
        redisTemplate.delete(noteDetailKey);

        // 删除本地缓存
        // log.info("==== 删除本地缓存: {}", noteId);
        // LOCAL_CACHE.invalidate(noteId);

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");



        return Response.success();
    }

    private void checkNoteVisible(Integer visible, Long userId, Long creatorId){
        if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                && !Objects.equals(userId, creatorId)){ // 仅自己可见, 并且访问用户为笔记创建者才能访问，非本人则抛出异常
            throw new BizException(ResponseCodeEnum.NOTE_PRIVATE);
        }
    }
}

