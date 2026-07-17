package com.mybook.mybook.oss.biz.strategy.impl;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.oss.biz.config.MinioProperties;
import com.mybook.mybook.oss.biz.strategy.FileStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class MinioFileStrategy implements FileStrategy {
    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;

    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("Minio oss serve");

        if (Objects.isNull(file) || file.getSize() == 0){
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }

        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();
        String key = UUID.randomUUID().toString().replace("-","");
        // 获取文件名后缀，如.jpg
        String suffix = originalName.substring(originalName.lastIndexOf("."));
        String objectName = String.format("%s%s", key, suffix);
        log.info("开始上传文件至 Minio，ObjectName：{}",objectName);

        try{
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(),file.getSize(),-1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (InternalException | XmlParserException | InvalidResponseException | InvalidKeyException | NoSuchAlgorithmException | ErrorResponseException | InsufficientDataException | ServerException | IOException e) {
            e.printStackTrace();
            return "";
        }
        String url = String.format("%s/%s/%s", minioProperties.getEndPoint(), bucketName, objectName);
        log.info("==> 上传文件至 Minio 成功，访问路径: {}", url);
        return url;
    }
}
