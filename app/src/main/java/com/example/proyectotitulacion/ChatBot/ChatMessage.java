package com.example.proyectotitulacion.ChatBot;


public class ChatMessage {
    private final String text;
    private final boolean isUser;
    private final boolean isTyping; // para animación "escribiendo…"


    public ChatMessage(String text, boolean isUser) {
        this(text, isUser, false);
    }


    public ChatMessage(String text, boolean isUser, boolean isTyping) {
        this.text = text;
        this.isUser = isUser;
        this.isTyping = isTyping;
    }


    public String getText() { return text; }
    public boolean isUser() { return isUser; }
    public boolean isTyping() { return isTyping; }
}