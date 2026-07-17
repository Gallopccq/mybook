package com.mybook.mybook.oss.biz.factory;

import com.mybook.mybook.oss.biz.strategy.FileStrategy;
import com.mybook.mybook.oss.biz.strategy.impl.AliyunOSSFileStrategy;
import com.mybook.mybook.oss.biz.strategy.impl.MinioFileStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RefreshScope
public class FileStrategyFactory {

    @Value("${storage.type}")
    private String storageType;

    @Bean
    @RefreshScope
    public FileStrategy getFileStrategy(){
        if (StringUtils.equals(storageType, "minio")){
            return new MinioFileStrategy();
        } else if (StringUtils.equals(storageType, "aliyun")) {
            return new AliyunOSSFileStrategy();
        }
        throw new IllegalArgumentException("不可用的存储类型");
    }
}
