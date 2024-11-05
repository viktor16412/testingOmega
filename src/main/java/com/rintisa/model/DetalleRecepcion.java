
package com.rintisa.model;

import java.time.LocalDateTime;

public class DetalleRecepcion {
    private Long id;
    private RecepcionMercancia recepcion;
    private Producto producto;
    private int cantidadEsperada;
    private Integer cantidadRecibida; // Cambiado a Integer para permitir null
    private double precioUnitario;
    private EstadoDetalle estado;
    private String observaciones;
    private LocalDateTime fechaVerificacion;
    
    public enum EstadoDetalle {
        PENDIENTE("Pendiente"),
        ACEPTADO("Aceptado"),
        RECHAZADO("Rechazado");
        
        private final String descripcion;
        
        EstadoDetalle(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public DetalleRecepcion() {
        this.estado = EstadoDetalle.PENDIENTE;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public RecepcionMercancia getRecepcion() { return recepcion; }
    public void setRecepcion(RecepcionMercancia recepcion) { this.recepcion = recepcion; }
    
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    
    public int getCantidadEsperada() { return cantidadEsperada; }
    public void setCantidadEsperada(int cantidadEsperada) { this.cantidadEsperada = cantidadEsperada; }
    
    public Integer getCantidadRecibida() { return cantidadRecibida; }
    public void setCantidadRecibida(Integer cantidadRecibida) { this.cantidadRecibida = cantidadRecibida; }
    
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    
    public EstadoDetalle getEstado() { return estado; }
    public void setEstado(EstadoDetalle estado) { this.estado = estado; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public LocalDateTime getFechaVerificacion() { return fechaVerificacion; }
    public void setFechaVerificacion(LocalDateTime fechaVerificacion) { 
        this.fechaVerificacion = fechaVerificacion; 
    }
    
    // Métodos de negocio
    public double getTotal() {
        return cantidadRecibida != null ? 
            cantidadRecibida * precioUnitario : 
            cantidadEsperada * precioUnitario;
    }
    
    public double getVariacion() {
        if (cantidadRecibida == null) return 0.0;
        return ((double) cantidadRecibida / cantidadEsperada) * 100;
    }
    
    public boolean tieneDiscrepancia() {
        return cantidadRecibida != null && cantidadRecibida != cantidadEsperada;
    }
    
    public double getDiferencia() {
        if (cantidadRecibida == null) return 0.0;
        return Math.abs(cantidadRecibida - cantidadEsperada) * precioUnitario;
    }
    
    public boolean estaCompleto() {
        return cantidadRecibida != null && estado != EstadoDetalle.PENDIENTE;
    }
    
    @Override
    public String toString() {
        return "DetalleRecepcion{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getCodigo() : "null") +
                ", cantidadEsperada=" + cantidadEsperada +
                ", cantidadRecibida=" + (cantidadRecibida != null ? cantidadRecibida : "pendiente") +
                ", estado=" + estado +
                '}';
    }
}