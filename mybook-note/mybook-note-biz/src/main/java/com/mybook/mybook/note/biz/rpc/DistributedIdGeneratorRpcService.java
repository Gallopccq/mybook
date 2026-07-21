package com.mybook.mybook.note.biz.rpc;

import org.springframework.stereotype.Service;

import com.mybook.mybook.distributed.id.generator.api.DistributedIdGeneratorFeignApi;

import jakarta.annotation.Resource;

@Service
public class DistributedIdGeneratorRpcService {
    @Resource
    DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    private static final String BIZ_TAG_MYBOOK_ID = "leaf-segment-mybook-id";
    private static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";
    private static final String BIZ_TAG_SNOWFLAKE_ID = "leaf-snowflake-id";
    

    public String getMybookId(){
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_MYBOOK_ID);
    }

    public String getUserId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID);
    }

    public String getSnowflakeId(){
        return distributedIdGeneratorFeignApi.getSnowflakeId(BIZ_TAG_SNOWFLAKE_ID);
    }
}
