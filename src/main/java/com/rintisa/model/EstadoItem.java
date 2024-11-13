
package com.rintisa.model;

public enum EstadoItem {
    ACTIVO("Activo"),
    ANULADO("Anulado"),
    RECHAZADO("Rechazado");

    private final String descripcion;

    EstadoItem(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
