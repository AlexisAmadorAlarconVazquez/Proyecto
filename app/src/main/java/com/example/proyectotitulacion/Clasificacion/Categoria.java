package com.example.proyectotitulacion.Clasificacion;

public class Categoria {
    private String nombre;
    private int icono;
    private CategoriaDetalle detalle;

    public Categoria(String nombre, int icono, CategoriaDetalle detalle) {
        this.nombre = nombre;
        this.icono = icono;
        this.detalle = detalle;
    }

    public String getNombre() {
        return nombre;
    }

    public int getIcono() {
        return icono;
    }

    public CategoriaDetalle getDetalle() {
        return detalle;
    }
}