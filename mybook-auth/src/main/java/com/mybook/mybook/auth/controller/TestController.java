package com.mybook.mybook.auth.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @NacosValue(value = "${rate-limit.api.limit:}", autoRefreshed = true)
    private Integer rateLimit;

    @GetMapping("/test/ratelimit")
    @ResponseBody
    public String testRateLimit(){
        return "rate-limit: " + rateLimit;
    }
}