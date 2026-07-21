package com.mybook.mybook.note.biz.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mybook.framework.biz.operationlog.aspect.ApiOperationLog;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.note.biz.model.vo.PublishNoteReqVO;
import com.mybook.mybook.note.biz.service.NoteService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/note")
@Slf4j
public class NoteController {
    @Resource
    NoteService noteService;
    
    @PostMapping("/publish")
    @ApiOperationLog(description = "笔记发布")
    public Response<?> publishNote(@RequestBody @Validated PublishNoteReqVO publishNoteReqVO) {
        return noteService.publishNote(publishNoteReqVO);
    }
}
