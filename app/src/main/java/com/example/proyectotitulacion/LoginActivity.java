package com.example.proyectotitulacion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

// IMPORTACIÓN NECESARIA PARA JSON
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText; // Mantienes tus nombres de variables
    Button loginButton;
    TextView registerLink; // Mantienes tu variable para el link de registro

    // Asegúrate de que esta IP y ruta sean correctas y coincidan con tu servidor XAMPP
    private static final String LOGIN_URL = "http://192.168.1.104/WebService/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate que este layout sea el correcto y contenga los IDs usados
        setContentView(R.layout.login_principal_main);

        usernameEditText = findViewById(R.id.username); // ID de tu EditText para usuario/email
        passwordEditText = findViewById(R.id.password); // ID de tu EditText para contraseña
        loginButton = findViewById(R.id.loginButton);   // ID de tu botón de login
        registerLink = findViewById(R.id.registerLink); // ID de tu TextView para ir a registro

        loginButton.setOnClickListener(v -> {
            String usuarioEmail = usernameEditText.getText().toString().trim(); // Cambié el nombre de la variable para mayor claridad
            String password = passwordEditText.getText().toString().trim();

            if (usuarioEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(usuarioEmail, password);
            }
        });

        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(final String usuarioEmail, final String password) { // 'usuarioEmail' en lugar de 'username'
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                response -> {
                    Log.d("LoginActivity", "Response: " + response); // Muy útil para depurar
                    try {
                        // ---- INICIO DEL PARSEO DE JSON ----
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status"); // Obtener el campo 'status' del JSON

                        if ("success".equals(status)) {
                            // Login exitoso
                            String message = jsonResponse.getString("message"); // Obtener mensaje de éxito
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

                            // Opcional: Obtener datos del usuario si los enviaste desde PHP
                            // if (jsonResponse.has("userData")) {
                            //     JSONObject userData = jsonResponse.getJSONObject("userData");
                            //     String nombreUsuario = userData.getString("usuario");
                            //     // Podrías guardar nombreUsuario en SharedPreferences o pasarlo a HomeActivity
                            // }

                            // Navegar a HomeActivity
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            // Opcional: pasar datos a HomeActivity
                            // intent.putExtra("NOMBRE_USUARIO", nombreUsuario); // Si obtuviste nombreUsuario
                            startActivity(intent);
                            finish(); // Finalizar LoginActivity para que no se pueda volver con el botón "atrás"

                        } else {
                            // Login fallido u otro error del servidor
                            String message = jsonResponse.getString("message"); // Obtener mensaje de error
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                        // ---- FIN DEL PARSEO DE JSON ----
// ...
                    } catch (JSONException e) {
                        // Error al parsear el JSON (respuesta inesperada del servidor)
                        // e.printStackTrace(); // <--- PUEDES COMENTAR O ELIMINAR ESTA LÍNEA

                        // Usa Log.e() pasando la excepción como el tercer argumento
                        // para que el stack trace completo se imprima en Logcat bajo tu TAG.
                        Log.e("LoginActivity", "JSON Parsing error: " + e.getMessage(), e);

                        Toast.makeText(LoginActivity.this, "Error al procesar la respuesta del servidor.", Toast.LENGTH_SHORT).show();
                    }
// ...
                },
                error -> {
                    // Error de Volley (conexión, timeout, etc.)
                    Log.e("LoginActivity", "Volley Error: " + error.toString());
                    Toast.makeText(LoginActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parametros = new HashMap<>();
                // ---- CAMBIO EN EL NOMBRE DEL PARÁMETRO ----
                // El script PHP mejorado espera "usuario_o_email"
                parametros.put("usuario_o_email", usuarioEmail);
                parametros.put("password", password);
                return parametros;
            }
        };

        queue.add(stringRequest);
    }
}