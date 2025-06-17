// ChatMessage.java
package com.example.proyectotitulacion; // Asegúrate que el paquete sea el correcto

public class ChatMessage {
    private final String messageText; // <-- MODIFICADO: añadido 'final'
    private final boolean isUserMessage; // <-- MODIFICADO: añadido 'final'

    // Constructor
    public ChatMessage(String messageText, boolean isUserMessage) {
        this.messageText = messageText;
        this.isUserMessage = isUserMessage;
    }

    // Getters
    public String getMessageText() {
        return messageText;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }
}