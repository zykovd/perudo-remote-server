package com.suai.perudo.web;

import com.google.gson.annotations.Expose;

public class ChatMessage {
    @Expose private String senderName;
    @Expose private String message;

    public ChatMessage(String senderName, String message) {
        this.senderName = senderName;
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return senderName + " : " + message;
    }
}
