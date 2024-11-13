package com.rintisa.model;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

public class Producto {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String unidadMedida;
    private double precioUnitario;
    private int stockMinimo;
    private int stockActual;
    private boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaModificacion;
    private String categoria;
    private String ubicacion;

    // Constructor
    public Producto() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
        this.stockActual = 0;
        this.stockMinimo = 0;
        this.precioUnitario = 0.0;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    // Métodos de negocio

    /**
     * Verifica si el stock está por debajo del mínimo
     */
    public boolean tieneStockBajo() {
        return this.stockActual <= this.stockMinimo;
    }

    /**
     * Calcula el valor total del inventario de este producto
     */
    public double getValorInventario() {
        return this.stockActual * this.precioUnitario;
    }

    /**
     * Verifica si hay stock suficiente para una cantidad dada
     */
    public boolean tieneStockSuficiente(int cantidad) {
        return this.stockActual >= cantidad;
    }

    /**
     * Actualiza el stock y la fecha de modificación
     */
    public void actualizarStock(int nuevoStock) {
        this.stockActual = nuevoStock;
        this.fechaModificacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Producto{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", stockActual=" + stockActual +
                ", unidadMedida='" + unidadMedida + '\'' +
                '}';
    }

    public double getStockMaximo() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public double getPrecioCompra() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public double getPrecioVenta() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}