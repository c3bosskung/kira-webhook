package com.kira.webhook.controller;

import com.kira.webhook.DTOs.GithubPayload.GithubPayloadDTO;
import com.kira.webhook.config.Discord;
import com.kira.webhook.config.Github;
import com.kira.webhook.enums.ActionGithub;
import com.kira.webhook.enums.DiscordUser;
import com.kira.webhook.enums.GithubUser;
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
    private Github githubSecret;
    @Autowired
    private Discord discordSecret;

    private int queue = 0;

    @PostMapping("/assignee")
    public String assignee(@RequestBody GithubPayloadDTO githubPayloadDTO) throws IOException{
        if (githubPayloadDTO.getAction() != null && githubPayloadDTO.getAction().equals(ActionGithub.LABELED.action)
                && githubPayloadDTO.getLabel().getName().equals(ActionGithub.READY_FOR_REVIEW.action)) {
            HttpURLConnection conn = getHttpURLConnection(githubPayloadDTO.getNumber(), "POST");
            String[] reviewers = new String[]{GithubUser.BOSS.user, GithubUser.NINE.user};
            String[] filteredReviewers = IntStream.range(0, reviewers.length)
                    .filter(index -> index == queue)
                    .mapToObj(index -> reviewers[index])
                    .toArray(String[]::new);

            if (githubPayloadDTO.getPull_request().getUser().getLogin().equals(filteredReviewers[0])) {
                queue++;
                queue = queue >= reviewers.length ? 0 : queue;
                filteredReviewers = IntStream.range(0, reviewers.length)
                        .filter(index -> index == queue)
                        .mapToObj(index -> reviewers[index])
                        .toArray(String[]::new);
            }

            JSONArray jsonArray = new JSONArray(Arrays.asList(filteredReviewers));
            String jsonInputString = "{\"reviewers\":" + jsonArray.toString() + "}";

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 201 && filteredReviewers.length != 0) {
                discordAnnounce(filteredReviewers[0], githubPayloadDTO.getPull_request().getHtml_url());
            }
            queue++;
            queue = queue >= reviewers.length ? 0 : queue;
        } else if (githubPayloadDTO.getAction() != null && githubPayloadDTO.getAction().equals(ActionGithub.UNLABELED.action)) {
            HttpURLConnection conn = getHttpURLConnection(githubPayloadDTO.getNumber(), "DELETE");
            String jsonInputString = "{\"reviewers\": \" "+ githubPayloadDTO.getRequested_reviewers()[0].getLogin() +" \" }";

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }
        return null;
    }

    private HttpURLConnection getHttpURLConnection(Integer prNumber, String method) throws IOException {
        URL url = new URL("https://api.github.com/repos/c3bosskung/kira-webhook/pulls/" + prNumber + "/requested_reviewers");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("Authorization", "Bearer " + githubSecret.getSecret());
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        conn.setDoOutput(true);
        return conn;
    }

    private HttpURLConnection discordAnnounce(String reviewer, String urlPR) throws IOException {
        URL url = new URL("https://discord.com/api/v9/channels/1230784978699288577/messages");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", discordSecret.getSecret());
        conn.setDoOutput(true);

        String metion = "@everyone";

        switch (reviewer) {
            case "c3bosskung":
                metion = DiscordUser.BOSS.user;
                break;
            case "Nine0512":
                metion = DiscordUser.NINE.user;
                break;
            default:
                metion = DiscordUser.EVERYONE.user;
                break;
        }

        String msg = "Hi! " + metion + ", you have been assigned to review a pull request. Please check it out at " + urlPR + ".";
        String body = "{ \"content\": \"" + msg + "\"}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return conn;
    }

}
