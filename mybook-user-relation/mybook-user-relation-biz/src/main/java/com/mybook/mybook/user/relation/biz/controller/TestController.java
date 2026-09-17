package com.mybook.mybook.user.relation.biz.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.mybook.framework.common.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/relation")
public class TestController {

    @Value("${mq-consumer.follow-unfollow.rate-limit}")
    private double rateLimit;
    @Resource
    private RateLimiter rateLimiter;

    @GetMapping("/rate-limit")
    public Response<String> getRateLimit(){
        String res = String.valueOf(rateLimit) +", "+ rateLimiter.getRate();
        return Response.success(res);
    }
}
