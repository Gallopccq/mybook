package com.mybook.mybook.kv.biz.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.mybook.mybook.kv.dto.rsp.FindNoteContentRspDTO;

public interface NoteContentService {

    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

    Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);

    Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}
