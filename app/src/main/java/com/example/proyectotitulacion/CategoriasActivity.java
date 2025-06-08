package com.example.proyectotitulacion;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class CategoriasActivity extends AppCompatActivity {

    RecyclerView recyclerCategorias;
    ArrayList<com.example.proyectotitulacion.Categoria> listaCategorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        recyclerCategorias = findViewById(R.id.recyclerCategorias);
        recyclerCategorias.setLayoutManager(new GridLayoutManager(this, 3));

        listaCategorias = new ArrayList<>();
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Fisioterapia General", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Fisioterapia Traumatologica", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Fisioterapia neural", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Fisioterapia Geriatrica", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Fisioterapia Deportiva", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Fisioterapia Oncologica", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Acupuntura", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Entrenamiento Fisico", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Osteopatia", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Kinesiologia", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Rehabilitación", R.drawable.persona_andadera));
        listaCategorias.add(new com.example.proyectotitulacion.Categoria("Fitoterapia", R.drawable.persona_andadera));

        recyclerCategorias.setAdapter(new CategoriaAdapter(listaCategorias));

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_categories);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_categories) {
                return true;
            } else if (id == R.id.nav_calendar) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_chat) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, PerfilActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });


    }

}
