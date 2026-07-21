package com.mybook.mybook.note.biz.model.vo;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishNoteReqVO {
    
    @NotNull(message = "笔记类型不能为空")
    private Integer type;
    
    private List<String> imgUris;

    private String videoUri;

    private String content;

    private String title;

    private Long topicId;

}
