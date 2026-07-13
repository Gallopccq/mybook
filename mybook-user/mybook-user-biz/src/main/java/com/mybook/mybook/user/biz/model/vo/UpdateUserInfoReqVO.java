package com.mybook.mybook.user.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserInfoReqVO {
    private MultipartFile avatar;
    private String nickName;
    private String mybookId;
    private Integer sex;
    private LocalDate birthday;
    private String introduction;
    private MultipartFile background;
}
