package com.experian.devicematcher.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AppController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World";
    }
}