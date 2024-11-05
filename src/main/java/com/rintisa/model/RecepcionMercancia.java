
package com.rintisa.model;

import java.time.LocalDateTime;

public class RecepcionMercancia {
    private Long id;
    private String numeroRecepcion;
    private LocalDateTime fechaRecepcion;
    private Usuario responsable;
    private String proveedor;
    private String numeroOrdenCompra;
    private EstadoRecepcion estado;
    private String observaciones;
    private LocalDateTime fechaVerificacion;
    private LocalDateTime fechaFinalizacion;
    
    public enum EstadoRecepcion {
        PENDIENTE("Pendiente"),
        VERIFICADO("Verificado"),
        ACEPTADO("Aceptado"),
        RECHAZADO("Rechazado");
        
        private final String descripcion;
        
        EstadoRecepcion(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public RecepcionMercancia() {
        this.fechaRecepcion = LocalDateTime.now();
        this.estado = EstadoRecepcion.PENDIENTE;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNumeroRecepcion() { return numeroRecepcion; }
    public void setNumeroRecepcion(String numeroRecepcion) { this.numeroRecepcion = numeroRecepcion; }
    
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }
    
    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario responsable) { this.responsable = responsable; }
    
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    
    public String getNumeroOrdenCompra() { return numeroOrdenCompra; }
    public void setNumeroOrdenCompra(String numeroOrdenCompra) { this.numeroOrdenCompra = numeroOrdenCompra; }
    
    public EstadoRecepcion getEstado() { return estado; }
    public void setEstado(EstadoRecepcion estado) { this.estado = estado; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public LocalDateTime getFechaVerificacion() { return fechaVerificacion; }
    public void setFechaVerificacion(LocalDateTime fechaVerificacion) { this.fechaVerificacion = fechaVerificacion; }
    
    public LocalDateTime getFechaFinalizacion() { return fechaFinalizacion; }
    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }
    
    
}
