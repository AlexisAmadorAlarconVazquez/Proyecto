package com.example.proyectotitulacion;

import android.util.Log;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView tvFechaSeleccionada, tvResultadoCita;
    Button btnSeleccionarFecha, btnEnviarCita, btnConsultarCitas;
    Spinner spinnerHorario;
    ListView listaCitas;

    ArrayList<String> listaDatos;
    ArrayList<Integer> listaIds;
    CitaAdapter adapter;

    DMLCitas dmlCitas;

    String fechaMostrada, fechaMySQL, horarioSeleccionado;
    int idUsuario = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        tvResultadoCita = findViewById(R.id.tvResultadoCita);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        btnEnviarCita = findViewById(R.id.btnEnviarCita);
        btnConsultarCitas = findViewById(R.id.btnConsultarCitas);
        spinnerHorario = findViewById(R.id.spinnerHorario);
        listaCitas = findViewById(R.id.listaCitas);

        dmlCitas = new DMLCitas(this);
        idUsuario = obtenerIdUsuarioGuardado();

        if (idUsuario <= 0) {
            Toast.makeText(this, "Usuario no identificado", Toast.LENGTH_LONG).show();
        }

        String[] horarios = {"08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, horarios);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHorario.setAdapter(spinnerAdapter);

        listaDatos = new ArrayList<>();
        listaIds = new ArrayList<>();
        adapter = new CitaAdapter(this, listaDatos, listaIds, this);
        listaCitas.setAdapter(adapter);

        btnSeleccionarFecha.setOnClickListener(v -> {
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
        });

        btnEnviarCita.setOnClickListener(v -> {
            horarioSeleccionado = spinnerHorario.getSelectedItem().toString();
            if (fechaMySQL != null && idUsuario > 0) {
                String horaMySQL = convertirHoraMySQL(horarioSeleccionado);
                String resultado = "Cita agendada para:\n" + fechaMostrada + " a las " + horarioSeleccionado;
                tvResultadoCita.setText(resultado);
                dmlCitas.insertarCita(this, idUsuario, fechaMySQL, horaMySQL);
            } else {
                Toast.makeText(this, "Selecciona una fecha válida", Toast.LENGTH_SHORT).show();
            }
        });

        btnConsultarCitas.setOnClickListener(v -> {
            if (idUsuario > 0) {
                cargarCitas(idUsuario);
            } else {
                Toast.makeText(this, "Usuario no identificado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int obtenerIdUsuarioGuardado() {
        try {
            String idStr = getSharedPreferences(LoginActivity.PREFS_APP_NAME, MODE_PRIVATE)
                    .getString(LoginActivity.KEY_CURRENT_USER_IDENTIFIER, null);
            if (idStr != null) {
                return Integer.parseInt(idStr);
            }
        } catch (NumberFormatException e) {
            Log.e("ERROR", "Error al convertir hora", e);
        }
        return 0;
    }

    private void cargarCitas(int idUsuario) {
        listaDatos.clear();
        listaIds.clear();

        HelperCitas helper = new HelperCitas(this);
        Cursor cursor = helper.consultarCitasUsuario(idUsuario);

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No tienes citas registradas", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            return;
        }

        while (cursor.moveToNext()) {
            int idCita = cursor.getInt(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_ID));
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_FECHA));
            String hora = cursor.getString(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_HORA));
            String estado = cursor.getString(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_ESTADO));
            String motivo = cursor.getString(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_MOTIVO));

            String texto = "ID: " + idCita + "\nFecha: " + fecha + "\nHora: " + hora + "\nEstado: " + estado;

            if ("Cancelado".equalsIgnoreCase(estado) || "Reprogramado".equalsIgnoreCase(estado)) {
                texto += "\nMotivo: " + motivo;
            }

            listaDatos.add(texto);
            listaIds.add(idCita);
        }

        cursor.close();
        adapter.notifyDataSetChanged();
    }

    public void abrirDialogoEditar(int idCita) {
        HelperCitas helper = new HelperCitas(this);
        Cursor cursor = helper.consultarCitaPorId(idCita);

        if (cursor != null && cursor.moveToFirst()) {
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_FECHA));
            String hora = cursor.getString(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_HORA));
            String motivo = cursor.getString(cursor.getColumnIndexOrThrow(HelperCitas.COLUMN_MOTIVO));
            cursor.close();

            View dialogView = getLayoutInflater().inflate(R.layout.dialogo_editar_cita, null);

            EditText etFecha = dialogView.findViewById(R.id.etFechaEditar);
            Spinner spinnerHora = dialogView.findViewById(R.id.spinnerHoraEditar);
            EditText etMotivo = dialogView.findViewById(R.id.etMotivoEditar);

            etFecha.setText(fecha);
            etMotivo.setText(motivo);

            String[] horarios = {"08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM"};
            ArrayAdapter<String> adapterHora = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, horarios);
            adapterHora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHora.setAdapter(adapterHora);

            String horaAMPM = convertirHoraAMPM(hora);
            for (int i = 0; i < horarios.length; i++) {
                if (horarios[i].equalsIgnoreCase(horaAMPM)) {
                    spinnerHora.setSelection(i);
                    break;
                }
            }

            etFecha.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, day) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(year, month, day);
                    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    etFecha.setText(formato.format(sel.getTime()));
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dpd.show();
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Editar cita");
            builder.setView(dialogView);

            builder.setPositiveButton("Actualizar", (dialog, which) -> {
                String nuevaFecha = etFecha.getText().toString();
                String nuevaHora = convertirHoraMySQL(spinnerHora.getSelectedItem().toString());
                String nuevoMotivo = etMotivo.getText().toString();

                if (idUsuario > 0) {
                    dmlCitas.actualizarCita(this, idCita, idUsuario, nuevaFecha, nuevaHora, "Reprogramado", nuevoMotivo);
                    cargarCitas(idUsuario);
                } else {
                    Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNeutralButton("Cancelar cita", (dialog, which) -> mostrarDialogoCancelar(idCita));


            builder.setNegativeButton("Cerrar", null);
            builder.show();

        } else {
            Toast.makeText(this, "No se encontró la cita", Toast.LENGTH_SHORT).show();
        }
    }

    public void mostrarDialogoCancelar(int idCita) {
        View view = getLayoutInflater().inflate(R.layout.dialogo_cancelar_cita, null);
        EditText etMotivo = view.findViewById(R.id.etMotivoCancelar);

        new AlertDialog.Builder(this)
                .setTitle("¿Cancelar cita?")
                .setMessage("La cita será eliminada localmente y cancelada en el servidor.")
                .setView(view)
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    String motivo = etMotivo.getText().toString().trim();
                    if (motivo.isEmpty()) {
                        Toast.makeText(this, "Debes escribir un motivo", Toast.LENGTH_SHORT).show();
                    } else {
                        dmlCitas.cancelarCita(this, idCita, motivo);
                        cargarCitas(idUsuario);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private String convertirHoraMySQL(String hora12h) {
        try {
            SimpleDateFormat formato12h = new SimpleDateFormat("hh:mm a", Locale.US);
            SimpleDateFormat formato24h = new SimpleDateFormat("HH:mm:ss", Locale.US);
            return formato24h.format(formato12h.parse(hora12h));
        } catch (ParseException e) {
            Log.e("ERROR", "Error al convertir hora", e);
            return "00:00:00";
        }
    }

    private String convertirHoraAMPM(String hora24h) {
        try {
            SimpleDateFormat formato24h = new SimpleDateFormat("HH:mm:ss", Locale.US);
            SimpleDateFormat formato12h = new SimpleDateFormat("hh:mm a", Locale.US);
            return formato12h.format(formato24h.parse(hora24h));
        } catch (ParseException e) {
            Log.e("ERROR", "Error al convertir hora", e);
            return "12:00 AM";
        }
    }
}
