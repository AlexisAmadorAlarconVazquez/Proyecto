package com.example.proyectotitulacion;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView tvFechaSeleccionada, tvResultadoCita;
    Button btnSeleccionarFecha, btnEnviarCita;
    Spinner spinnerHorario;
    String fechaFinal;
    String horarioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        tvResultadoCita = findViewById(R.id.tvResultadoCita);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        btnEnviarCita = findViewById(R.id.btnEnviarCita);
        spinnerHorario = findViewById(R.id.spinnerHorario);

        // Adaptador de horarios
        String[] horarios = {"08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, horarios);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHorario.setAdapter(adapter);

        btnSeleccionarFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar seleccion = Calendar.getInstance();
                seleccion.set(year, month, dayOfMonth);
                SimpleDateFormat formato = new SimpleDateFormat("EEEE dd/MMMM/yyyy", new Locale("es", "ES"));
                fechaFinal = formato.format(seleccion.getTime());
                tvFechaSeleccionada.setText(fechaFinal);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        btnEnviarCita.setOnClickListener(v -> {
            horarioSeleccionado = spinnerHorario.getSelectedItem().toString();
            if (fechaFinal != null) {
                String resultado = "Cita agendada para:\n" + fechaFinal + " a las " + horarioSeleccionado;
                tvResultadoCita.setText(resultado);
            } else {
                Toast.makeText(this, "Por favor selecciona una fecha", Toast.LENGTH_SHORT).show();
            }
        });

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_calendar); // Resalta el ítem actual

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) {
                // Ya estás en esta pantalla
                return true;
            } else if (id == R.id.nav_chat) {
                Intent intent = new Intent(this, ChatActivity.class);
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
