// ChatAdapter.java
package com.example.proyectotitulacion; // Asegúrate que el paquete sea el correcto

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<ChatMessage> messageList; // <-- MODIFICADO: añadido 'final'

    // Constructor
    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        String displayText;

        if (message.isUserMessage()) {
            displayText = "Tú: " + message.getMessageText();

        } else {
            displayText = "Bot: " + message.getMessageText();
            // Y aquí para los mensajes del bot
            // Ejemplo: holder.messageTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        holder.messageTextView.setText(displayText);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder para cada mensaje
    // MODIFICADO: cambiado a 'public static'
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}