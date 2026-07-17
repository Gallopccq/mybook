package com.mybook.mybook.kv.biz.controller;


import com.mybook.framework.biz.operationlog.aspect.ApiOperationLog;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.kv.biz.service.NoteContentService;
import com.mybook.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.mybook.mybook.kv.dto.rsp.FindNoteContentRspDTO;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kv")
public class NoteContentController {
    @Resource
    private NoteContentService noteContentService;

    @PostMapping("/note/content/add")
    @ApiOperationLog(description = "根据 noteId 查询笔记内容")
    public Response<?> addNoteContent(@RequestBody @Validated AddNoteContentReqDTO addNoteContentReqDTO){
        return noteContentService.addNoteContent(addNoteContentReqDTO);
    }

    @PostMapping("/note/content/find")
    @ApiOperationLog(description = "")
    public Response<FindNoteContentRspDTO> findNoteContent(@RequestBody @Validated FindNoteContentReqDTO findNoteContentReqDTO){
        return noteContentService.findNoteContent(findNoteContentReqDTO);
    }

    @PostMapping("/note/content/delete")
    @ApiOperationLog(description = "根据 noteId 删除笔记内容")
    public Response<?> deleteNoteContent(@RequestBody @Validated DeleteNoteContentReqDTO deleteNoteContentReqDTO){
        return noteContentService.deleteNoteContent(deleteNoteContentReqDTO);
    }

}
