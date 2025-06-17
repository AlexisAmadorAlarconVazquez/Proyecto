package com.example.proyectotitulacion;

import android.content.Intent;
import android.content.SharedPreferences; // Necesario
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;
    Button loginButton;
    TextView registerLink;

    private static final String LOGIN_URL = "http://192.168.0.4/WebService/login.php";
    private static final String TAG = "LoginActivity";

    // --- Constantes para SharedPreferences (Solo para recordar usuario) ---
    private static final String PREFS_APP_NAME = "MyLoginAppPrefs"; // Nombre del archivo de preferencias
    private static final String KEY_LAST_USED_USERNAME = "lastUsername"; // Clave para el último usuario ingresado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_principal_main); // Asegúrate que este sea tu layout correcto

        usernameEditText = findViewById(R.id.username); // ID de tu EditText para usuario/email
        passwordEditText = findViewById(R.id.password); // ID de tu EditText para contraseña
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        // --- Cargar el último nombre de usuario guardado ---
        loadLastUsername();

        loginButton.setOnClickListener(v -> {
            String usuarioEmail = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (usuarioEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // --- Guardar el nombre de usuario actual antes de intentar el login ---
                saveUsername(usuarioEmail);
                loginUser(usuarioEmail, password);
            }
        });

        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loadLastUsername() {
        SharedPreferences prefs = getSharedPreferences(PREFS_APP_NAME, MODE_PRIVATE);
        String lastUsername = prefs.getString(KEY_LAST_USED_USERNAME, null); // null si no hay nada guardado
        if (lastUsername != null) {
            usernameEditText.setText(lastUsername);
            Log.d(TAG, "Último usuario cargado: " + lastUsername);
        }
    }

    private void saveUsername(String username) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_APP_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_LAST_USED_USERNAME, username);
        editor.apply(); // Guardar asíncronamente
        Log.d(TAG, "Usuario guardado para la próxima vez: " + username);
    }

    private void loginUser(final String usuarioEmail, final String password) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                response -> {
                    Log.d(TAG, "Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

                            // NO guardaremos estado de sesión aquí, solo el usuario ya se guardó
                            // NO redirigiremos automáticamente aquí

                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            // Opcional: pasar datos del usuario a HomeActivity si es necesario
                            // if (jsonResponse.has("userData")) {
                            //    JSONObject userData = jsonResponse.getJSONObject("userData");
                            //    String nombreUsuarioReal = userData.getString("usuario"); // Asumiendo que PHP lo devuelve
                            //    intent.putExtra("USERNAME_FROM_SERVER", nombreUsuarioReal);
                            // }
                            startActivity(intent);
                            finish();

                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing error: " + e.getMessage(), e);
                        Toast.makeText(LoginActivity.this, "Error al procesar la respuesta del servidor.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error: " + error.toString(), error);
                    Toast.makeText(LoginActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("usuario_o_email", usuarioEmail);
                parametros.put("password", password);
                return parametros;
            }
        };
        queue.add(stringRequest);
    }

    // Opcional: Si quieres borrar el nombre de usuario recordado en algún punto (ej. logout)
    // aunque en este escenario simplificado no estamos manejando un logout completo.
    /*
    private void clearSavedUsername() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_APP_NAME, MODE_PRIVATE).edit();
        editor.remove(KEY_LAST_USED_USERNAME);
        // O también podrías guardar una cadena vacía:
        // editor.putString(KEY_LAST_USED_USERNAME, "");
        editor.apply();
        Log.d(TAG, "Nombre de usuario recordado borrado.");
    }
    */
}