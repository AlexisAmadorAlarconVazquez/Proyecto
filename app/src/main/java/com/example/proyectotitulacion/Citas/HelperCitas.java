package com.example.proyectotitulacion.Citas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class HelperCitas extends SQLiteOpenHelper {

    private static final String DB_NAME = "citas.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_CITAS = "citas";
    public static final String COLUMN_ID = "IDCita";
    public static final String COLUMN_ID_USUARIO = "IDUsuario";
    public static final String COLUMN_FECHA = "Fecha";
    public static final String COLUMN_HORA = "Hora";
    public static final String COLUMN_ESTADO = "Estado";
    public static final String COLUMN_MOTIVO = "Motivo";

    public HelperCitas(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_CITAS = "CREATE TABLE " + TABLE_CITAS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ID_USUARIO + " INTEGER, " +
                COLUMN_FECHA + " TEXT, " +
                COLUMN_HORA + " TEXT, " +
                COLUMN_ESTADO + " TEXT, " +
                COLUMN_MOTIVO + " TEXT)";
        db.execSQL(CREATE_TABLE_CITAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITAS);
        onCreate(db);
    }

    public boolean insertarCita(int idUsuario, String fecha, String hora) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COLUMN_ID_USUARIO, idUsuario);
        valores.put(COLUMN_FECHA, fecha);
        valores.put(COLUMN_HORA, hora);
        valores.put(COLUMN_ESTADO, "Agendado");
        valores.put(COLUMN_MOTIVO, "");

        long resultado = db.insert(TABLE_CITAS, null, valores);
        db.close();
        return resultado != -1;
    }

    public Cursor consultarCitasUsuario(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CITAS + " WHERE " + COLUMN_ID_USUARIO + " = ?", new String[]{String.valueOf(idUsuario)});
    }

    public List<String> obtenerCitasPorUsuarioComoString(int idUsuario) {
        List<String> listaCitas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_FECHA + ", " + COLUMN_HORA + ", " + COLUMN_ESTADO + ", " + COLUMN_MOTIVO +
                " FROM " + TABLE_CITAS + " WHERE " + COLUMN_ID_USUARIO + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(idUsuario)});

        if (cursor.moveToFirst()) {
            do {
                int idCita = cursor.getInt(0);
                String fecha = cursor.getString(1);
                String hora = cursor.getString(2);
                String estado = cursor.getString(3);
                String motivo = cursor.getString(4);

                String citaStr = "ID: " + idCita + "\n" +
                        "Fecha: " + fecha + "\n" +
                        "Hora: " + hora + "\n" +
                        "Estado: " + estado;

                if ("Cancelado".equalsIgnoreCase(estado) || "Reprogramado".equalsIgnoreCase(estado)) {
                    citaStr += "\nMotivo: " + (motivo != null ? motivo : "N/A");
                }

                listaCitas.add(citaStr);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return listaCitas;
    }

    public boolean actualizarCita(int idCita, String nuevaFecha, String nuevaHora, String motivo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COLUMN_FECHA, nuevaFecha);
        valores.put(COLUMN_HORA, nuevaHora);
        valores.put(COLUMN_ESTADO, "Reprogramado");
        valores.put(COLUMN_MOTIVO, motivo);

        int filasActualizadas = db.update(TABLE_CITAS, valores, COLUMN_ID + " = ?", new String[]{String.valueOf(idCita)});
        db.close();
        return filasActualizadas > 0;
    }
    public Cursor consultarCitaPorId(int idCita) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CITAS + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(idCita)});
    }

    public boolean eliminarCita(int idCita) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete(TABLE_CITAS, COLUMN_ID + " = ?", new String[]{String.valueOf(idCita)});
        db.close();
        return filas > 0;
    }


}
