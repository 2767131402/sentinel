package com.zhenglei.feign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//使用注册中心
@EnableDiscoveryClient
//获取注册中心的接口
@EnableFeignClients
public class SpringCloudFeignConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudFeignConsumerApplication.class, args);
    }

}
