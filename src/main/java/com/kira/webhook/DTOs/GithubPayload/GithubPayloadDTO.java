package com.kira.webhook.DTOs.GithubPayload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubPayloadDTO {
    private String action;
    private Integer number;
    private LabelsDTO label;
}
