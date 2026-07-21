package com.mybook.mybook.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FindUserByIdRspDTO {
    
    private Long id;
    private String nickName;
    private String avatar;
}
