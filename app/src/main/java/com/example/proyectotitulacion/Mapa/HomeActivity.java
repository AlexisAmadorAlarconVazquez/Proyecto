package com.example.proyectotitulacion.Mapa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Import necesario para la vista
import androidx.viewpager2.widget.ViewPager2;
import java.util.Timer;
import java.util.TimerTask;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectotitulacion.ChatBot.ChatActivity;
import com.example.proyectotitulacion.Citas.MainActivity;
import com.example.proyectotitulacion.PerfilUsuario.PerfilActivity;
import com.example.proyectotitulacion.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private int[] images = {
            R.drawable.imagen1,
            R.drawable.imagen2,
            R.drawable.imagen3,
            R.drawable.imagen4
    };
    private int currentPage = 0;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Habilitar EdgeToEdge
        setContentView(R.layout.activity_home);

        // --- INICIALIZACIÓN DE COMPONENTES DE LA UI ---
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapsFragmentContainer, new MapsFragment())
                .commit();

        viewPager = findViewById(R.id.imageSlider);
        ImageAdapter adapter = new ImageAdapter(images);
        viewPager.setAdapter(adapter);

        // Carrusel automático
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Asegurarse de que las actualizaciones de la UI se ejecuten en el hilo principal
                if (viewPager != null) {
                    viewPager.post(() -> {
                        currentPage = (currentPage + 1) % images.length;
                        viewPager.setCurrentItem(currentPage, true);
                    });
                }
            }
        }, 3000, 3000); // 3 segundos

        // --- MANEJO DE WINDOW INSETS ---
        // Obtener la referencia al contenedor del contenido principal
        // ESTE ES EL CAMBIO IMPORTANTE: Usar R.id.main_content_container
        View mainContentContainer = findViewById(R.id.main_content_container);

        if (mainContentContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContentContainer, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                // Aplicar padding al contenedor del contenido para que no se solape con las barras del sistema
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                // Devolver los insets originales para que otros listeners (si los hubiera) puedan consumirlos.
                return windowInsets;
            });
        } else {
            // Considera loggear un error si main_content_container no se encuentra,
            // aunque con el layout XML correcto, esto no debería suceder.
            // Log.e("HomeActivity", "main_content_container no encontrado!");
        }


        // --- CONFIGURACIÓN DEL BOTTOMNAVIGATIONVIEW ---
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_home); // Marcar el ítem home como seleccionado

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Ya estamos en Home, no hacer nada o recargar si es necesario
                return true;
            } else if (id == R.id.nav_calendar) {
                navigateTo(MainActivity.class);
                return true;
            } else if (id == R.id.nav_chat) {
                navigateTo(ChatActivity.class);
                return true;
            } else if (id == R.id.nav_profile) {
                navigateTo(PerfilActivity.class);
                return true;
            }
            return false;
        });
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // Cierra la actividad actual
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null; // Buena práctica para liberar recursos
        }
    }
}