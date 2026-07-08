package com.mybook.mybook.user.biz.domain.dataobject;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDO {
    private Long id;

    private String mybookId;

    private String phone;

    private String password;

    private LocalDateTime birthday;

    private String nickname;

    private String avatar;

    private String backgroundImg;

    private Byte sex;

    private Byte status;

    private String introduction;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean isDeleted;

}