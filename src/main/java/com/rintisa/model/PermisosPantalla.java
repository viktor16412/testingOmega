
package com.rintisa.model;

import java.time.LocalDateTime;

public class PermisosPantalla {
    private Long id;
    private String rolNombre;
    private Pantalla pantalla;
    private boolean acceso;
    private boolean edicion;
    private boolean eliminacion;
    private LocalDateTime fechaModificacion;
    private Long usuarioModificacion;

    // Constructor
    public PermisosPantalla() {
        this.acceso = false;
        this.edicion = false;
        this.eliminacion = false;
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor completo
    public PermisosPantalla(String rolNombre, Pantalla pantalla, boolean acceso, 
                           boolean edicion, boolean eliminacion) {
        this.rolNombre = rolNombre;
        this.pantalla = pantalla;
        this.acceso = acceso;
        this.edicion = edicion;
        this.eliminacion = eliminacion;
        this.fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }

    public Pantalla getPantalla() { return pantalla; }
    public void setPantalla(Pantalla pantalla) { this.pantalla = pantalla; }

    public boolean isAcceso() { return acceso; }
    public void setAcceso(boolean acceso) { 
        this.acceso = acceso;
        if (!acceso) {
            // Si no hay acceso, tampoco puede haber edición ni eliminación
            this.edicion = false;
            this.eliminacion = false;
        }
    }

    public boolean isEdicion() { return edicion; }
    public void setEdicion(boolean edicion) { 
        this.edicion = edicion;
        if (edicion) {
            // Si hay edición, debe haber acceso
            this.acceso = true;
        }
    }

    public boolean isEliminacion() { return eliminacion; }
    public void setEliminacion(boolean eliminacion) { 
        this.eliminacion = eliminacion;
        if (eliminacion) {
            // Si hay eliminación, debe haber acceso
            this.acceso = true;
        }
    }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Long getUsuarioModificacion() { return usuarioModificacion; }
    public void setUsuarioModificacion(Long usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    // Métodos de utilidad
    public void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PermisosPantalla{" +
                "rolNombre='" + rolNombre + '\'' +
                ", pantalla=" + pantalla +
                ", acceso=" + acceso +
                ", edicion=" + edicion +
                ", eliminacion=" + eliminacion +
                '}';
    }
}

