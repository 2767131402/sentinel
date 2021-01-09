package com.zhenglei.sentinel.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        try (Entry entry = SphU.entry("Hello")) {
            return "Hello Sentinel";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "系统繁忙，请稍后";
    }

    /**
     * 定义限流规则
     *
     * @PostConstruct 此注解的含义是：本类构造方法执行结束后执行
     */
    @PostConstruct
    public void init() {
        //1.创建存放限流规则的集合
        List<FlowRule> rules = new ArrayList<>();
        //2.创建限流规则
        FlowRule rule = new FlowRule();
        //定义资源，表示Sentinel会对哪个资源生效
        rule.setResource("Hello");
        //定义限流的类型(此处使用QPS作为限流类型)
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //定义QPS每秒通过的请求数
        rule.setCount(2);
        //3.将限流规则存放到集合中
        rules.add(rule);
        //4.加载限流规则
        FlowRuleManager.loadRules(rules);
    }
}

