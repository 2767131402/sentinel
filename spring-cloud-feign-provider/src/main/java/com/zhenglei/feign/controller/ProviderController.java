package com.zhenglei.feign.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderController {


    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @GetMapping("/sayHello")
    public String sayHello() {
        return "Hello provider ";
    }

    @GetMapping("/sayHi")
    public String sayHi() {
        String name = configurableApplicationContext.getEnvironment().getProperty("name");
        return "Hi provider " + name;
    }
}
