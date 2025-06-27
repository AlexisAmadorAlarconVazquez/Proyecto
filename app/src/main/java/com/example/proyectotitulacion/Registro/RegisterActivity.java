package com.example.proyectotitulacion.Registro;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyectotitulacion.Login.LoginActivity;
import com.example.proyectotitulacion.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etUsuario, etNombre, etFecha, etEmail, etPass, etConfirmPass;
    RadioGroup rgGenero;
    Button btnRegistrar;
    TextView tvIniciarSesion;

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
        tvIniciarSesion = findViewById(R.id.tvIniciarSesion);

        tvIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        etFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String fechaFormateada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, (month + 1), dayOfMonth);
                        etFecha.setText(fechaFormateada);
                    },
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        btnRegistrar.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String fecha = etFecha.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPass.getText().toString().trim();
            String confirm = etConfirmPass.getText().toString().trim();

// en caso de que no se cumpla con los campos mandar los siguientes mensajes
            if (usuario.isEmpty() || nombre.isEmpty() || fecha.isEmpty() ||
                    email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
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
            registrarUsuario(usuario, nombre, fecha, genero, email, password);
        });
    }
// LOGIN EXITOSO
    private void registrarUsuario(String usuario, String nombre, String fecha, String genero, String email, String password) {
        String url = "" +
                "http://192.168.137.1/WebService/registro.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    if (response.toLowerCase().contains("éxito") || response.toLowerCase().contains("exito")) {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                error -> {   /// Error de conexión con el servidor
                    Toast.makeText(this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("VolleyError", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("usuario", usuario);
                params.put("nombre_completo", nombre);
                params.put("fecha_nacimiento", fecha);
                params.put("genero", genero);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        queue.add(stringRequest);
    }
}
