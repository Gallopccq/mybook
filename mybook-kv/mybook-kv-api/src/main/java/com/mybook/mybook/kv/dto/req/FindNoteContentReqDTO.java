package com.mybook.mybook.kv.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindNoteContentReqDTO {

    @NotBlank(message = "笔记 UUID 不能为空")
    private String uuid;
}
