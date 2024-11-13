
package com.rintisa.model;

public enum Pantalla {
    USUARIOS("Usuarios", "users", "Gestión de usuarios del sistema"),
    ROLES("Roles", "roles", "Gestión de roles y permisos"),
    PERMISOS("Permisos", "permissions", "Gestión de permisos por pantalla"),
    RECEPCION("Recepción", "reception", "Recepción de mercancía"),
    PRODUCTOS("Productos", "products", "Gestión de productos"),
    REPORTES("Reportes", "reports", "Reportes del sistema");

    private final String nombre;
    private final String icono;
    private final String descripcion;

    Pantalla(String nombre, String icono, String descripcion) {
        this.nombre = nombre;
        this.icono = icono;
        this.descripcion = descripcion;
    }

    public String getNombre() { return nombre; }
    public String getIcono() { return icono; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() {
        return nombre;
    }

    // Método de utilidad para obtener una Pantalla por su nombre
    public static Pantalla porNombre(String nombre) {
        for (Pantalla pantalla : values()) {
            if (pantalla.getNombre().equalsIgnoreCase(nombre)) {
                return pantalla;
            }
        }
        throw new IllegalArgumentException("Pantalla no encontrada: " + nombre);
    }

    // Método para verificar si una pantalla existe
    public static boolean existe(String nombre) {
        try {
            porNombre(nombre);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}