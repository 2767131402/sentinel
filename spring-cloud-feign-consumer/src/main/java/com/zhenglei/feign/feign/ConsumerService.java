package com.zhenglei.feign.feign;

import com.zhenglei.feign.feign.fallback.ConsumerServiceImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "nacos-provider", fallback = ConsumerServiceImpl.class)
public interface ConsumerService {

    @GetMapping("/sayHello")
    String getHello();
}
