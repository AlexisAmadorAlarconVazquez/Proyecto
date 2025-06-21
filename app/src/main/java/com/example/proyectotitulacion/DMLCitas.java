package com.example.proyectotitulacion;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DMLCitas {

    private final HelperCitas helperCitas;

    public DMLCitas(Context context) {
        helperCitas = new HelperCitas(context);
    }

    // Insertar en ambas BD
    public void insertarCita(Context context, int idUsuario, String fecha, String hora) {
        boolean insertadoLocal = helperCitas.insertarCita(idUsuario, fecha, hora);
        if (insertadoLocal) {
            Toast.makeText(context, "Cita insertada localmente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error al insertar localmente", Toast.LENGTH_SHORT).show();
        }

        enviarCitaMySql(context, idUsuario, fecha, hora);
    }

    // Obtener citas en formato String para mostrar
    public List<String> obtenerCitasStringPorUsuario(int idUsuario) {
        return helperCitas.obtenerCitasPorUsuarioComoString(idUsuario);
    }

    // Actualizar cita
    public void actualizarCita(Context context, int idCita, int idUsuario, String nuevaFecha, String nuevaHora, String nuevoEstado, String nuevoMotivo) {
        boolean actualizadoLocal = helperCitas.actualizarCita(idCita, nuevaFecha, nuevaHora, nuevoMotivo);

        if (actualizadoLocal) {
            Toast.makeText(context, "Cita actualizada localmente", Toast.LENGTH_SHORT).show();
            enviarReagendadaMySQL(context, idCita, nuevaFecha, nuevaHora, nuevoEstado, nuevoMotivo);
        } else {
            Toast.makeText(context, "No se pudo actualizar la cita localmente", Toast.LENGTH_SHORT).show();
        }
    }

    // Enviar cita reagendada al servidor
    private void enviarReagendadaMySQL(Context context, int idCita, String fecha, String hora, String estado, String motivo) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.1.103/WebService/actualizar_cita.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String params = "id_cita=" + URLEncoder.encode(String.valueOf(idCita), StandardCharsets.UTF_8.name()) +
                        "&fecha=" + URLEncoder.encode(fecha, StandardCharsets.UTF_8.name()) +
                        "&hora=" + URLEncoder.encode(hora, StandardCharsets.UTF_8.name()) +
                        "&estado=" + URLEncoder.encode(estado, StandardCharsets.UTF_8.name()) +
                        "&motivo=" + URLEncoder.encode(motivo, StandardCharsets.UTF_8.name());

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(params.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(context, "Cita actualizada en servidor", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error al actualizar cita en servidor", Toast.LENGTH_SHORT).show();
                    }
                });

                conn.disconnect();
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // Enviar cita nueva al servidor
    private void enviarCitaMySql(Context context, int idUsuario, String fecha, String hora) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.1.103/WebService/registrar_cita.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String params = "id_usuario=" + URLEncoder.encode(String.valueOf(idUsuario), StandardCharsets.UTF_8.name()) +
                        "&fecha=" + URLEncoder.encode(fecha, StandardCharsets.UTF_8.name()) +
                        "&hora=" + URLEncoder.encode(hora, StandardCharsets.UTF_8.name());

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(params.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(context, "Cita enviada a MySQL", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error al enviar cita al servidor", Toast.LENGTH_SHORT).show();
                    }
                });

                conn.disconnect();
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // Cancelar cita
    public void cancelarCita(Context context, int idCita, String motivo) {
        boolean eliminado = helperCitas.eliminarCita(idCita);
        if (eliminado) {
            Toast.makeText(context, "Cita eliminada localmente", Toast.LENGTH_SHORT).show();
            enviarReagendadaMySQL(context, idCita, "", "", "Cancelado", motivo);
        } else {
            Toast.makeText(context, "Error al eliminar la cita localmente", Toast.LENGTH_SHORT).show();
        }
    }


    // Enviar cancelación a servidor
    private void enviarCanceladaMySQL(Context context, int idCita, String motivo) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.1.103/WebService/cancelar_cita.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String params = "id_cita=" + URLEncoder.encode(String.valueOf(idCita), "UTF-8") +
                        "&estado=Cancelado" +
                        "&motivo=" + URLEncoder.encode(motivo, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(params.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(context, "Cita cancelada en servidor", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error al cancelar en servidor", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

}
