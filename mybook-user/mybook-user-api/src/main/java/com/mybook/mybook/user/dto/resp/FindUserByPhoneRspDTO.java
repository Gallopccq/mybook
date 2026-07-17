package com.mybook.mybook.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class FindUserByPhoneRspDTO {
    private Long id;

    private String password;
}
