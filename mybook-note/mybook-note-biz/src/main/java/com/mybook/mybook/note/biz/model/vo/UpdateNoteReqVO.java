package com.mybook.mybook.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateNoteReqVO {
    @NotNull(message = "笔记 ID 不能为空")
    private Long id;
    private Integer type;
    private String videoUri;
    private List<String> imgUris;
    private String title;
    private String content;
    private Long topicId;

}
