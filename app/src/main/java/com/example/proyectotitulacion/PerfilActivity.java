package com.example.proyectotitulacion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PerfilActivity extends AppCompatActivity {

    private static final String TAG = "PerfilActivity";
    private static final String GET_PROFILE_URL = "http://192.168.1.104/WebService/get_profile_data.php";
    private static final String UPDATE_PROFILE_URL = "http://192.168.1.104/WebService/update_profile.php";
    private static final String DELETE_PROFILE_URL = "http://192.168.1.104/WebService/delete_profile.php";
    @SuppressWarnings("FieldCanBeLocal")
    private TextView tvUsuario;
    @SuppressWarnings("FieldCanBeLocal")
    private TextView tvNombre;
    @SuppressWarnings("FieldCanBeLocal")
    private TextView tvEmail;
    private TextView tvFechaNacimiento;
    private TextView tvGenero;
    private EditText etUsuario;
    private EditText etNombre;
    private EditText etEmail;
    private EditText etFechaNacimiento;
    private EditText etGenero;

    private ImageButton btnEditarPerfil;
    private LinearLayout llBotonesEdicion;
    // btnGuardarCambios, btnCancelarCambios, btnBorrarPerfil se convertirán en locales en onCreate
    private Button btnCerrarSesion;
    private ProgressBar progressBarPerfil;

    private RequestQueue requestQueue;
    private String currentUserIdentifier;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        tvUsuario = findViewById(R.id.tvUsuario);
        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvFechaNacimiento = findViewById(R.id.tvFechaNacimiento);
        tvGenero = findViewById(R.id.tvGenero);

        etUsuario = findViewById(R.id.etUsuario);
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etGenero = findViewById(R.id.etGenero);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        llBotonesEdicion = findViewById(R.id.llBotonesEdicion);

        // Declaración local para estos botones
        Button btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        Button btnCancelarCambios = findViewById(R.id.btnCancelarCambios);
        Button btnBorrarPerfil = findViewById(R.id.btnBorrarPerfil);

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        progressBarPerfil = findViewById(R.id.progressBarPerfil);

        requestQueue = Volley.newRequestQueue(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

//noinspection SimplifiableIfStatement
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_calendar) {
                Toast.makeText(PerfilActivity.this, getString(R.string.calendar_selected_toast), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_chat) {
                Toast.makeText(PerfilActivity.this, getString(R.string.chat_selected_toast), Toast.LENGTH_SHORT).show();
                return true;
            } else return itemId == R.id.nav_profile;
        });

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_APP_NAME, MODE_PRIVATE);
        currentUserIdentifier = prefs.getString(LoginActivity.KEY_CURRENT_USER_IDENTIFIER, null);

        //noinspection SimplifiableIfStatement
        if (currentUserIdentifier == null || currentUserIdentifier.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_missing_user_identifier), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        loadUserProfile();

        btnEditarPerfil.setOnClickListener(v -> toggleEditMode());

        btnCancelarCambios.setOnClickListener(v -> { // Usa la variable local
            toggleEditMode();
            loadUserProfile();
        });

        btnGuardarCambios.setOnClickListener(v -> saveUserProfileChanges()); // Usa la variable local
        btnBorrarPerfil.setOnClickListener(v -> mostrarDialogoConfirmacionBorrado()); // Usa la variable local
        btnCerrarSesion.setOnClickListener(v -> mostrarDialogoConfirmacionCerrarSesion());

        setEditMode(false);
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        setEditMode(isEditMode);
    }

    private void setEditMode(boolean enable) {
        isEditMode = enable;
        if (enable) {
            btnEditarPerfil.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            tvUsuario.setVisibility(View.GONE);
            tvNombre.setVisibility(View.GONE);
            tvEmail.setVisibility(View.GONE);
            tvFechaNacimiento.setVisibility(View.GONE);
            tvGenero.setVisibility(View.GONE);

            etUsuario.setText(tvUsuario.getText());
            etNombre.setText(tvNombre.getText());
            etEmail.setText(tvEmail.getText());
            etFechaNacimiento.setText(tvFechaNacimiento.getText());
            etGenero.setText(tvGenero.getText());

            etUsuario.setVisibility(View.VISIBLE);
            etNombre.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.VISIBLE);
            etFechaNacimiento.setVisibility(View.VISIBLE);
            etGenero.setVisibility(View.VISIBLE);

            etUsuario.setEnabled(false);
            etEmail.setEnabled(false);

            llBotonesEdicion.setVisibility(View.VISIBLE);
            btnCerrarSesion.setVisibility(View.GONE);
        } else {
            btnEditarPerfil.setImageResource(android.R.drawable.ic_menu_edit);
            tvUsuario.setVisibility(View.VISIBLE);
            tvNombre.setVisibility(View.VISIBLE);
            tvEmail.setVisibility(View.VISIBLE);
            tvFechaNacimiento.setVisibility(View.VISIBLE);
            tvGenero.setVisibility(View.VISIBLE);

            etUsuario.setVisibility(View.GONE);
            etNombre.setVisibility(View.GONE);
            etEmail.setVisibility(View.GONE);
            etFechaNacimiento.setVisibility(View.GONE);
            etGenero.setVisibility(View.GONE);

            llBotonesEdicion.setVisibility(View.GONE);
            btnCerrarSesion.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserProfile() {
        progressBarPerfil.setVisibility(View.VISIBLE);
        if (isEditMode) {
            setEditMode(false);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_PROFILE_URL,
                response -> {
                    progressBarPerfil.setVisibility(View.GONE);
                    Log.d(TAG, "Profile Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            JSONObject userData = jsonResponse.getJSONObject("userData");
                            tvUsuario.setText(userData.optString("usuario", getString(R.string.not_available_placeholder)));
                            tvNombre.setText(userData.optString("nombre_completo", getString(R.string.not_available_placeholder)));
                            tvEmail.setText(userData.optString("email", getString(R.string.not_available_placeholder)));
                            tvFechaNacimiento.setText(userData.optString("fecha_nacimiento", getString(R.string.not_available_placeholder)));
                            tvGenero.setText(userData.optString("genero", getString(R.string.not_available_placeholder)));
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(PerfilActivity.this, getString(R.string.server_error_prefix) + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing error: " + e.getMessage(), e);
                        Toast.makeText(PerfilActivity.this, getString(R.string.error_json_parsing), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBarPerfil.setVisibility(View.GONE);
                    Log.e(TAG, "Volley Error: " + error.toString(), error);
                    Toast.makeText(PerfilActivity.this, getString(R.string.error_network_connection), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (currentUserIdentifier != null) {
                    params.put("identificador_usuario", currentUserIdentifier);
                }
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void saveUserProfileChanges() {
        progressBarPerfil.setVisibility(View.VISIBLE);
        final String nombre = etNombre.getText().toString().trim();
        final String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        final String genero = etGenero.getText().toString().trim();

        if (nombre.isEmpty() || fechaNacimiento.isEmpty() || genero.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields_error), Toast.LENGTH_SHORT).show();
            progressBarPerfil.setVisibility(View.GONE);
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_PROFILE_URL,
                response -> {
                    progressBarPerfil.setVisibility(View.GONE);
                    Log.d(TAG, "Update Profile Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(PerfilActivity.this, message, Toast.LENGTH_SHORT).show();
                            setEditMode(false);
                            loadUserProfile();
                        } else {
                            Toast.makeText(PerfilActivity.this, getString(R.string.profile_update_error_prefix) + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing error en update: " + e.getMessage(), e);
                        Toast.makeText(PerfilActivity.this, getString(R.string.error_processing_save_response), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBarPerfil.setVisibility(View.GONE);
                    Log.e(TAG, "Volley Error en update: " + error.toString(), error);
                    Toast.makeText(PerfilActivity.this, getString(R.string.error_network_connection_save), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (currentUserIdentifier != null) {
                    params.put("identificador_usuario", currentUserIdentifier);
                }
                params.put("nombre_completo", nombre);
                params.put("fecha_nacimiento", fechaNacimiento);
                params.put("genero", genero);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void mostrarDialogoConfirmacionBorrado() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_profile_title))
                .setMessage(getString(R.string.confirm_delete_profile_message))
                .setPositiveButton(getString(R.string.delete_button_confirm), (dialog, which) -> procederConBorradoDePerfil())
                .setNegativeButton(getString(R.string.cancel_button_text), (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void procederConBorradoDePerfil() {
        progressBarPerfil.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_PROFILE_URL,
                response -> {
                    progressBarPerfil.setVisibility(View.GONE);
                    Log.d(TAG, "Delete Profile Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(PerfilActivity.this, message, Toast.LENGTH_LONG).show();

                            SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_APP_NAME, MODE_PRIVATE);
                            prefs.edit().remove(LoginActivity.KEY_CURRENT_USER_IDENTIFIER).apply();

                            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(PerfilActivity.this, getString(R.string.profile_delete_error_prefix) + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing error en delete: " + e.getMessage(), e);
                        Toast.makeText(PerfilActivity.this, getString(R.string.error_processing_delete_response), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBarPerfil.setVisibility(View.GONE);
                    Log.e(TAG, "Volley Error en delete: " + error.toString(), error);
                    Toast.makeText(PerfilActivity.this, getString(R.string.error_network_connection_delete), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (currentUserIdentifier != null) {
                    params.put("identificador_usuario", currentUserIdentifier);
                }
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void mostrarDialogoConfirmacionCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_logout_title))
                .setMessage(getString(R.string.confirm_logout_message))
                .setPositiveButton(getString(R.string.logout_button_confirm), (dialog, which) -> procederConCierreDeSesion())
                .setNegativeButton(getString(R.string.logout_button_cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void procederConCierreDeSesion() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_APP_NAME, MODE_PRIVATE);
        prefs.edit().remove(LoginActivity.KEY_CURRENT_USER_IDENTIFIER).apply();

        Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, getString(R.string.logout_success_message), Toast.LENGTH_SHORT).show();
    }
}