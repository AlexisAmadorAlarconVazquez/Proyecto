package com.example.proyectotitulacion.ChatBot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectotitulacion.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT = 1;
    private static final int TYPE_TYPING = 2;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages, ChatActivity chatActivity) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage m = messages.get(position);
        if (m.isTyping()) return TYPE_TYPING;
        return m.isUser() ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            View v = inf.inflate(R.layout.item_message_user, parent, false);
            return new UserVH(v);
        } else if (viewType == TYPE_BOT) {
            View v = inf.inflate(R.layout.item_message_bot, parent, false);
            return new BotVH(v);
        } else {
            View v = inf.inflate(R.layout.item_message_bot_typing, parent, false);
            return new TypingVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = messages.get(position);
        if (holder instanceof UserVH) {
            ((UserVH) holder).txt.setText(m.getText());
            animate(holder.itemView);
        } else if (holder instanceof BotVH) {
            ((BotVH) holder).txt.setText(m.getText());
            animate(holder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void animate(View itemView) {
        Animation anim = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.item_fade_in);
        itemView.startAnimation(anim);
    }

    public interface OnQuickReplyClickListener {
        void onQuickReplyClick(String reply);
    }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView txt;
        UserVH(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.textMessageUser);
        }
    }

    static class BotVH extends RecyclerView.ViewHolder {
        TextView txt;
        BotVH(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.textMessageBot);
        }
    }

    static class TypingVH extends RecyclerView.ViewHolder {
        TypingVH(@NonNull View itemView) {
            super(itemView);
        }
    }
}
