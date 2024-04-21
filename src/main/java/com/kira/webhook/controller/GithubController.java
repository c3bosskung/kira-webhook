package com.kira.webhook.controller;

import com.kira.webhook.DTOs.GithubPayload.GithubPayloadDTO;
import com.kira.webhook.config.Discord;
import com.kira.webhook.config.Github;
import com.kira.webhook.enums.ActionGithub;
import com.kira.webhook.enums.DiscordUser;
import com.kira.webhook.enums.GithubUser;
import com.kira.webhook.services.DiscordService;
import com.kira.webhook.services.GithubService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    @Autowired
    private GithubService githubService;
    @Autowired
    private DiscordService discordService;
    @Autowired
    private Discord discord;

    @PostMapping("/assignee")
    public String assignee(@RequestBody GithubPayloadDTO githubPayloadDTO) throws IOException{
        try {
            if (githubPayloadDTO.getAction().equals(ActionGithub.LABELED.action)) {
                githubService.assignUserToReviewers(
                        githubPayloadDTO.getNumber(),
                        "POST",
                        githubPayloadDTO
                                .getPull_request()
                                .getUser()
                                .getLogin(),
                        githubPayloadDTO.getPull_request().getHtml_url()
                );
                return "Assignee added";
            } else {
                return "Action not supported";
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

}
