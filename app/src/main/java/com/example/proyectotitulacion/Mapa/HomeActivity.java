package com.example.proyectotitulacion.Mapa;

import android.content.Intent;
import android.os.Bundle;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
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
                viewPager.post(() -> {
                    currentPage = (currentPage + 1) % images.length;
                    viewPager.setCurrentItem(currentPage, true);
                });
            }
        }, 3000, 3000); // 3 segundos


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_home);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_calendar) {
                Intent intent = new Intent(this, MainActivity.class);
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
        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (timer != null) {
                timer.cancel();
            }

    }
}