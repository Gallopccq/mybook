package com.mybook.mybook.note.biz.constant;

import java.time.LocalDateTime;

public class LocalCacheConstants {
    private static final String NOTE_UPDATE_TIME_PREFIX = "note:updateTime:";

    public static String buildNoteUpdateTimeKey(Long noteId){
        return NOTE_UPDATE_TIME_PREFIX + noteId;
    }
}
