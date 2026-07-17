package com.mybook.mybook.user.biz.rpc;

import com.mybook.mybook.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class DistributedIdGeneratorRpcService {
    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    private static final String BIZ_TAG_MYBOOK_ID = "leaf-segment-mybook-id";
    private static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";

    public String getMybookId(){
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_MYBOOK_ID);
    }

    public String getUserId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID);
    }
}
