package com.mybook.mybook.kv.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AddNoteContentReqDTO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long id;
    @NotBlank(message = "笔记内容不能为空")
    private String noteContent;
}
