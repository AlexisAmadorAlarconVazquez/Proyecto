package com.example.proyectotitulacion.Clasificacion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.proyectotitulacion.R;

public class GifViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_viewer);

        ImageView gifImageView = findViewById(R.id.gifImageView);
        Button btnClose = findViewById(R.id.btnCloseGif);

        int gifResourceId = getIntent().getIntExtra("GIF_RESOURCE_ID", -1);

        if (gifResourceId != -1) {
            Glide.with(this)
                    .asGif()
                    .load(gifResourceId)
                    .into(gifImageView);
        }

        btnClose.setOnClickListener(v -> {
            finish();
        });
    }
}