
package com.rintisa.model;

import com.rintisa.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.rintisa.model.enums.EstadoRecepcion;

/*
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
    private String proveedorNombre;
    
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
    
     public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    // Modificar el método getProveedor para que use el nombre si está disponible
    public String getProveedorDisplay() {
        return proveedorNombre != null ? proveedorNombre : proveedor;
    }
    
    // Método para clonar una recepción
    public RecepcionMercancia clone() {
        RecepcionMercancia clone = new RecepcionMercancia();
        clone.setId(this.id);
        clone.setNumeroRecepcion(this.numeroRecepcion);
        clone.setFechaRecepcion(this.fechaRecepcion);
        clone.setProveedor(this.proveedor);
        clone.setNumeroOrdenCompra(this.numeroOrdenCompra);
        clone.setEstado(this.estado);
        clone.setObservaciones(this.observaciones);
        clone.setFechaVerificacion(this.fechaVerificacion);
        clone.setFechaFinalizacion(this.fechaFinalizacion);
        clone.setResponsable(this.responsable);
        clone.setProveedorNombre(this.proveedorNombre);
        return clone;
    }    
}
*/
public class RecepcionMercancia {
    private Long id;
    private String numeroRecepcion;
    private String numeroDocumento;
    private String numeroOrdenCompra;
    private String numeroGuiaRemision;
    private LocalDateTime fecha;
    private Proveedor proveedor;
    private Usuario usuario;
    private Usuario responsable;
    private List<RecepcionItem> items;
    private double total;
    private String observaciones;
    private EstadoRecepcion estado;  // Referencia directa al enum en el mismo paquete
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long usuarioModificacion;
    
    // Constructor
    public RecepcionMercancia() {
        this.items = new ArrayList<>();
        this.fecha = LocalDateTime.now();
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoRecepcion.PENDIENTE;
    }

