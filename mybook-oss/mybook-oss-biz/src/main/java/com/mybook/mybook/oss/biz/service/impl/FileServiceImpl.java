package com.mybook.mybook.oss.biz.service.impl;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.oss.biz.service.FileService;
import com.mybook.mybook.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {
    @Resource
    public FileStrategy fileStrategy;

    @Override
    public Response<?> uploadFile(MultipartFile file) {
        String url = fileStrategy.uploadFile(file, "mybook");
        return Response.success(url);
    }
}
