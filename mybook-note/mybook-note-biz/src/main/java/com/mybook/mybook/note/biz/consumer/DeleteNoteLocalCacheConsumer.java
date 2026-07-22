package com.mybook.mybook.note.biz.consumer;

import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.mybook.mybook.note.biz.constant.MQConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RocketMQMessageListener(consumerGroup = "mybook_group",// Group
    topic = MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, // 消费的主题 Topic
    messageModel = MessageModel.BROADCASTING // 广播模式
)
public class DeleteNoteLocalCacheConsumer implements RocketMQListener<String>{

    @Override
    public void onMessage(String body) {
        Long noteId = Long.valueOf(body);
        log.info("## 消费者消费成功, noteId: {}", noteId);
    }

    
    
}
