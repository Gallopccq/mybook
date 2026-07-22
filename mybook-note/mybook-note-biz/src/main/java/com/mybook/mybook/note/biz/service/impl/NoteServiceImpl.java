package com.mybook.mybook.note.biz.service.impl;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.google.common.base.Preconditions;
import com.mybook.framework.biz.context.holder.LoginUserContextHolder;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.kv.dto.rsp.FindNoteContentRspDTO;
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
import com.mybook.mybook.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.mybook.mybook.note.biz.rpc.KeyValueRpcService;
import com.mybook.mybook.note.biz.rpc.UserRpcService;
import com.mybook.mybook.note.biz.service.NoteService;
import com.mybook.mybook.user.dto.req.FindUserByIdReqDTO;
import com.mybook.mybook.user.dto.resp.FindUserByIdRspDTO;

import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


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

        FindNoteDetailRspVO findNoteDetailRspVO = resultFuture.get();


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
        Boolean isContentEmpty = true;
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
            boolean isSavedSuccess = keyValueRpcService.addNoteContent(contentUuid, noteContent);
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
}
