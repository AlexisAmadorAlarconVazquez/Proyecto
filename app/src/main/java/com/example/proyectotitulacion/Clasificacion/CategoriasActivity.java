package com.example.proyectotitulacion.Clasificacion;

import android.app.Activity; // Necesario para el tipo en navigateToSection
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Importaciones de las Activities a las que se va a navegar
import com.example.proyectotitulacion.Citas.MainActivity;
import com.example.proyectotitulacion.Mapa.HomeActivity;
import com.example.proyectotitulacion.ChatBot.ChatActivity;
import com.example.proyectotitulacion.PerfilUsuario.PerfilActivity;

import com.example.proyectotitulacion.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;

public class CategoriasActivity extends AppCompatActivity {

    RecyclerView recyclerCategorias;
    ArrayList<Categoria> listaCategorias;
    // No es necesario declarar BottomNavigationView nav; a nivel de clase
    // si solo se usa en onCreate y no en onResume para setSelectedItemId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        recyclerCategorias = findViewById(R.id.recyclerCategorias);
        recyclerCategorias.setLayoutManager(new GridLayoutManager(this, 3));

        listaCategorias = new ArrayList<>();
        llenarCategorias();

        CategoriaAdapter adapter = new CategoriaAdapter(listaCategorias, categoria -> {
            mostrarDialogoInformacion(categoria.getDetalle());
        });
        recyclerCategorias.setAdapter(adapter);

        // --- CONFIGURACIÓN DEL BOTTOMNAVIGATIONVIEW ---
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_categories); // Marcar "Categorías" como seleccionado

            nav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_categories) {
                    // Ya estamos en Categorías
                    return true;
                } else if (id == R.id.nav_home) {
                    navigateToSection(HomeActivity.class);
                    return true;
                } else if (id == R.id.nav_calendar) {
                    navigateToSection(MainActivity.class);
                    return true;
                } else if (id == R.id.nav_chat) {
                    navigateToSection(ChatActivity.class);
                    return true;
                } else if (id == R.id.nav_profile) {
                    navigateToSection(PerfilActivity.class);
                    return true;
                }
                return false;
            });
        }
        // --- FIN DE LA CONFIGURACIÓN DEL BOTTOMNAVIGATIONVIEW ---
    }


    private void navigateToSection(Class<?> targetActivityClass) {
        if (!this.getClass().equals(targetActivityClass)) {
            Intent intent = new Intent(this, targetActivityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    private void llenarCategorias() {
        String especialista = "Fisioterapeuta Guillermo Andrés Alemán Guerrero";
        String ubicacion = "Nicolás Bravo 34 Col, La Conchita, 56600 Chalco de Díaz Covarrubias, Méx.";

        listaCategorias.add(new Categoria("Fisioterapia General", R.drawable.icon11, new CategoriaDetalle(especialista, "Especialista en Fisioterapia General", ubicacion, R.drawable.fisioterapia_general)));
        listaCategorias.add(new Categoria("Fisioterapia Traumatologica", R.drawable.icon10, new CategoriaDetalle(especialista, "Especialista en Lesiones Deportivas", ubicacion, R.drawable.fisioterapia_traumatologica)));
        listaCategorias.add(new Categoria("Fisioterapia neural", R.drawable.icon9, new CategoriaDetalle(especialista, "Especialista en Fisioterapia Neural", ubicacion, R.drawable.fisioterapia_neural)));
        listaCategorias.add(new Categoria("Fisioterapia Geriatrica", R.drawable.icon8, new CategoriaDetalle(especialista, "Especialista en Fisioterapia Geriátrica", ubicacion, R.drawable.fisioterapia_geriatrica)));
        listaCategorias.add(new Categoria("Fisioterapia Deportiva", R.drawable.icon7, new CategoriaDetalle(especialista, "Especialista en Alto Rendimiento", ubicacion, R.drawable.fisioterapia_deportiva)));
        listaCategorias.add(new Categoria("Fisioterapia Oncologica", R.drawable.icon6, new CategoriaDetalle(especialista, "Especialista en Rehabilitación Oncológica", ubicacion, R.drawable.fisioterapia_oncologica)));
        listaCategorias.add(new Categoria("Acupuntura", R.drawable.icon5, new CategoriaDetalle(especialista, "Especialista en Medicina Tradicional China", ubicacion, R.drawable.acupuntura)));
        listaCategorias.add(new Categoria("Entrenamiento Fisico", R.drawable.icon4, new CategoriaDetalle(especialista, "Entrenador Personal Certificado", ubicacion, R.drawable.entrenamiento_fisico)));
        listaCategorias.add(new Categoria("Osteopatia", R.drawable.icon3, new CategoriaDetalle(especialista, "Doctor en Osteopatía", ubicacion, R.drawable.osteopatia)));
        listaCategorias.add(new Categoria("Kinesiologia", R.drawable.icon2, new CategoriaDetalle(especialista, "Kinesiólogo Aplicado", ubicacion, R.drawable.kinesiologia)));
        listaCategorias.add(new Categoria("Rehabilitación", R.drawable.icon1, new CategoriaDetalle(especialista, "Centro de Rehabilitación Física", ubicacion, R.drawable.rehabilitacion)));
        listaCategorias.add(new Categoria("Fitoterapia", R.drawable.icon, new CategoriaDetalle(especialista, "Especialista en Herbolaria", ubicacion, R.drawable.fitoterapia)));
    }

    private void mostrarDialogoInformacion(CategoriaDetalle detalle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_info_categoria, null);
        builder.setView(view);

        TextView tvNombreEspecialista = view.findViewById(R.id.tvNombreEspecialista);
        TextView tvEspecialidad = view.findViewById(R.id.tvEspecialidad);
        TextView tvUbicaciones = view.findViewById(R.id.tvUbicaciones);
        Button btnVerGif = view.findViewById(R.id.btnVer);
        Button btnCerrar = view.findViewById(R.id.btnCerrar);

        tvNombreEspecialista.setText(detalle.getNombreEspecialista());
        tvEspecialidad.setText(detalle.getEspecialidad());
        tvUbicaciones.setText(detalle.getUbicaciones());

        final AlertDialog dialog = builder.create();

        btnVerGif.setOnClickListener(v -> {
            Intent intent = new Intent(CategoriasActivity.this, GifViewerActivity.class);
            intent.putExtra("GIF_RESOURCE_ID", detalle.getGifResourceId());
            startActivity(intent);
            dialog.dismiss(); // Cierra el diálogo de información
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
