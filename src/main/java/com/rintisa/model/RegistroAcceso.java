package com.rintisa.model;

import java.time.LocalDateTime;

public class RegistroAcceso {
    private Long id;
    private Usuario usuario;
    private LocalDateTime fechaAcceso;
    private TipoAcceso tipoAcceso;
    private String ipAddress;
    private String detalles;

    public enum TipoAcceso {
        LOGIN("Inicio de Sesión"),
        LOGOUT("Cierre de Sesión"),
        FAILED_LOGIN("Intento Fallido");

        private final String descripcion;

        TipoAcceso(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }

    // Constructor
    public RegistroAcceso() {
        this.fechaAcceso = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaAcceso() {
        return fechaAcceso;
    }

    public void setFechaAcceso(LocalDateTime fechaAcceso) {
        this.fechaAcceso = fechaAcceso;
    }

    public TipoAcceso getTipoAcceso() {
        return tipoAcceso;
    }

    public void setTipoAcceso(TipoAcceso tipoAcceso) {
        this.tipoAcceso = tipoAcceso;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }
}