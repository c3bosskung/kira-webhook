package com.kira.webhook.services;

import com.kira.webhook.DTOs.GithubPayload.RequestReviewer;
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

    public HttpURLConnection assignUserToReviewers(String api, Integer prNumber, String method, String author, String prURL, RequestReviewer[] existReviewer) throws IOException {
        String[] reviewers = Arrays.stream(sendRequestUtils.getReviewerFromGithub(api)).filter(s -> !s.equals("iamyourdeadpool")).toArray(String[]::new);

        if (reviewers == null || reviewers.length == 0) {
            return null;
        }

        System.out.println("All reviewer: " + Arrays.toString(reviewers));

        System.out.println("author: " + author);
        System.out.println("prNumber: " + prNumber);
        System.out.println("method: " + method);
        System.out.println("prURL: " + prURL);

        String reviewerCondition = existReviewer.length == 0? sendRequestUtils.getReviewer(reviewers, author) : existReviewer[0].getLogin().toString();

        System.out.println("reviewerCondition: " + reviewerCondition);
        HttpURLConnection conn = sendRequestUtils.githubReviewerAssign(api, reviewerCondition, author, prNumber, method);
        System.out.println("Connection Github: " + conn.getResponseCode());
        System.out.println("Connection Github: " + conn.getResponseMessage());

        if (existReviewer.length > 0 || conn.getResponseCode() == 201) {
            HttpURLConnection dis = discordService.sendMessage(reviewerCondition, prURL, author);
            System.out.println("Connection Discord: " + dis.getResponseCode());
            System.out.println("Connection Discord: " + dis.getResponseMessage());
        }
        return conn;
    }

    public HttpURLConnection removeLabel(Integer prNumber) throws IOException {
        return sendRequestUtils.removeLabel(prNumber);
    }
}
