package com.mybook.mybook.kv.api;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.kv.constant.ApiConstants;
import com.mybook.mybook.kv.dto.req.AddNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.DeleteNoteContentReqDTO;
import com.mybook.mybook.kv.dto.req.FindNoteContentReqDTO;
import com.mybook.mybook.kv.dto.rsp.FindNoteContentRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVER_NAME)
public interface KeyValueFeignApi {

    @PostMapping(value = "/kv/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);

    @PostMapping(value = "kv/note/content/find")
    Response<FindNoteContentRspDTO> findNoteContent(@RequestBody FindNoteContentReqDTO findNoteContentReqDTO);

    @PostMapping(value = "kv/note/content/delete")
    Response<?> deleteNoteContent(@RequestBody DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}
