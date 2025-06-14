package com.example.proyectotitulacion;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;

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

            new Thread(() -> {
                try {
                    URL url = new URL("http://192.168.1.104/WebService/registro.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    String parametros = "usuario=" + URLEncoder.encode(usuario, StandardCharsets.UTF_8) +
                            "&nombre=" + URLEncoder.encode(nombre, StandardCharsets.UTF_8) +
                            "&fecha=" + URLEncoder.encode(fecha, StandardCharsets.UTF_8) +
                            "&genero=" + URLEncoder.encode(genero, StandardCharsets.UTF_8) +
                            "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
                            "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
                    writer.write(parametros);
                    writer.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    String respuesta = reader.readLine();

                    runOnUiThread(() -> {
                        if (respuesta != null) {
                            Toast.makeText(this, respuesta, Toast.LENGTH_LONG).show();
                            if (respuesta.toLowerCase().contains("éxito") || respuesta.toLowerCase().contains("exito")) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(this, "Error al recibir respuesta", Toast.LENGTH_SHORT).show();
                        }
                    });

                    writer.close();
                    reader.close();
                } catch (Exception e) {
                    Log.e("RegisterActivity", "Error en el registro", e);
                    runOnUiThread(() -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }
}
