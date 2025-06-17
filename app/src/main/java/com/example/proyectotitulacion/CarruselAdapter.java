package com.example.proyectotitulacion;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarruselAdapter extends RecyclerView.Adapter<CarruselAdapter.CarruselViewHolder> {

    private Context context;
    private List<Integer> imagenes;

    public CarruselAdapter(Context context, List<Integer> imagenes) {
        this.context = context;
        this.imagenes = imagenes;
    }

    @NonNull
    @Override
    public CarruselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_carrousel, parent, false);
        return new CarruselViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull CarruselViewHolder holder, int position) {
        holder.imageView.setImageResource(imagenes.get(position));
    }

    @Override
    public int getItemCount() {
        return imagenes.size();
    }

    public static class CarruselViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public CarruselViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageCarrusel);
        }
    }
}
