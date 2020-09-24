package com.mohammadreza.mrchat.notification;

import androidx.annotation.Keep;

@Keep
public class Data {
    private String sender;
    private String message;
    private long messageId;

    public Data(String sender, String message, long messageId) {
        this.sender = sender;
        this.message = message;
        this.messageId = messageId;
    }

    public Data() {
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public long getMessageId() {
        return messageId;
    }
}
