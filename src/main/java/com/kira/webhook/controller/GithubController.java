package com.kira.webhook.controller;

import com.kira.webhook.DTOs.GithubPayload.GithubPayloadDTO;
import com.kira.webhook.config.Github;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    @Autowired
    private Github githubSecret;

    @PostMapping("/assignee")
    public String assignee(@RequestBody GithubPayloadDTO githubPayloadDTO) throws IOException{
        if (githubPayloadDTO.getAction().equals("labeled") && githubPayloadDTO.getLabel().getName().equals("Ready to Review")) {
            HttpURLConnection conn = getHttpURLConnection(githubPayloadDTO.getNumber());
            System.out.println(githubPayloadDTO.getPull_request().getUser().getLogin());
            String[] reviewers = new String[]{"BosskungGit", "c3bosskung", "nine0512"};
            JSONArray jsonArray = new JSONArray(Arrays.stream(reviewers)
                    .filter(reviewer ->
                            !reviewer.equals(githubPayloadDTO.getPull_request().getUser().getLogin())).toArray());
            String jsonInputString = "{\"reviewers\":" + jsonArray.toString() + "}";

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println(responseCode); // Should print 200
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

}
