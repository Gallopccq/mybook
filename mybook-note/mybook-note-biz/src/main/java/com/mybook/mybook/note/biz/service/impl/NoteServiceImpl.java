package com.mybook.mybook.note.biz.service.impl;

import com.google.common.base.Preconditions;
import com.mybook.framework.biz.context.holder.LoginUserContextHolder;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.note.biz.domain.dataobject.NoteDO;
import com.mybook.mybook.note.biz.domain.mapper.NoteDOMapper;
import com.mybook.mybook.note.biz.domain.mapper.TopicDOMapper;
import com.mybook.mybook.note.biz.enums.NoteStatusEnum;
import com.mybook.mybook.note.biz.enums.NoteTypeEnum;
import com.mybook.mybook.note.biz.enums.NoteVisibleEnum;
import com.mybook.mybook.note.biz.enums.ResponseCodeEnum;
import com.mybook.mybook.note.biz.model.vo.PublishNoteReqVO;
import com.mybook.mybook.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.mybook.mybook.note.biz.rpc.KeyValueRpcService;
import com.mybook.mybook.note.biz.service.NoteService;

import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import jnr.ffi.Struct.caddr_t;
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
    @Resource
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    
    @Resource
    DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;

    @Resource 
    KeyValueRpcService keyValueRpcService;

    @Resource
    NoteDOMapper noteDOMapper;

    @Resource
    TopicDOMapper topicDOMapper;

//    @Override
    @SneakyThrows
    public Response<?> findNoteDetail(){
        CompletableFuture<Object> userResultFuture = CompletableFuture
                .supplyAsync(() -> Integer.valueOf(1), threadPoolTaskExecutor);
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if (Objects.equals(Integer.valueOf(1), Integer.valueOf(1))) {
            contentResultFuture = CompletableFuture
                    .supplyAsync(() -> String.valueOf(1), threadPoolTaskExecutor);
        }
        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<Object> resultFuture = CompletableFuture
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s -> {
                   Object findUserByIdRspDTO = userResultFuture.join();
                   String content = finalContentResultFuture.join();

                   Integer noteType = Integer.valueOf(1);
                   String imgUrisStr = String.valueOf(1);
                   List<String> imgUris = null;
                   if (Objects.equals(noteType, 0)
                        && !imgUrisStr.isBlank()){
                       imgUris = List.of(imgUrisStr.split(","));
                   }
                   return Integer.valueOf(1);

                });
        Object findNoteDetailRspVO = resultFuture.get();

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
