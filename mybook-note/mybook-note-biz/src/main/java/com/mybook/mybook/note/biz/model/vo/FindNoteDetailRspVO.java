package com.mybook.mybook.note.biz.model.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FindNoteDetailRspVO {

    private Long id;

    private Integer type;

    private Long creatorId;
    
    private String creatorName;

    private String avatar;

    private List<String> imgUris;

    private String title;

    private String content;

    private Long topicId;

    private String topicName;

    private String videoUri;

    private LocalDateTime updateTime;

    private Integer visible;
}
