package com.kira.webhook.enums;

public enum ActionGithub {
    LABELED("labeled"),
    UNLABELED("unlabeled"),
    READY_FOR_REVIEW("Ready to Review");

    public final String action;

    ActionGithub(String action) {
        this.action = action;
    }
}
