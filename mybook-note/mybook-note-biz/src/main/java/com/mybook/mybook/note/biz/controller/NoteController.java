package com.mybook.mybook.note.biz.controller;

import com.mybook.mybook.note.biz.model.vo.UpdateNoteReqVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mybook.framework.biz.operationlog.aspect.ApiOperationLog;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.note.biz.model.vo.FindNoteDetailReqVO;
import com.mybook.mybook.note.biz.model.vo.FindNoteDetailRspVO;
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

    @PostMapping("/detail")
    @ApiOperationLog(description = "查询笔记详情")
    public Response<FindNoteDetailRspVO> findNoteDetail(@RequestBody @Validated FindNoteDetailReqVO findNoteDetailReqVO) {
        return noteService.findNoteDetail(findNoteDetailReqVO);
    }

    @PostMapping("/detailWithSync")
    @ApiOperationLog(description = "查询笔记详情")
    public Response<FindNoteDetailRspVO> findNoteDetailWithSync(@RequestBody @Validated FindNoteDetailReqVO findNoteDetailReqVO) {
        return noteService.findNoteDetailWithSync(findNoteDetailReqVO);
    }

    @PostMapping("update")
    @ApiOperationLog(description = "更新笔记信息")
    public Response<?> updateNote(@RequestBody @Validated UpdateNoteReqVO updateNoteReqVO){
        return noteService.updateNote(updateNoteReqVO);
    }
    
}
