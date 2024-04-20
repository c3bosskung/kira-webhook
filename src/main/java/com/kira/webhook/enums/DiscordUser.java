package com.kira.webhook.enums;

public enum DiscordUser {
    BOSS("<@448817650869469185>"),
    NINE("<@808572813526040616>"),
    EVERYONE("@everyone");

    public final String user;

    DiscordUser(String user) {
        this.user = user;
    }
}
