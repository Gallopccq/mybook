package com.mybook.mybook.user.relation.biz.domain.dataobject;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FansDO {
    private Long id;

    private Long userId;

    private Long fansUserId;

    private LocalDateTime createTime;

    
}