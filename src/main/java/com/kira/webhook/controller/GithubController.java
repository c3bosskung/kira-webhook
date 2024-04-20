package com.kira.webhook.controller;

import com.kira.webhook.DTOs.GithubPayload.GithubPayloadDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    @PostMapping("/assignee")
    public String assignee(@RequestBody GithubPayloadDTO githubPayloadDTO) {
        if (githubPayloadDTO.getAction().equals("labeled")) {
            return "labeled";
        } else if (githubPayloadDTO.getAction().equals("unlabeled")) {
            return "unlabeled";
        }
        return null;
    }

}
