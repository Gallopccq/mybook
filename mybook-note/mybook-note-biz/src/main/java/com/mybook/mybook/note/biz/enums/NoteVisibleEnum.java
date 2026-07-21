package com.mybook.mybook.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NoteVisibleEnum {
    PUBLIC(0), // 公开，所有人可见
    PRIVATE(1); // 仅自己可见
    ;


    private final Integer code;
}
