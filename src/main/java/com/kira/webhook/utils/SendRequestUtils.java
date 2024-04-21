package com.kira.webhook.utils;

import com.kira.webhook.config.Discord;
import com.kira.webhook.config.Github;
import com.kira.webhook.enums.DiscordUser;
import com.kira.webhook.enums.GithubUser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.IntStream;

@Service
public class SendRequestUtils {

    @Autowired
    private Github githubSecret;

    @Autowired
    private Discord discordSecret;

    private static Integer queue = 0;

    public HttpURLConnection openConnection(String URLString) throws IOException {
        URL url = new URL(URLString);
        return  (HttpURLConnection) url.openConnection();
    }

    public HttpURLConnection githubReviewerAssign(String reviewer, String author, Integer prNumber, String method) throws IOException {
        HttpURLConnection conn = openConnection(githubSecret.getApi() + prNumber + "/requested_reviewers");
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("Authorization", "Bearer " + githubSecret.getSecret());
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        conn.setDoOutput(true);

        JSONArray jsonArray = new JSONArray(Arrays.asList(reviewer));
        String content = "{\"reviewers\":" + jsonArray + "}";

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = content.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return conn;
    }

    public String[] getReviewerFromGithub() throws IOException {
        HttpURLConnection conn = openConnection("https://api.github.com/repos/c3bosskung/kira-webhook/collaborators");
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("Authorization", "Bearer " + githubSecret.getSecret());
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");

        // Get the response
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Close connections
            in.close();
            conn.disconnect();

            // Parse JSON response
            JSONArray jsonArray = new JSONArray(content.toString());

            // Convert JSONArray to String array
            String[] reviewers = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                reviewers[i] = jsonObject.getString("login");
            }

            return reviewers;
        } else {
            System.out.println("GET request not worked");
        }

        return null;
    }

    public HttpURLConnection discordAnnounce(String reviewer, String urlPR, String author) throws IOException {
        HttpURLConnection conn = openConnection(discordSecret.getApi() + discordSecret.getChannel() + "/messages");
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", discordSecret.getSecret());
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = getContent(reviewer, urlPR, author).getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return conn;
    }

    public String getReviewer(String[] reviewers, String author) {
        System.out.println("queue: " + queue);
        String reviewer = reviewers[queue].equals(author) ?
                reviewers[queue + 1 >= reviewers.length ? 0 : queue++] : reviewers[queue];
        queue++;
        System.out.println("queue (Person): " + reviewer);
        return reviewer;
    }

    private String getMention(String reviewer) {
        if (reviewer.equals(GithubUser.BOSS.user)) {
            return DiscordUser.BOSS.user;
        } else if (reviewer.equals(GithubUser.NINE.user)) {
            return DiscordUser.NINE.user;
        } else {
            return DiscordUser.EVERYONE.user;
        }
    }

    private String getContent(String reviewer, String urlPR, String author) {
        String metion = getMention(reviewer);
        String authorMention = getMention(author);
        String msg = "Hi! " + metion + ", you have been assigned to review a pull request. Please check it out at " + urlPR + ". Author: " + authorMention + ".";
        String body = "{ \"content\": \"" + msg + "\"}";
        return body;
    }

}
