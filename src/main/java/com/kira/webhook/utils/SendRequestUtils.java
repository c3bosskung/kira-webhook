package com.kira.webhook.utils;

import com.kira.webhook.config.Discord;
import com.kira.webhook.config.Github;
import com.kira.webhook.enums.DiscordUser;
import com.kira.webhook.enums.GithubUser;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

        String content = "{\"reviewers\":" + reviewer + "}";

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = content.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return conn;
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
        String[] filteredReviewers = IntStream.range(0, reviewers.length)
                .filter(index -> index == queue)
                .mapToObj(index -> reviewers[index])
                .toArray(String[]::new);


        if (author.equals(filteredReviewers[0])) {
           filteredReviewers[0] = reviewers[queue + 1 >= reviewers.length ? 0 : queue + 1];
        }

        queue = queue + 1 >= reviewers.length ? 0 : queue + 1;

        JSONArray jsonArray = new JSONArray(Arrays.asList(filteredReviewers));
        return jsonArray.toString();
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
