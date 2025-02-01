package com.kira.webhook.utils;

import com.kira.webhook.config.Discord;
import com.kira.webhook.config.Github;
import com.kira.webhook.enums.ActionGithub;
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
import java.net.URL;
import java.util.Arrays;

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

    public HttpURLConnection githubReviewerAssign(String api, String reviewer, String author, Integer prNumber, String method) throws IOException {
        HttpURLConnection conn = openConnection(api + "/pulls/" + prNumber + "/requested_reviewers");
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

    public String[] getReviewerFromGithub(String api) throws IOException {
        HttpURLConnection conn = openConnection(api + "/collaborators");
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("Authorization", "Bearer " + githubSecret.getSecret());
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");

        int responseCode = conn.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            JSONArray jsonArray = new JSONArray(content.toString());

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

    public HttpURLConnection removeLabel(Integer prNumber) throws IOException {
        HttpURLConnection conn = openConnection(githubSecret.getApi() + "/issues/" + prNumber + "/labels");
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("Authorization", "Bearer " + githubSecret.getSecret());
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
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

    public HttpURLConnection discordAnnounceDeploy(String author, Boolean isProd, String URL, String step) throws IOException {
        HttpURLConnection conn = openConnection(discordSecret.getApi() +
                (isProd? discordSecret.getChannel_prod() : discordSecret.getChannel_qa()) + "/messages");
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", discordSecret.getSecret());
        conn.setDoOutput(true);

        System.out.println("discord author: " + author);
        System.out.println("discord isProd: " + isProd);
        System.out.println("discord URL: " + URL);
        System.out.println("discord step: " + step);

        try (OutputStream os = conn.getOutputStream()) {
            System.out.println();
            byte[] input;
            if (step.equals(ActionGithub.COMPLETED.action)) {
                input = getContentDeployCompleted(isProd? "everyone" : author, URL).getBytes("utf-8");
            } else if (step.equals(ActionGithub.IN_PROGRESS.action)) {
                input = getContentDeployInProgress(isProd? "everyone" : author, URL).getBytes("utf-8");
            } else {
                input = ("{ \"content\": \"" + "Something Wrong" + "\"}").getBytes("utf-8");
            }
            os.write(input, 0, input.length);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return conn;
    }

    public String getReviewer(String[] reviewers, String author) {
        String reviewer = reviewers[queue].equals(author) ?
                reviewers[queue + 1 >= reviewers.length ? 0 : ++queue] : reviewers[queue];
        queue = queue + 1 >= reviewers.length ? 0 : queue + 1;
        return reviewer;
    }

    private String getMention(String reviewer) {
        if (reviewer.equals(GithubUser.BOSS.user)) {
            return DiscordUser.BOSS.user;
        } else if (reviewer.equals(GithubUser.NINE.user)) {
            return DiscordUser.NINE.user;
        } else if (reviewer.equals(GithubUser.JEROME.user)) {
            return DiscordUser.JEROME.user;
        } else {
            return DiscordUser.EVERYONE.user;
        }
    }

    private String getContent(String reviewer, String urlPR, String author) {
        System.out.println("discord reviewer: " + reviewer);
        String metion = getMention(reviewer);
        String authorMention = getMention(author);
        String msg = "Hi! " + metion + ", you have been assigned to review a pull request. Please check it out at " + urlPR + ". Author: " + authorMention + ".";
        String body = "{ \"content\": \"" + msg + "\"}";
        return body;
    }

    private String getContentDeployInProgress(String author, String URL) {
        String authorMention = getMention(author);
        String msg = "Hi! " + authorMention + ", \n\n:yellow_square: your deployment is in progress. \nPlease check it out at " + URL + ".";
        String body = "{ \"content\": \"" + msg + "\"}";
        return body;
    }

    private String getContentDeployCompleted(String author, String URL) {
        String authorMention = getMention(author);
        String msg = "Hi! " + authorMention + ", \n\n:green_square: your deployment has been completed. \nPlease check it out at " + URL + ".";
        String body = "{ \"content\": \"" + msg + "\"}";
        return body;
    }

}
