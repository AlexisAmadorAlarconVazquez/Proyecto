package com.example.proyectotitulacion.ChatBot;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyectotitulacion.R;
import java.util.List;


public class QuickReplyAdapter extends RecyclerView.Adapter<QuickReplyAdapter.VH> {


    public interface OnQuickTap { void onTap(String text); }


    private final List<String> items;
    private final OnQuickTap callback;


    public QuickReplyAdapter(List<String> items, OnQuickTap callback) {
        this.items = items;
        this.callback = callback;
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quick_reply, parent, false);
        return new VH(v);
    }


    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String text = items.get(position);
        holder.txt.setText(text);
        holder.itemView.setOnClickListener(v -> callback.onTap(text));
    }


    @Override
    public int getItemCount() { return items.size(); }


    static class VH extends RecyclerView.ViewHolder {
        TextView txt;
        VH(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.textQuickReply);
        }
    }
}