package com.zhenglei.feign.feign.fallback;

import com.zhenglei.feign.feign.ConsumerService;
import org.springframework.stereotype.Service;

@Service
public class ConsumerServiceImpl implements ConsumerService {
    @Override
    public String getHello() {
        return "break down";
    }
}
