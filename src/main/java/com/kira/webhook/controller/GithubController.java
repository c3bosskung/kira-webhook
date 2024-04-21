package com.kira.webhook.controller;

import com.kira.webhook.DTOs.GithubPayload.GithubPayloadDTO;
import com.kira.webhook.enums.ActionGithub;
import com.kira.webhook.services.GithubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;


@RestController
@RequestMapping("/api/github")
public class GithubController {

    @Autowired
    private GithubService githubService;

    @PostMapping("/request-reviewer")
    public String assignee(@RequestHeader(value = "X-GitHub-Event") String event, @RequestBody GithubPayloadDTO githubPayloadDTO){
        try {
            if (githubPayloadDTO.getAction().equals(ActionGithub.SYNCHRONIZE.action) &&
                    Arrays.stream(githubPayloadDTO
                            .getPull_request().getLabels())
                            .anyMatch(label -> label.getName().equals(ActionGithub.READY_FOR_REVIEW.action))) {
                System.out.println("Remove  labels");
                return "Remove labels";
            } else if (githubPayloadDTO.getAction().equals(ActionGithub.LABELED.action)) {
                HttpURLConnection conn = githubService.assignUserToReviewers(
                        githubPayloadDTO.getNumber(),
                        "POST",
                        githubPayloadDTO
                                .getPull_request()
                                .getUser()
                                .getLogin(),
                        githubPayloadDTO.getPull_request().getHtml_url()
                );
                if (conn.getResponseCode() == 201) {
                    return "Reviewer assigned";
                } else {
                    return "Error: " + conn.getResponseMessage();
                }
            } else {
                return "Action not supported";
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
