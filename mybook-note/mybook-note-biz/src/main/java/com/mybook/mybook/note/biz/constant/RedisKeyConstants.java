package com.mybook.mybook.note.biz.constant;

public class RedisKeyConstants {
    private static final String NOTE_DETAIL_KEY = "note:detail:";


    /**
     * 构建验证码 KEY
     *
     * @param noteId
     * @return
     */
    public static String buildNoteDetailKey(Long noteId) {
        return NOTE_DETAIL_KEY + noteId;
    }
}