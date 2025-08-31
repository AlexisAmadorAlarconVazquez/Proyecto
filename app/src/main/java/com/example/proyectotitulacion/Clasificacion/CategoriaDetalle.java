package com.example.proyectotitulacion.Clasificacion;

public class CategoriaDetalle {
    private String nombreEspecialista;
    private String especialidad;
    private String ubicaciones;
    private int gifResourceId;

    public CategoriaDetalle(String nombreEspecialista, String especialidad, String ubicaciones, int gifResourceId) {
        this.nombreEspecialista = nombreEspecialista;
        this.especialidad = especialidad;
        this.ubicaciones = ubicaciones;
        this.gifResourceId = gifResourceId;
    }

    // Getters
    public String getNombreEspecialista() {
        return nombreEspecialista;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getUbicaciones() {
        return ubicaciones;
    }

    public int getGifResourceId() {
        return gifResourceId;
    }
}