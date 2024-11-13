
package com.rintisa.model.enums;

public enum EstadoRecepcion {
    PENDIENTE("Pendiente", "Recepción registrada pero no procesada"),
    EN_PROCESO("En Proceso", "Recepción en proceso de verificación"),
    APROBADA("Aprobada", "Recepción verificada y aprobada"),
    PROCESADA("Procesada", "Recepción procesada y stock actualizado"),
    ANULADA("Anulada", "Recepción anulada"),
    RECHAZADA("Rechazada", "Recepción rechazada por inconsistencias");

    private final String nombre;
    private final String descripcion;

    EstadoRecepcion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Obtiene un EstadoRecepcion a partir de su nombre
     * @param nombre Nombre del estado
     * @return EstadoRecepcion correspondiente
     * @throws IllegalArgumentException si el nombre no corresponde a ningún estado
     */
    public static EstadoRecepcion fromNombre(String nombre) {
        for (EstadoRecepcion estado : EstadoRecepcion.values()) {
            if (estado.getNombre().equalsIgnoreCase(nombre)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + nombre);
    }

    /**
     * Verifica si se puede transicionar a otro estado
     * @param nuevoEstado Estado al que se quiere transicionar
     * @return true si la transición es válida
     */
    public boolean puedeTransicionarA(EstadoRecepcion nuevoEstado) {
        switch (this) {
            case PENDIENTE:
                return nuevoEstado == EN_PROCESO || 
                       nuevoEstado == ANULADA;
                
            case EN_PROCESO:
                return nuevoEstado == APROBADA || 
                       nuevoEstado == RECHAZADA;
                
            case APROBADA:
                return nuevoEstado == PROCESADA || 
                       nuevoEstado == RECHAZADA;
                
            case PROCESADA:
                return nuevoEstado == ANULADA;
                
            case ANULADA:
            case RECHAZADA:
                return false;
                
            default:
                return false;
        }
    }

    /**
     * Verifica si el estado es un estado final
     * @return true si es un estado final
     */
    public boolean esEstadoFinal() {
        return this == PROCESADA || this == ANULADA || this == RECHAZADA;
    }

    /**
     * Verifica si el estado permite edición
     * @return true si se puede editar en este estado
     */
    public boolean permitaEdicion() {
        return this == PENDIENTE || this == EN_PROCESO;
    }

    /**
     * Verifica si se pueden agregar items en este estado
     * @return true si se pueden agregar items
     */
    public boolean permiteAgregarItems() {
        return this == PENDIENTE;
    }
    
}