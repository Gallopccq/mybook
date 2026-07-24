package com.mybook.mybook.note.biz.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.note.biz.model.vo.*;

public interface NoteService {

    Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);

    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);

    Response<FindNoteDetailRspVO> findNoteDetailWithSync(FindNoteDetailReqVO findNoteDetailReqVO);

    Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO);

    void deleteNoteLocalCache(Long noteId);

    Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO);

    Response<?> updateNoteVisibleOnlyMe(UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO);

    Response<?> updateNoteIsTop(UpdateNoteTopReqVO updateNoteTopReqVO);
}
