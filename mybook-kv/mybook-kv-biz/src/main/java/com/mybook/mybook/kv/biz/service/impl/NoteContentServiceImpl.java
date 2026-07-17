package com.mybook.mybook.kv.biz.service.impl;

import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.kv.biz.domain.dataobject.NoteContentDO;
import com.mybook.mybook.kv.biz.domain.repository.NoteContentRepository;
import com.mybook.mybook.kv.biz.enums.ResponseCodeEnum;
import com.mybook.mybook.kv.biz.service.NoteContentService;
import com.mybook.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.mybook.mybook.kv.dto.rsp.FindNoteContentRspDTO;
import jakarta.annotation.Resource;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class NoteContentServiceImpl implements NoteContentService {

    @Resource
    private NoteContentRepository noteContentRepository;
    /**
     * 将新增 Note 保存到 Cassandra 中。
     * @param addNoteContentReqDTO
     * @return
     */
    @Override
    public Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO) {
        Long id = addNoteContentReqDTO.getId();
        String content = addNoteContentReqDTO.getNoteContent();
        NoteContentDO noteContentDO = NoteContentDO.builder()
                .id(UUID.randomUUID())
                .content(content)
                .build();
        noteContentRepository.save(noteContentDO);
        return Response.success();
    }


    /**
     * 根据笔记 id 找到对应的笔记 content
     * @param findNoteContentReqDTO
     * @return FindNoteContentRspDTO
     */
    @Override
    public Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO) {
        String noteId = findNoteContentReqDTO.getNoteId();
        Optional<NoteContentDO> optional = noteContentRepository.findById(UUID.fromString(noteId));
        if (!optional.isPresent()){
            throw new BizException(ResponseCodeEnum.NOTE_CONTENT_NOT_FOUND);
        }
        NoteContentDO noteContentDO = optional.get();
        FindNoteContentRspDTO findNoteContentRspDTO = FindNoteContentRspDTO.builder()
                .noteId(noteContentDO.getId())
                .content(noteContentDO.getContent())
                .build();

        return Response.success(findNoteContentRspDTO);
    }

    @Override
    public Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        String noteId = deleteNoteContentReqDTO.getNoteId();
        noteContentRepository.deleteById(UUID.fromString(noteId));
        return Response.success();
    }
}
