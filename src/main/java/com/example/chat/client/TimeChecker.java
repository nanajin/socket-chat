package com.example.chat.client;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeChecker implements Runnable {
    private final ObservableList<TimeChatLog> chatLog;
    private Timeline timeline;
    private boolean isStop;

    public TimeChecker(ObservableList<TimeChatLog> chatLog) {
        this.chatLog = chatLog;
    }

    public void stopTime() {
        timeline.stop();
        isStop = true;
    }

    public void reStart() {
        timeline.play();
        isStop = false;
    }

    public boolean isStop() {
        return isStop;
    }

    @Override
    public void run() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event ->
            chatLog.removeIf(item -> ChronoUnit.SECONDS.between(item.getAddedAt(), Instant.now()) >= 30)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        isStop = false;
        timeline.play();
    }
}
