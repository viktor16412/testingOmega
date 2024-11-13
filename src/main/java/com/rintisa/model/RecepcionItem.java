
package com.rintisa.model;

import java.time.LocalDateTime;

public class RecepcionItem {
    private Long id;
    private RecepcionMercancia recepcion;
    private Producto producto;
    private int cantidad;
    private double precioUnitario;
    private String lote;
    private LocalDateTime fechaVencimiento;
    private String observaciones;
    private EstadoItem estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long usuarioModificacion;

    // Constructores
    public RecepcionItem() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoItem.ACTIVO;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecepcionMercancia getRecepcion() {
        return recepcion;
    }

    public void setRecepcion(RecepcionMercancia recepcion) {
        this.recepcion = recepcion;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public EstadoItem getEstado() {
        return estado;
    }

    public void setEstado(EstadoItem estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Long getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(Long usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    // Métodos de negocio
    public double getSubtotal() {
        return cantidad * precioUnitario;
    }

    public boolean estaVencido() {
        if (fechaVencimiento == null) {
            return false;
        }
        return fechaVencimiento.isBefore(LocalDateTime.now());
    }

    public boolean tieneStockSuficiente() {
        return producto != null && cantidad <= producto.getStockActual();
    }

    // Object methods
    @Override
    public String toString() {
        return "RecepcionItem{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getNombre() : "null") +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", lote='" + lote + '\'' +
                '}';
    }
}

