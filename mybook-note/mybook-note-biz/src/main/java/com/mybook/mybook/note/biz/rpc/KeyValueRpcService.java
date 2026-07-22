package com.mybook.mybook.note.biz.rpc;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.kv.api.KeyValueFeignApi;
import com.mybook.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.mybook.mybook.kv.dto.rsp.FindNoteContentRspDTO;
import com.mybook.mybook.note.biz.enums.ResponseCodeEnum;

import jakarta.annotation.Resource;

@Service
public class KeyValueRpcService {
    @Resource
    KeyValueFeignApi keyValueFeignApi;

    public boolean saveNoteContent(String uuid, String noteContent){
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

    public String findNoteContent(String uuid){
        FindNoteContentReqDTO findNoteContentReqDTO = FindNoteContentReqDTO.builder().uuid(uuid).build();
        Response<FindNoteContentRspDTO> response = keyValueFeignApi.findNoteContent(findNoteContentReqDTO);
        if (Objects.isNull(response) || !response.isSuccess() || Objects.isNull(response.getData())){
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }
        return response.getData().getContent();
    }
}
