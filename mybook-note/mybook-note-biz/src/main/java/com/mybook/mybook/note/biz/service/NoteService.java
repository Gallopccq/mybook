package com.mybook.mybook.note.biz.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.note.biz.model.vo.PublishNoteReqVO;

public interface NoteService {

    Response<?>  findNoteDetail();

    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);
}
