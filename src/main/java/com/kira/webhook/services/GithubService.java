package com.kira.webhook.services;

import com.kira.webhook.enums.GithubUser;
import com.kira.webhook.utils.SendRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;

@Service
public class GithubService {

    @Autowired
    private SendRequestUtils sendRequestUtils;

    @Autowired DiscordService discordService;

    public HttpURLConnection assignUserToReviewers(Integer prNumber, String method, String author, String prURL) throws IOException {
        String[] reviewers = new String[]{GithubUser.BOSS.user, GithubUser.NINE.user};
        String reviewer = sendRequestUtils.getReviewer(reviewers, author);
        sendRequestUtils.githubReviewerAssign(reviewer, author, prNumber, method);
        return discordService.sendMessage(reviewer, prURL, author);
    }
}
