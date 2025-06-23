package com.example.proyectotitulacion;

public class ChatMessage {
    private final String messageText;
    private final boolean isUserMessage;
    public ChatMessage(String messageText, boolean isUserMessage) {
        this.messageText = messageText;
        this.isUserMessage = isUserMessage;
    }
    public String getMessageText() {
        return messageText;
    }
    public boolean isUserMessage() {
        return isUserMessage;
    }
}