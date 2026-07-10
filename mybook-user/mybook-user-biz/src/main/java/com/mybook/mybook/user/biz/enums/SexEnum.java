package com.mybook.mybook.user.biz.enums;

import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public enum SexEnum {
    WOMAN(0),
    MAN(1),
    ;

    private final Integer value;

    public static boolean isValid(Integer value){
        for (SexEnum sexEnum : SexEnum.values()){
            if (Objects.equals(sexEnum, value)){
                return true;
            }
        }
        return false;
    }
}
