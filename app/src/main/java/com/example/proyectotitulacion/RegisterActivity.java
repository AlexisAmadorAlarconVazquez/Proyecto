package com.example.proyectotitulacion;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    EditText etUsuario, etNombre, etFecha, etEmail, etPass, etConfirmPass;
    RadioGroup rgGenero;
    Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsuario = findViewById(R.id.etUsuario);
        etNombre = findViewById(R.id.etNombre);
        etFecha = findViewById(R.id.etFechaNacimiento);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        etConfirmPass = findViewById(R.id.etConfirmarPassword);
        rgGenero = findViewById(R.id.rgGenero);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // Selección de fecha
// Selección de fecha
        etFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String fechaFormateada = getString(R.string.fecha_formateada, dayOfMonth, (month + 1), year);
                        etFecha.setText(fechaFormateada);
                    },
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });


        // Registro
        btnRegistrar.setOnClickListener(v -> {
            String pass = etPass.getText().toString();
            String confirm = etConfirmPass.getText().toString();

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
            }
            int selectedId = rgGenero.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Selecciona un género", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rbSeleccionado = findViewById(selectedId);
            String genero = rbSeleccionado.getText().toString();

            Toast.makeText(this, "Registro exitoso\nGénero: " + genero, Toast.LENGTH_SHORT).show();

            // Aquí podrías continuar con guardar en BD o pasar a otra actividad
        });
    }
}
