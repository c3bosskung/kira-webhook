package com.kira.webhook.controller;

import com.kira.webhook.DTOs.GithubPayload.GithubPayloadDTO;
import com.kira.webhook.config.Discord;
import com.kira.webhook.config.Github;
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
        if (githubPayloadDTO.getAction().equals("labeled") && githubPayloadDTO.getLabel().getName().equals("Ready to Review")) {
            HttpURLConnection conn = getHttpURLConnection(githubPayloadDTO.getNumber());
            System.out.println(githubPayloadDTO.getPull_request().getUser().getLogin());
            String[] reviewers = new String[]{"BosskungGit", "c3bosskung", "Nine0512"};
            String[] filteredReviewers = IntStream.range(0, reviewers.length)
                    .filter(index -> index == queue)
                    .mapToObj(index -> reviewers[index])
                    .toArray(String[]::new);


            if (githubPayloadDTO.getPull_request().getUser().getLogin().equals(filteredReviewers[0])) {
                queue++;
                queue = queue > 2 ? 0 : queue;
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
            System.out.println(responseCode); // Should print 200
            if (responseCode == 201 && filteredReviewers.length != 0) {
                discordAnnounce(filteredReviewers[0], githubPayloadDTO.getPull_request().getHtml_url());
            }
            queue++;
            queue = queue > 2 ? 0 : queue;
        } else if (githubPayloadDTO.getAction().equals("unlabeled")) {
            return "unlabeled";
        }
        return null;
    }

    private HttpURLConnection getHttpURLConnection(Integer prNumber) throws IOException {
        URL url = new URL("https://api.github.com/repos/c3bosskung/kira-webhook/pulls/" + prNumber + "/requested_reviewers");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
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
                metion = "<@448817650869469185>";
                break;
            case "Nine0512":
                metion = "<@808572813526040616>";
                break;
            default:
                metion = "@everyone";
                break;
        }

        String msg = "Hi! " + metion + ", you have been assigned to review a pull request. Please check it out at " + urlPR + ".";

        System.out.println(msg);

        String body = "{ \"content\": \"" + msg + "\"}";
        System.out.println(body);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        System.out.println(responseCode); // Should print 200
        System.out.println(conn.getResponseMessage());

        return conn;
    }

}
