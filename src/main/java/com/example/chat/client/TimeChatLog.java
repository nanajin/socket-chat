package com.example.chat.client;

import java.time.Instant;

public class TimeChatLog {
    private final String element;
    private final Instant addedAt;

    public TimeChatLog(String element) {
        this.element = element;
        this.addedAt = Instant.now();
    }

    public String getElement() {
        return element;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
