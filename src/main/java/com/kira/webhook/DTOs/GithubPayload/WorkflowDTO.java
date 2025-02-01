package com.kira.webhook.DTOs.GithubPayload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowDTO {
    private String workflow_name;
    private String status;
    private String html_url;
    private String name;
    private String conclusion;
}
