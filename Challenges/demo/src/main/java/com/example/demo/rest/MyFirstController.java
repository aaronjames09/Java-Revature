package com.example.demo.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

@RestController
public class MyFirstController{

    @Value("${my.name}")
    private String name;

    private static final Logger logger = LoggerFactory.getLogger(MyFirstController.class);

    @GetMapping("/")
    public String sayHello() {
        logger.info("Get Request on /");
        return "Hello World";
    }

    @GetMapping("/test")
    public String sayHotReload() {
        return "Hot Reload Works";
    }

    @GetMapping("/name")
    public String getName() {
        logger.info("GET request received on /name");
        return "My name is: " + name;
    }
    
}