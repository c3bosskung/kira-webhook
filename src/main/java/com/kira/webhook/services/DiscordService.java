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
}
