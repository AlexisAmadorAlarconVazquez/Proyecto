package com.example.proyectotitulacion;

public class Categoria {
    private String nombre;
    private int icono;

    public Categoria(String nombre, int icono) {
        this.nombre = nombre;
        this.icono = icono;
    }

    public String getNombre() {
        return nombre;
    }

    public int getIcono() {
        return icono;
    }
}
