package com.kira.webhook.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    @PostMapping("/assignee")
    public String assignee(@RequestBody String body) {
        return "body: " + body;
    }

}
