package com.mybook.mybook.note.biz.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.note.biz.model.vo.FindNoteDetailReqVO;
import com.mybook.mybook.note.biz.model.vo.FindNoteDetailRspVO;
import com.mybook.mybook.note.biz.model.vo.PublishNoteReqVO;

public interface NoteService {

    Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);

    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);

    Response<FindNoteDetailRspVO> findNoteDetailWithSync(FindNoteDetailReqVO findNoteDetailReqVO);
}
