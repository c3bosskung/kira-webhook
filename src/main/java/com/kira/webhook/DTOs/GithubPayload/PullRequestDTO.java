package com.kira.webhook.DTOs.GithubPayload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PullRequestDTO {
    private UserDTO user;
    private String html_url;
}
