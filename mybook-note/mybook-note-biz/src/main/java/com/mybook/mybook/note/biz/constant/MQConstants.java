package com.mybook.mybook.note.biz.constant;

public interface MQConstants {
    String TOPIC_DELETE_NOTE_LOCAL_CACHE = "DeleteNoteLocalCacheTopic";
    /**
     * Topic 主题：延迟双删 Redis 笔记缓存
     */
    String TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE = "DelayDeleteNoteRedisCacheTopic";
}
