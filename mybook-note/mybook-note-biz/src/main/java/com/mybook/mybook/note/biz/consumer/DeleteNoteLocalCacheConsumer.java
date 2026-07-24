package com.mybook.mybook.note.biz.consumer;

import com.mybook.mybook.note.biz.service.NoteService;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import com.mybook.mybook.note.biz.constant.MQConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RocketMQMessageListener(consumerGroup = "mybook_group",topic = MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE,messageModel = MessageModel.BROADCASTING)
public class DeleteNoteLocalCacheConsumer implements RocketMQListener<String>{
    @Resource
    NoteService noteService;

    @Override
    public void onMessage(String body) {
        Long noteId = Long.valueOf(body);
        log.info("## 删除本地缓存, noteId: {}", noteId);
        noteService.deleteNoteLocalCache(noteId);
    }
}