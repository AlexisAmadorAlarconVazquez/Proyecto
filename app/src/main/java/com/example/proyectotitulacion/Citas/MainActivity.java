package com.example.proyectotitulacion.Citas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup; // Necesario para MarginLayoutParams
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectotitulacion.ChatBot.ChatActivity;
import com.example.proyectotitulacion.Mapa.HomeActivity;
import com.example.proyectotitulacion.Login.LoginActivity;
import com.example.proyectotitulacion.PerfilUsuario.PerfilActivity;
import com.example.proyectotitulacion.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView tvFechaSeleccionada, tvResultadoCita, tvTitulo; // Añadido tvTitulo aquí
    Button btnSeleccionarFecha, btnEnviarCita, btnConsultarCitas;
    Spinner spinnerHorario;
    ListView listaCitas;
    BottomNavigationView bottomNavigationView;

    ArrayList<String> listaDatos;
    ArrayList<Integer> listaIds;
    CitaAdapter adapter;

    DMLCitas dmlCitas;

    String fechaMostrada, fechaMySQL, horarioSeleccionado;
    int idUsuario = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilitar Edge-to-Edge para que la UI se dibuje detrás de las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        // --- INICIALIZACIÓN DE VISTAS ---
        tvTitulo = findViewById(R.id.tvTitulo); // Inicializar tvTitulo
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        tvResultadoCita = findViewById(R.id.tvResultadoCita);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        btnEnviarCita = findViewById(R.id.btnEnviarCita);
        btnConsultarCitas = findViewById(R.id.btnConsultarCitas);
        spinnerHorario = findViewById(R.id.spinnerHorario);
        listaCitas = findViewById(R.id.listaCitas);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // --- MANEJO DE WINDOW INSETS ---

        // Aplicar Insets al TextView del título para que no se solape con la status bar
        if (tvTitulo != null) {
            final int originalTopMargin = ((ViewGroup.MarginLayoutParams) tvTitulo.getLayoutParams()).topMargin;
            ViewCompat.setOnApplyWindowInsetsListener(tvTitulo, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                params.topMargin = originalTopMargin + systemBars.top;
                v.setLayoutParams(params);
                // No consumir insets aquí si otras vistas los necesitan individualmente.
                // Devolver los insets originales permite que otros listeners también los procesen.
                return windowInsets;
            });
        }

        // Aplicar Insets al contenedor principal scrolleable
        View contentContainer = findViewById(R.id.citas_content_scrollview);
        if (contentContainer != null) {
            final int originalPaddingLeft = contentContainer.getPaddingLeft();
            final int originalPaddingTop = contentContainer.getPaddingTop();
            final int originalPaddingRight = contentContainer.getPaddingRight();
            final int originalPaddingBottom = contentContainer.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(contentContainer, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

                // Solo aplicar padding para la barra de navegación inferior y el IME en el contenedor scrolleable.
                // El padding superior (status bar) ya se manejó para el tvTitulo.
                // El padding lateral (para display cutouts en landscape, etc.) se puede añadir aquí si es necesario.
                v.setPadding(
                        originalPaddingLeft + systemBars.left,
                        originalPaddingTop, // El padding superior del scroll no necesita cambiar si el título lo maneja
                        originalPaddingRight + systemBars.right,
                        originalPaddingBottom + Math.max(systemBars.bottom, imeInsets.bottom)
                );
                return WindowInsetsCompat.CONSUMED; // Consumir los insets
            });
        }


        // --- LÓGICA DE LA ACTIVIDAD ---
        dmlCitas = new DMLCitas(this);
        idUsuario = obtenerIdUsuarioGuardado();

        if (idUsuario <= 0) {
            Toast.makeText(this, "Usuario no identificado. Por favor, inicie sesión.", Toast.LENGTH_LONG).show();
        }

        String[] horarios = {"08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, horarios);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHorario.setAdapter(spinnerAdapter);

        listaDatos = new ArrayList<>();
        listaIds = new ArrayList<>();
        adapter = new CitaAdapter(this, listaDatos, listaIds, this);
        listaCitas.setAdapter(adapter);

        btnSeleccionarFecha.setOnClickListener(v -> mostrarDatePicker());
        btnEnviarCita.setOnClickListener(v -> enviarCita());
        btnConsultarCitas.setOnClickListener(v -> {
            if (idUsuario > 0) {
                cargarCitas(idUsuario);
            } else {
                Toast.makeText(this, "Usuario no identificado para consultar citas.", Toast.LENGTH_SHORT).show();
            }
        });

        configurarBottomNavigation();
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar seleccion = Calendar.getInstance();
            seleccion.set(year, month, dayOfMonth);

            SimpleDateFormat formatoMostrar = new SimpleDateFormat("EEEE dd/MMMM/yyyy", new Locale("es", "ES"));
            fechaMostrada = formatoMostrar.format(seleccion.getTime());
            tvFechaSeleccionada.setText(fechaMostrada);

            SimpleDateFormat formatoMySQL = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            fechaMySQL = formatoMySQL.format(seleccion.getTime());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void enviarCita() {
        horarioSeleccionado = spinnerHorario.getSelectedItem().toString();
        if (fechaMySQL != null && !fechaMySQL.isEmpty() && idUsuario > 0) {
            String horaMySQL = convertirHoraMySQL(horarioSeleccionado);
            String resultado = "Cita agendada para:\n" + fechaMostrada + " a las " + horarioSeleccionado;
            tvResultadoCita.setText(resultado);
            dmlCitas.insertarCita(this, idUsuario, fechaMySQL, horaMySQL);
            // Considera limpiar campos o recargar citas aquí si es necesario
        } else if (idUsuario <= 0) {
            Toast.makeText(this, "Usuario no identificado. No se puede agendar la cita.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Por favor, selecciona una fecha válida.", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    navigateTo(HomeActivity.class);
                    return true;
                } else if (itemId == R.id.nav_calendar) {
                    return true; // Ya estamos aquí
                } else if (itemId == R.id.nav_chat) {
                    navigateTo(ChatActivity.class);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    navigateTo(PerfilActivity.class);
                    return true;
                }
                return false;
            });
        }
    }

    private void navigateTo(Class<?> activityClass) {
        if (!this.getClass().equals(activityClass)) {
            Intent intent = new Intent(this, activityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            // No llamar a finish() para navegación tipo "tab"
        }
    }

    private int obtenerIdUsuarioGuardado() {
        try {
            String idStr = getSharedPreferences(LoginActivity.PREFS_APP_NAME, MODE_PRIVATE)
                    .getString(LoginActivity.KEY_CURRENT_USER_IDENTIFIER, null);
            if (idStr != null) {
                return Integer.parseInt(idStr);
            }
        } catch (NumberFormatException e) {
            Log.e("MainActivity", "Error al convertir idUsuario a entero: " + e.getMessage());
        }
        return 0; // Retorna 0 o un valor que indique "no identificado"
    }

    private void cargarCitas(int idUsuario) {
        listaDatos.clear();
        listaIds.clear();
        HelperCitas helper = new HelperCitas(this);
        Cursor cursor = null; // Inicializar a null

        try {
            cursor = helper.consultarCitasUsuario(idUsuario);
            if (cursor == null) {
                Toast.makeText(this, "Error al consultar citas.", Toast.LENGTH_SHORT).show();
                return; // Salir temprano si el cursor es null
            }
            if (cursor.getCount() == 0) {
                Toast.makeText(this, "No tienes citas registradas.", Toast.LENGTH_SHORT).show();
                // No es necesario hacer nada más aquí, el adapter.notifyDataSetChanged() en finally limpiará la lista.
            } else {
                // Mapeo de nombres de columna a índices para eficiencia
                int columnId = cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_ID);
                int columnFecha = cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_FECHA);
                int columnHora = cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_HORA);
                int columnEstado = cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_ESTADO);
                int columnMotivo = cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_MOTIVO);

                while (cursor.moveToNext()) {
                    int idCita = cursor.getInt(columnId);
                    String fecha = cursor.getString(columnFecha);
                    String hora = cursor.getString(columnHora);
                    String estado = cursor.getString(columnEstado);
                    String motivo = cursor.getString(columnMotivo); // getString puede devolver null

                    String fechaFormateada = formatearFechaVisible(fecha);
                    String horaFormateada = convertirHoraAMPM(hora);

                    String texto = "Cita ID: " + idCita + "\nFecha: " + fechaFormateada + "\nHora: " + horaFormateada + "\nEstado: " + estado;
                    if (("Cancelado".equalsIgnoreCase(estado) || "Reprogramado".equalsIgnoreCase(estado)) && motivo != null && !motivo.isEmpty()) {
                        texto += "\nMotivo: " + motivo;
                    }
                    listaDatos.add(texto);
                    listaIds.add(idCita);
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e("MainActivity", "Error al leer datos del cursor: " + e.getMessage());
            Toast.makeText(this, "Error al procesar los datos de las citas.", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            adapter.notifyDataSetChanged(); // Asegurar que la UI se actualice siempre
        }
    }

    private String formatearFechaVisible(String fechaMySQL) {
        if (fechaMySQL == null) return "Fecha no disponible";
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat formatoSalida = new SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            return formatoSalida.format(formatoEntrada.parse(fechaMySQL));
        } catch (ParseException e) {
            Log.e("MainActivity", "Error al formatear fecha: " + fechaMySQL, e);
            return fechaMySQL; // Devuelve la original si hay error
        }
    }

    public void abrirDialogoEditar(int idCita) {
        HelperCitas helper = new HelperCitas(this);
        Cursor cursor = null;

        try {
            cursor = helper.consultarCitaPorId(idCita);
            if (cursor != null && cursor.moveToFirst()) {
                String fechaActual = cursor.getString(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_FECHA));
                String horaActual = cursor.getString(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_HORA));
                String motivoActual = "";
                int motivoColumnIndex = cursor.getColumnIndex(HelperCitas.COLUMN_MOTIVO);
                if (motivoColumnIndex != -1 && !cursor.isNull(motivoColumnIndex)) {
                    motivoActual = cursor.getString(motivoColumnIndex);
                }
                // Es importante cerrar el cursor aquí si ya no se va a usar,
                // especialmente antes de mostrar un diálogo que podría tomar tiempo.
                cursor.close();
                cursor = null; // Marcar como cerrado

                // Inflar vista del diálogo
                View dialogView = getLayoutInflater().inflate(R.layout.dialogo_editar_cita, null);
                EditText etFecha = dialogView.findViewById(R.id.etFechaEditar);
                Spinner spinnerHoraEditar = dialogView.findViewById(R.id.spinnerHoraEditar);
                EditText etMotivo = dialogView.findViewById(R.id.etMotivoEditar);

                etFecha.setText(fechaActual);
                etMotivo.setText(motivoActual != null ? motivoActual : "");

                // Configurar Spinner de Hora
                String[] horarios = {"08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM"};
                ArrayAdapter<String> adapterHora = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, horarios);
                adapterHora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerHoraEditar.setAdapter(adapterHora);
                String horaAMPM = convertirHoraAMPM(horaActual);
                for (int i = 0; i < horarios.length; i++) {
                    if (horarios[i].equalsIgnoreCase(horaAMPM)) {
                        spinnerHoraEditar.setSelection(i);
                        break;
                    }
                }

                final String[] fechaSeleccionadaParaDialogo = {fechaActual};
                etFecha.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        if (fechaSeleccionadaParaDialogo[0] != null && !fechaSeleccionadaParaDialogo[0].isEmpty()) {
                            calendar.setTime(sdf.parse(fechaSeleccionadaParaDialogo[0]));
                        }
                    } catch (ParseException e) {
                        Log.e("MainActivity", "Error al parsear fecha para DatePicker: " + fechaSeleccionadaParaDialogo[0], e);
                    }
                    DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, day) -> {
                        Calendar sel = Calendar.getInstance();
                        sel.set(year, month, day);
                        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        fechaSeleccionadaParaDialogo[0] = formato.format(sel.getTime());
                        etFecha.setText(fechaSeleccionadaParaDialogo[0]);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    dpd.show();
                });

                // Construir y mostrar el diálogo
                new AlertDialog.Builder(this)
                        .setTitle("Editar Cita")
                        .setView(dialogView)
                        .setPositiveButton("Actualizar", (dialog, which) -> {
                            String nuevaFecha = etFecha.getText().toString();
                            String nuevaHoraSeleccionada = spinnerHoraEditar.getSelectedItem().toString();
                            String nuevaHoraMySQL = convertirHoraMySQL(nuevaHoraSeleccionada);
                            String nuevoMotivo = etMotivo.getText().toString().trim();

                            if (idUsuario <= 0) {
                                Toast.makeText(this, "Usuario no identificado.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (nuevaFecha.isEmpty() || nuevaHoraMySQL.isEmpty()) {
                                Toast.makeText(this, "La fecha y hora son obligatorias.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (nuevoMotivo.isEmpty()) {
                                Toast.makeText(this, "Debe ingresar un motivo para reprogramar.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            dmlCitas.actualizarCita(this, idCita, idUsuario, nuevaFecha, nuevaHoraMySQL, "Reprogramado", nuevoMotivo);
                            cargarCitas(idUsuario);
                        })
                        .setNeutralButton("Cancelar Cita", (dialog, which) -> mostrarDialogoCancelar(idCita))
                        .setNegativeButton("Cerrar", null)
                        .show();

            } else {
                Toast.makeText(this, "No se pudo encontrar la cita para editar.", Toast.LENGTH_SHORT).show();
            }
        } catch (IllegalArgumentException e) {
            Log.e("MainActivity", "Error al preparar diálogo de edición: " + e.getMessage());
            Toast.makeText(this, "Error al cargar datos de la cita para editar.", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public void mostrarDialogoCancelar(int idCita) {
        View view = getLayoutInflater().inflate(R.layout.dialogo_cancelar_cita, null);
        EditText etMotivoCancelar = view.findViewById(R.id.etMotivoCancelar);

        new AlertDialog.Builder(this)
                .setTitle("Cancelar Cita")
                .setView(view) // Añadir el EditText para el motivo
                .setMessage("¿Estás seguro de que deseas cancelar esta cita? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Cancelar", (dialog, which) -> {
                    String motivo = etMotivoCancelar.getText().toString().trim();
                    if (motivo.isEmpty()) {
                        Toast.makeText(this, "Debes ingresar un motivo para la cancelación.", Toast.LENGTH_SHORT).show();
                        // Considera reabrir el diálogo o evitar que se cierre si el motivo es obligatorio.
                        // Para evitarlo, se necesitaría un manejo más avanzado del botón del diálogo.
                        return; // No proceder sin motivo
                    }
                    if (idUsuario > 0) {
                        dmlCitas.cancelarCita(this, idCita, motivo);
                        cargarCitas(idUsuario);
                    } else {
                        Toast.makeText(this, "Usuario no identificado. No se puede cancelar.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private String convertirHoraMySQL(String hora12h) {
        if (hora12h == null) return "00:00:00";
        try {
            SimpleDateFormat formato12h = new SimpleDateFormat("hh:mm a", Locale.US); // "a" para AM/PM
            SimpleDateFormat formato24h = new SimpleDateFormat("HH:mm:ss", Locale.US);
            return formato24h.format(formato12h.parse(hora12h));
        } catch (ParseException e) {
            Log.e("MainActivity", "Error al convertir hora a formato MySQL: " + hora12h, e);
            return "00:00:00"; // Valor por defecto en caso de error
        }
    }

    private String convertirHoraAMPM(String hora24h) {
        if (hora24h == null) return "12:00 AM"; // Valor por defecto o manejo de error
        if (hora24h.matches(".*[AP]M.*")) { // Ya está en formato AM/PM
            return hora24h;
        }
        try {
            // Intenta primero con formato HH:mm:ss
            SimpleDateFormat formato24h_completo = new SimpleDateFormat("HH:mm:ss", Locale.US);
            SimpleDateFormat formato12h = new SimpleDateFormat("hh:mm a", Locale.US); // "a" para AM/PM
            return formato12h.format(formato24h_completo.parse(hora24h));
        } catch (ParseException e1) {
            // Si falla, intenta con formato HH:mm
            try {
                SimpleDateFormat formato24h_corto = new SimpleDateFormat("HH:mm", Locale.US);
                SimpleDateFormat formato12h = new SimpleDateFormat("hh:mm a", Locale.US);
                return formato12h.format(formato24h_corto.parse(hora24h));
            } catch (ParseException e2) {
                Log.e("MainActivity", "Error al convertir hora a formato AM/PM (intentos con HH:mm:ss y HH:mm fallidos): " + hora24h, e2);
                return hora24h; // Devuelve la original si ambos formatos fallan
            }
        }
    }
}