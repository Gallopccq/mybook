package com.mybook.mybook.user.relation.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FollowUserMqDTO {

    private Long userId;

    private Long followerId;

    private LocalDateTime createTime;
}
