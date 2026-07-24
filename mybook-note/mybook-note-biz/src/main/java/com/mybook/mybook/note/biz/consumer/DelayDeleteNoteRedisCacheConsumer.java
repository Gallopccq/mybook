package com.mybook.mybook.note.biz.consumer;

import com.mybook.mybook.note.biz.constant.MQConstants;
import com.mybook.mybook.note.biz.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE,
consumerGroup = "mybook_group_" + MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE)
public class DelayDeleteNoteRedisCacheConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void onMessage(String message) {
        Long noteId = Long.valueOf(message);
        log.info("## 延迟消息消费成功, noteId: {}", noteId);

        // 删除 Redis 笔记缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);
    }
}
