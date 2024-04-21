package com.kira.webhook.services;

import com.kira.webhook.utils.SendRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

@Service
public class GithubService {

    @Autowired
    private SendRequestUtils sendRequestUtils;

    @Autowired DiscordService discordService;

    public HttpURLConnection assignUserToReviewers(Integer prNumber, String method, String author, String prURL) throws IOException {
        String[] reviewers = sendRequestUtils.getReviewerFromGithub();
        System.out.println("All reviewer: " + Arrays.toString(reviewers));
        String reviewer = sendRequestUtils.getReviewer(reviewers, author);

        System.out.println("Reviewer: " + reviewer);
        System.out.println("author: " + author);
        System.out.println("prNumber: " + prNumber);
        System.out.println("method: " + method);
        System.out.println("prURL: " + prURL);

        HttpURLConnection conn = sendRequestUtils.githubReviewerAssign(reviewer, author, prNumber, method);
        System.out.println("Connection Github: " + conn.getResponseCode());
        System.out.println("Connection Github: " + conn.getResponseMessage());
        if (conn.getResponseCode() == 201) {
            HttpURLConnection dis = discordService.sendMessage(reviewer, prURL, author);
            System.out.println("Connection Discord: " + dis.getResponseCode());
            System.out.println("Connection Discord: " + dis.getResponseMessage());
        }
        return conn;
    }
}
