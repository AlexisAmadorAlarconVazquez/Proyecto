package com.example.proyectotitulacion.Login;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.proyectotitulacion.Mapa.HomeActivity;
import com.example.proyectotitulacion.R;
import com.example.proyectotitulacion.Registro.RegisterActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;
    Button loginButton;
    TextView registerLink;
    private static final String LOGIN_URL = "http://192.168.137.1/WebService/login.php";
    private static final String TAG = "LoginActivity";
    public static final String PREFS_APP_NAME = "MyLoginAppPrefs";
    public static final String KEY_LAST_USED_USERNAME = "lastUsername";
    public static final String KEY_CURRENT_USER_IDENTIFIER = "CURRENT_USER_IDENTIFIER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_principal_main);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        loadLastUsername();

        loginButton.setOnClickListener(v -> {
            String usuarioEmail = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (usuarioEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                saveUsername(usuarioEmail); // Guardar el nombre de usuario
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
        String lastUsername = prefs.getString(KEY_LAST_USED_USERNAME, null);
        if (lastUsername != null) {
            usernameEditText.setText(lastUsername);
            Log.d(TAG, "Último usuario cargado: " + lastUsername);
        }
    }

    private void saveUsername(String username) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_APP_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_LAST_USED_USERNAME, username);
        editor.apply();
        Log.d(TAG, "Usuario guardado para la próxima vez: " + username);
    }

    private void loginUser(final String usuarioEmailInput, final String password) {
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

                            //Guarda el usuario actual
                            String currentUserIdentifier = "";
                            if (jsonResponse.has("userData")) {
                                JSONObject userData = jsonResponse.getJSONObject("userData");
                                if (userData.has("id")) {
                                    currentUserIdentifier = String.valueOf(userData.getInt("id"));
                                } else if (userData.has("usuario_o_email")) {
                                    currentUserIdentifier = userData.getString("usuario_o_email");
                                } else if (userData.has("email")) {
                                    currentUserIdentifier = userData.getString("email");
                                }
                            }

                            // Si el Usuario no se encontro entonces mandara un mensaje
                            if (currentUserIdentifier.isEmpty()) {
                                currentUserIdentifier = usuarioEmailInput;
                                Log.w(TAG, "No se encontró un identificador específico en la respuesta del login. Usando el input: " + currentUserIdentifier);
                            }


                            if (!currentUserIdentifier.isEmpty()) {
                                SharedPreferences prefs = getSharedPreferences(PREFS_APP_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(KEY_CURRENT_USER_IDENTIFIER, currentUserIdentifier);
                                editor.apply();
                                Log.i(TAG, "Identificador de usuario actual guardado: " + currentUserIdentifier);
                            } else {
                                Log.e(TAG, "¡ALERTA! No se pudo obtener/determinar un identificador de usuario para guardar en SharedPreferences.");

                            }
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
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
                parametros.put("usuario_o_email", usuarioEmailInput);
                parametros.put("password", password);
                return parametros;
            }
        };
        queue.add(stringRequest);
    }
}