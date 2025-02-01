package com.kira.webhook.enums;

public enum ActionGithub {
    LABELED("labeled"),
    UNLABELED("unlabeled"),
    READY_FOR_REVIEW("Ready to Review"),
    SYNCHRONIZE("synchronize"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed");

    public final String action;

    ActionGithub(String action) {
        this.action = action;
    }
}
