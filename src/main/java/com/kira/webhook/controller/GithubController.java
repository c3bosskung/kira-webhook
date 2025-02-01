package com.kira.webhook.controller;

import com.kira.webhook.DTOs.GithubPayload.GithubPayloadDTO;
import com.kira.webhook.enums.ActionGithub;
import com.kira.webhook.services.DiscordService;
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

    @Autowired
    private DiscordService discordService;

    @PostMapping("/request-reviewer")
    public String assignee(@RequestBody GithubPayloadDTO githubPayloadDTO){
        try {
            System.out.println(githubPayloadDTO.getRepository().getUrl());
            if (githubPayloadDTO.getAction() != null && githubPayloadDTO.getAction().equals(ActionGithub.SYNCHRONIZE.action) &&
                    Arrays.stream(githubPayloadDTO
                            .getPull_request().getLabels())
                            .anyMatch(label -> label.getName().equals(ActionGithub.READY_FOR_REVIEW.action))) {
                HttpURLConnection conn = githubService.removeLabel(githubPayloadDTO.getNumber());
                if (conn.getResponseCode() == 204) {
                    return "Label removed";
                } else {
                    return "Error: " + conn.getResponseMessage();
                }
            } else if (githubPayloadDTO.getAction() != null && githubPayloadDTO.getAction().equals(ActionGithub.LABELED.action)) {
                HttpURLConnection conn = githubService.assignUserToReviewers(
                        githubPayloadDTO.getRepository().getUrl(),
                        githubPayloadDTO.getNumber(),
                        "POST",
                        githubPayloadDTO
                                .getPull_request()
                                .getUser()
                                .getLogin(),
                        githubPayloadDTO.getPull_request().getHtml_url(),
                        githubPayloadDTO.getPull_request().getRequested_reviewers()
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

    @PostMapping("/deploy-status")
    public String deployStatus(@RequestBody GithubPayloadDTO githubPayloadDTO) {
        try {
            System.out.println(githubPayloadDTO.getRepository().getUrl());
            if (githubPayloadDTO.getAction() != null
                    && githubPayloadDTO.getWorkflow_job().getName().contains("deploy")
                    && !githubPayloadDTO.getWorkflow_job().getStatus().contains("queued")
                    && (((githubPayloadDTO.getWorkflow_job().getConclusion() == null &&
                        githubPayloadDTO.getWorkflow_job().getStatus().contains("in_progress"))) ||
                    (githubPayloadDTO.getWorkflow_job().getConclusion() != null &&
                            githubPayloadDTO.getWorkflow_job().getConclusion().contains("success")))) {
                HttpURLConnection conn = githubService.announceDeployStatus(
                        githubPayloadDTO.getSender().getLogin(),
                        githubPayloadDTO.getWorkflow_job().getWorkflow_name().toLowerCase().contains("prod"),
                        githubPayloadDTO.getWorkflow_job().getHtml_url(),
                        githubPayloadDTO.getWorkflow_job().getStatus()
                );
                if (conn.getResponseCode() == 200) {
                    return "Deployment status announced";
                } else {
                    return "Error: " + conn.getResponseMessage();
                }
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return "Action not supported";
    }
}
