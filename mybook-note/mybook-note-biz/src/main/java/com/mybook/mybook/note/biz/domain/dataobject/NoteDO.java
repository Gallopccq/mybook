package com.mybook.mybook.note.biz.domain.dataobject;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NoteDO {
    private Long id;

    private String title;

    private Boolean isContentEmpty;

    private Long creatorId;

    private Long topicId;

    private Boolean isTop;

    private Byte type;

    private String imgUris;

    private String videoUris;

    private Byte visible;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Byte status;

    private String contentUuid;
    
    private String topicName;
}