package com.mybook.mybook.note.biz.rpc;

import java.util.Objects;
import java.util.UUID;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.kv.api.KeyValueFeignApi;
import com.mybook.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.DeleteNoteContentReqDTO;

import jakarta.annotation.Resource;

public class KeyValueRpcService {
    @Resource
    KeyValueFeignApi keyValueFeignApi;

    public boolean addNoteContent(String uuid, String noteContent){
        AddNoteContentReqDTO addNoteContentReqDTO = AddNoteContentReqDTO.builder()
            .uuid(uuid)
            .noteContent(noteContent)
            .build();
        Response<?> response = keyValueFeignApi.addNoteContent(addNoteContentReqDTO);
        if (Objects.isNull(response) || !response.isSuccess()){
            return false;
        }
        return true;
    }

    public boolean deleteNoteContent(String uuid){
        DeleteNoteContentReqDTO deleteNoteContentReqDTO = DeleteNoteContentReqDTO.builder()
            .uuid(uuid)
            .build();
        Response<?> response = keyValueFeignApi.deleteNoteContent(deleteNoteContentReqDTO);
        if (Objects.isNull(response) || !response.isSuccess()){
            return false;
        }
        return true;
    }
}
