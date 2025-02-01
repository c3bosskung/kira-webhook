package com.kira.webhook.services;

import com.kira.webhook.utils.SendRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;

@Service
public class DiscordService {

    @Autowired
    private SendRequestUtils sendRequestUtils;

    public HttpURLConnection sendMessage(String reviewer, String prURL, String author) throws IOException {
       return sendRequestUtils.discordAnnounce(reviewer, prURL, author);
    }

    public HttpURLConnection sendMessageDeploymentStatus(String author, Boolean isProd, String URL, String step, String branch) throws IOException {
        System.out.println("Sending deployment status to Discord");
        //value
        System.out.println("-----------------");
        System.out.println("author: " + author);
        System.out.println("isProd: " + isProd);
        System.out.println("URL: " + URL);
        System.out.println("step: " + step);
        System.out.println("branch: " + branch);
        System.out.println("-----------------");
        return sendRequestUtils.discordAnnounceDeploy(author, isProd, URL, step, branch);
    }
}
