package com.example.proyectotitulacion;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectotitulacion.R;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    EditText etUsuario, etNombre, etFecha, etEmail, etPass, etConfirmPass;
    Spinner spGenero;
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
        spGenero = findViewById(R.id.spGenero);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // Configurar Spinner posible cambio
        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
          //      R.array.generos_array, android.R.layout.simple_spinner_item);
       // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       // spGenero.setAdapter(adapter);

        // Selección de fecha
        etFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        etFecha.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    },
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Registro
        btnRegistrar.setOnClickListener(v -> {
            String pass = etPass.getText().toString();
            String confirm = etConfirmPass.getText().toString();

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                // Aquí puedes continuar con guardar en BD o pasar a otra actividad
            }
        });
    }
}