    // Getters y Setters originales
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public EstadoRecepcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoRecepcion estado) {
        this.estado = estado;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    // Nuevos getters y setters para numeroOrdenCompra
    public String getNumeroOrdenCompra() {
        return numeroOrdenCompra;
    }

    public void setNumeroOrdenCompra(String numeroOrdenCompra) {
        this.numeroOrdenCompra = numeroOrdenCompra;
    }

    // Nuevos getters y setters para numeroGuiaRemision
    public String getNumeroGuiaRemision() {
        return numeroGuiaRemision;
    }

    public void setNumeroGuiaRemision(String numeroGuiaRemision) {
        this.numeroGuiaRemision = numeroGuiaRemision;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<RecepcionItem> getItems() {
        return items;
    }

    public void setItems(List<RecepcionItem> items) {
        this.items = items;
        calcularTotal();
    }

    public double getTotal() {
        return total;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
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

     public String getNumeroRecepcion() {
        return numeroRecepcion;
    }
    public void setNumeroRecepcion(String numeroRecepcion) {
        this.numeroRecepcion = numeroRecepcion;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public void setResponsable(Usuario responsable) {
        this.responsable = responsable;
    }
    
     // Métodos helper para responsable
    public void setResponsableId(Long responsableId) {
        if (this.responsable == null) {
            this.responsable = new Usuario();
        }
        this.responsable.setId(responsableId);
    }

    public void setResponsableId(String responsableId) {
        if (responsableId != null && !responsableId.trim().isEmpty()) {
            try {
                setResponsableId(Long.parseLong(responsableId.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID de responsable inválido: " + responsableId);
            }
        }
    }
    
    public Long getResponsableId() {
        return responsable != null ? responsable.getId() : null;
    }

    // Método para generar número de recepción automático
    public void generarNumeroRecepcion() {
        String año = String.valueOf(LocalDate.now().getYear());
        String mes = String.format("%02d", LocalDate.now().getMonthValue());
        String random = String.format("%04d", new Random().nextInt(10000));
        this.numeroRecepcion = "REC-" + año + mes + "-" + random;
    }

    // Método para validar campos obligatorios
    public void validar() throws ValidationException {
        List<String> errores = new ArrayList<>();

        if (numeroRecepcion == null || numeroRecepcion.trim().isEmpty()) {
            errores.add("Número de recepción es requerido");
        }
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            errores.add("Número de documento es requerido");
        }
        if (numeroOrdenCompra == null || numeroOrdenCompra.trim().isEmpty()) {
            errores.add("Número de orden de compra es requerido");
        }
        if (numeroGuiaRemision == null || numeroGuiaRemision.trim().isEmpty()) {
            errores.add("Número de guía de remisión es requerido");
        }
        if (proveedor == null || proveedor.getId() == null) {
            errores.add("Proveedor es requerido");
        }
        if (usuario == null || usuario.getId() == null) {
            errores.add("Usuario es requerido");
        }
        if (responsable == null || responsable.getId() == null) {
            errores.add("Responsable es requerido");
        }
        if (items.isEmpty()) {
            errores.add("Debe incluir al menos un ítem");
        }

        if (!errores.isEmpty()) {
            throw new ValidationException("Errores de validación: " + String.join(", ", errores));
        }
    }
    
    // Métodos de negocio
    public void agregarItem(RecepcionItem item) {
        item.setRecepcion(this);
        items.add(item);
        calcularTotal();
    }

    public void removerItem(RecepcionItem item) {
        items.remove(item);
        calcularTotal();
    }

    public void calcularTotal() {
        this.total = items.stream()
                .mapToDouble(RecepcionItem::getSubtotal)
                .sum();
    }

    public boolean tieneItemsVencidos() {
        return items.stream().anyMatch(RecepcionItem::estaVencido);
    }

    public boolean puedeSerProcesada() {
        return !items.isEmpty() && 
               estado == EstadoRecepcion.PENDIENTE &&
               items.stream().allMatch(RecepcionItem::tieneStockSuficiente);
    }

    public boolean validarDocumentos() {
        return numeroDocumento != null && !numeroDocumento.trim().isEmpty() &&
               numeroOrdenCompra != null && !numeroOrdenCompra.trim().isEmpty() &&
               numeroGuiaRemision != null && !numeroGuiaRemision.trim().isEmpty();
    }
    
    
    // Métodos específicos para manejo de estados
    public void procesarRecepcion() throws ValidationException {
        if (!estado.puedeTransicionarA(EstadoRecepcion.PROCESADA)) {
            throw new ValidationException("No se puede procesar la recepción en estado: " + estado);
        }
        if (items.isEmpty()) {
            throw new ValidationException("No se puede procesar una recepción sin items");
        }
        this.estado = EstadoRecepcion.PROCESADA;
    }
    
    public void anularRecepcion(String motivo) throws ValidationException {
        if (!estado.puedeTransicionarA(EstadoRecepcion.ANULADA)) {
            throw new ValidationException("No se puede anular la recepción en estado: " + estado);
        }
        this.estado = EstadoRecepcion.ANULADA;
        this.observaciones = (this.observaciones != null ? this.observaciones + ". " : "") 
                         + "ANULADO: " + motivo;
    }
    
    public void rechazarRecepcion(String motivo) throws ValidationException {
        if (!estado.puedeTransicionarA(EstadoRecepcion.RECHAZADA)) {
            throw new ValidationException("No se puede rechazar la recepción en estado: " + estado);
        }
        this.estado = EstadoRecepcion.RECHAZADA;
        this.observaciones = (this.observaciones != null ? this.observaciones + ". " : "") 
                         + "RECHAZADO: " + motivo;
    }
    
        // Validaciones basadas en estado
    public boolean puedeSerEditada() {
        return estado.permitaEdicion();
    }
    
    public boolean puedeAgregarItems() {
        return estado.permiteAgregarItems();
    }
         
    /**
     * Establece el proveedor usando un ID
     * @param proveedorId ID del proveedor
     */
    public void setProveedorId(Long proveedorId) {
        if (this.proveedor == null) {
            this.proveedor = new Proveedor();
        }
        this.proveedor.setId(proveedorId);
    }

    /**
     * Establece el proveedor usando un String ID
     * @param proveedorId ID del proveedor en formato String
     */
    public void setProveedorId(String proveedorId) {
        if (proveedorId != null && !proveedorId.trim().isEmpty()) {
            try {
                setProveedorId(Long.parseLong(proveedorId.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID de proveedor inválido: " + proveedorId);
            }
        }
    }
    
    /**
     * Obtiene el ID del proveedor
     * @return ID del proveedor o null si no hay proveedor
     */
    public Long getProveedorId() {
        return proveedor != null ? proveedor.getId() : null;
    }

    // Métodos similares para usuario
    public void setUsuarioId(Long usuarioId) {
        if (this.usuario == null) {
            this.usuario = new Usuario();
        }
        this.usuario.setId(usuarioId);
    }
    
    public void setUsuarioId(String usuarioId) {
        if (usuarioId != null && !usuarioId.trim().isEmpty()) {
            try {
                setUsuarioId(Long.parseLong(usuarioId.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID de usuario inválido: " + usuarioId);
            }
        }
    }

    public Long getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }
    
    
    // Object methods
     @Override
    public String toString() {
        return "RecepcionMercancia{" +
                "id=" + id +
                ", numeroRecepcion='" + numeroRecepcion + '\'' +
                ", numeroDocumento='" + numeroDocumento + '\'' +
                ", numeroOrdenCompra='" + numeroOrdenCompra + '\'' +
                ", numeroGuiaRemision='" + numeroGuiaRemision + '\'' +
                ", fecha=" + fecha +
                ", proveedor=" + (proveedor != null ? proveedor.getRazonSocial() : "null") +
                ", responsable=" + (responsable != null ? responsable.getNombre() : "null") +
                ", estado=" + estado +
                ", total=" + total +
                '}';
    }
}