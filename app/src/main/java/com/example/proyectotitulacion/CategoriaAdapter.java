package com.example.proyectotitulacion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ViewHolder> {

    private ArrayList<com.example.proyectotitulacion.Categoria> categorias;

    public CategoriaAdapter(ArrayList<com.example.proyectotitulacion.Categoria> categorias) {
        this.categorias = categorias;
    }

    @NonNull
    @Override
    public CategoriaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaAdapter.ViewHolder holder, int position) {
        com.example.proyectotitulacion.Categoria categoria = categorias.get(position);
        holder.nombre.setText(categoria.getNombre());
        holder.icono.setImageResource(categoria.getIcono());
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre;
        ImageView icono;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.tvNombreCategoria);
            icono = itemView.findViewById(R.id.imgIconoCategoria);
        }
    }
}
