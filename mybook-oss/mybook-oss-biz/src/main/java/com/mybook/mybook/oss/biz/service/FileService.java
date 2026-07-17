package com.mybook.mybook.oss.biz.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    Response<?> uploadFile(MultipartFile file);
}
