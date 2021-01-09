package com.zhenglei.feign.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zhenglei.feign.feign.ConsumerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ConsumerController {

    @Resource
    private ConsumerService consumerService;

    @SentinelResource(value = "sayHello", blockHandler = "exceptionHandler")
    @GetMapping("/sayHello")
    public String getHello(){
        return consumerService.getHello();
    }

    public String exceptionHandler(BlockException e) {
        e.printStackTrace();
        return "系统繁忙请稍后再试";
    }
}
