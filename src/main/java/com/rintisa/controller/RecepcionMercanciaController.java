package com.rintisa.controller;

import com.rintisa.model.RecepcionMercancia;
import com.rintisa.model.DetalleRecepcion;
import com.rintisa.model.Producto;
import com.rintisa.model.Proveedor;
import com.rintisa.service.interfaces.IRecepcionMercanciaService;
import com.rintisa.service.interfaces.IProductoService;
import com.rintisa.service.interfaces.IProveedorService;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import com.rintisa.model.Proveedor;
import com.rintisa.model.RecepcionMercancia.EstadoRecepcion;
import com.rintisa.model.Usuario;
import com.rintisa.service.impl.RecepcionMercanciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public class RecepcionMercanciaController {
    private static final Logger logger = LoggerFactory.getLogger(RecepcionMercanciaController.class);
    
    private final IRecepcionMercanciaService recepcionService;
    private final IProductoService productoService;
    private final UsuarioController usuarioController;
    private final IProveedorService proveedorService;

    public RecepcionMercanciaController(
            IRecepcionMercanciaService recepcionService,
            IProductoService productoService, IProveedorService proveedorService,
            UsuarioController usuarioController) {
        
        if (recepcionService == null) throw new IllegalArgumentException("recepcionService no puede ser null");
        if (productoService == null) throw new IllegalArgumentException("productoService no puede ser null");
        if (proveedorService == null) throw new IllegalArgumentException("proveedorService no puede ser null");
        if (usuarioController == null) throw new IllegalArgumentException("usuarioController no puede ser null");    
        
        this.recepcionService = recepcionService;
        this.productoService = productoService;
        this.proveedorService = proveedorService;
        this.usuarioController = usuarioController;
    }
    
    public RecepcionMercancia crearRecepcion(String numeroOrdenCompra, String proveedorId, String observaciones) {
        try {
            // Crear objeto recepción
            RecepcionMercancia recepcion = new RecepcionMercancia();
            recepcion.setNumeroOrdenCompra(numeroOrdenCompra);
            recepcion.setProveedor(proveedorId);
            
            recepcion.setObservaciones(observaciones);
            recepcion.setResponsable(usuarioController.getUsuarioActual());
            recepcion.setNumeroRecepcion(recepcionService.generarNumeroRecepcion());

            // Guardar recepción
            RecepcionMercancia recepcionCreada = recepcionService.crear(recepcion);
            logger.info("Recepción creada exitosamente: {}", recepcionCreada.getNumeroRecepcion());
            return recepcionCreada;

        } catch (Exception e) {
            logger.error("Error al crear recepción", e);
            throw new RuntimeException("No se pudo crear la recepción: " + e.getMessage());
        }
    }

    public List<Proveedor> obtenerProveedores() {
        try {
            return proveedorService.listarTodos(); // Necesitas inyectar el ProveedorService en el constructor
        } catch (Exception e) {
            logger.error("Error al obtener lista de proveedores", e);
            throw new RuntimeException("No se pudo obtener la lista de proveedores: " + e.getMessage());
        }
    }
        
       
        /**
     * Agrega un producto a la recepción
     */
    public void agregarProducto(Long recepcionId, Long productoId, 
                              int cantidadEsperada, double precioUnitario) {
         try {
            // Verificar que exista el producto
            Optional<Producto> producto = productoService.buscarPorId(productoId);
            if (!producto.isPresent()) {
                throw new ValidationException("productoId", "El producto no existe");
            }

            // Crear detalle
            DetalleRecepcion detalle = new DetalleRecepcion();
            detalle.setProducto(producto.get());
            detalle.setCantidadEsperada(cantidadEsperada);
            detalle.setPrecioUnitario(precioUnitario);

            // Agregar a la recepción
            recepcionService.agregarDetalle(recepcionId, detalle);
            
            logger.info("Producto agregado a recepción {}: {}", recepcionId, productoId);
        } catch (Exception e) {
            logger.error("Error al agregar producto a recepción", e);
            throw new RuntimeException("No se pudo agregar el producto: " + e.getMessage());
        }
    }

    /**
     * Verifica una recepción
     */
    public void verificarRecepcion(Long recepcionId, Map<Long, Integer> cantidadesRecibidas, 
                                 String observaciones) {
           try {
            logger.debug("Iniciando verificación de recepción {}", recepcionId);
            
            // Verificar que exista la recepción
            Optional<RecepcionMercancia> recepcionOpt = recepcionService.buscarPorId(recepcionId);
            if (!recepcionOpt.isPresent()) {
                throw new ValidationException("id", "La recepción no existe");
            }

            RecepcionMercancia recepcion = recepcionOpt.get();

            // Verificar estado
            if (recepcion.getEstado() != EstadoRecepcion.PENDIENTE) {
                throw new ValidationException("estado",
                    "Solo se pueden verificar recepciones en estado PENDIENTE");
            }

            // Validar cantidades
            if (cantidadesRecibidas == null || cantidadesRecibidas.isEmpty()) {
                throw new ValidationException("cantidades",
                    "Debe proporcionar las cantidades recibidas");
            }

            // Llamar al servicio para verificar la recepción
            recepcionService.verificarRecepcion(recepcionId, cantidadesRecibidas, observaciones);
            
            logger.info("Recepción {} verificada exitosamente", recepcionId);
            
        } catch (Exception e) {
            logger.error("Error al verificar recepción", e);
            throw new RuntimeException("No se pudo verificar la recepción: " + e.getMessage());
        }
    }
    
    
    
    
    /**
     * Obtiene los detalles de una recepción
     */
    public List<DetalleRecepcion> obtenerDetalles(Long recepcionId) {
        try {
            return recepcionService.listarDetalles(recepcionId);
        } catch (Exception e) {
            logger.error("Error al obtener detalles de recepción", e);
            throw new RuntimeException("No se pudieron obtener los detalles: " + e.getMessage());
        }
    }
    
     /**
     * Verifica si el usuario tiene permiso para verificar recepciones
     */
    public boolean tienePermisoVerificar() {
        try {
            return usuarioController.tienePermiso("ALMACEN_RECEPCION_VERIFICAR");
        } catch (Exception e) {
            logger.error("Error al verificar permisos", e);
            return false;
        }
    }
    
    
    
    /**
     * Obtiene la lista de productos activos
     */
    public List<Producto> obtenerProductos() {
        try {
            return productoService.listarTodos();
        } catch (Exception e) {
            logger.error("Error al obtener lista de productos", e);
            throw new RuntimeException("No se pudo obtener la lista de productos: " + e.getMessage());
        }
    }
    
    
    
    
    
    
    
    
    
    

    /**
     * Acepta una recepción verificada
     */
    public void aceptarRecepcion(Long recepcionId, String observaciones) {
        try {
            logger.debug("Aceptando recepción: {}", recepcionId);
            
            // Verificar que exista la recepción
            Optional<RecepcionMercancia> recepcionOpt = recepcionService.buscarPorId(recepcionId);
            if (!recepcionOpt.isPresent()) {
                throw new ValidationException("id", "La recepción no existe");
            }

            RecepcionMercancia recepcion = recepcionOpt.get();

            // Verificar estado
            if (recepcion.getEstado() != EstadoRecepcion.VERIFICADO) {
                throw new ValidationException("estado",
                    "Solo se pueden aceptar recepciones verificadas");
            }

            // Aceptar recepción
            recepcionService.aceptarRecepcion(recepcionId, observaciones);
            
            logger.info("Recepción {} aceptada exitosamente", recepcionId);
            
        } catch (Exception e) {
            logger.error("Error al aceptar recepción", e);
            throw new RuntimeException("No se pudo aceptar la recepción: " + e.getMessage());
        }
    }

    /**
     * Rechaza una recepción
     */
    public void rechazarRecepcion(Long recepcionId, String motivo) {
        try {
            logger.debug("Rechazando recepción: {}", recepcionId);
            
            // Verificar que exista la recepción
            Optional<RecepcionMercancia> recepcionOpt = recepcionService.buscarPorId(recepcionId);
            if (!recepcionOpt.isPresent()) {
                throw new ValidationException("id", "La recepción no existe");
            }

            RecepcionMercancia recepcion = recepcionOpt.get();

            // Verificar estado
            if (recepcion.getEstado() == EstadoRecepcion.ACEPTADO ||
                recepcion.getEstado() == EstadoRecepcion.RECHAZADO) {
                throw new ValidationException("estado",
                    "No se puede rechazar una recepción que ya está finalizada");
            }

            // Validar motivo
            if (motivo == null || motivo.trim().isEmpty()) {
                throw new ValidationException("motivo",
                    "Debe proporcionar un motivo para el rechazo");
            }

            // Rechazar recepción
            recepcionService.rechazarRecepcion(recepcionId, motivo);
            
            logger.info("Recepción {} rechazada exitosamente", recepcionId);
            
        } catch (Exception e) {
            logger.error("Error al rechazar recepción", e);
            throw new RuntimeException("No se pudo rechazar la recepción: " + e.getMessage());
        }
    }

    /**
     * Lista las recepciones según filtros
     */
    public List<RecepcionMercancia> listarRecepciones(
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin,
            RecepcionMercancia.EstadoRecepcion estado) {
        try {
            if (estado != null) {
                return recepcionService.listarPorEstado(estado);
            } else if (fechaInicio != null && fechaFin != null) {
                return recepcionService.listarPorFechas(fechaInicio, fechaFin);
            } else {
                return recepcionService.listarTodas();
            }
        } catch (DatabaseException e) {
            logger.error("Error al listar recepciones", e);
            throw new RuntimeException("Error al obtener lista de recepciones: " + e.getMessage());
        }
    }

   

    /**
     * Exporta una recepción a PDF o Excel
     */
    public String exportarRecepcion(Long recepcionId, String formato) {
        try {
            return recepcionService.exportarRecepcion(recepcionId, formato);
        } catch (Exception e) {
            logger.error("Error al exportar recepción", e);
            throw new RuntimeException("Error al exportar recepción: " + e.getMessage());
        }
    }

    /**
     * Obtiene estadísticas de recepciones
     */
    public Map<String, Object> obtenerEstadisticas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            return recepcionService.obtenerEstadisticas(fechaInicio, fechaFin);
        } catch (DatabaseException e) {
            logger.error("Error al obtener estadísticas", e);
            throw new RuntimeException("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    /**
     * Verifica si el usuario actual tiene permisos de almacén
     */
    public boolean tienePermisoAlmacen() {
        return usuarioController.getUsuarioActual() != null &&
               usuarioController.getUsuarioActual().getRol() != null &&
               "ALMACEN".equalsIgnoreCase(usuarioController.getUsuarioActual().getRol().getNombre());
    }
    
    
    public String obtenerNombreProveedor(String proveedorId) {
    try {
        Optional<Proveedor> proveedor = proveedorService.buscarPorId(Long.parseLong(proveedorId));
        return proveedor.map(Proveedor::getRazonSocial).orElse("N/A");
    } catch (Exception e) {
        logger.error("Error al obtener nombre del proveedor", e);
        return "N/A";
    }
        }
    
    
    public void actualizarRecepcion(RecepcionMercancia recepcion) {
          try {
            logger.debug("Iniciando actualización de recepción ID: {}", recepcion.getId());

            // Obtener la recepción original primero
            Optional<RecepcionMercancia> recepcionOriginalOpt = recepcionService.buscarPorId(recepcion.getId());
            if (!recepcionOriginalOpt.isPresent()) {
                throw new ValidationException("id", "La recepción no existe");
            }

            RecepcionMercancia recepcionOriginal = recepcionOriginalOpt.get();
            logger.debug("Recepción original obtenida. Responsable: {}", 
                recepcionOriginal.getResponsable() != null ? 
                recepcionOriginal.getResponsable().getId() : "null");

            // Validar que exista el responsable en la recepción original
            if (recepcionOriginal.getResponsable() == null) {
                // Si no tiene responsable, asignar el usuario actual
                recepcionOriginal.setResponsable(usuarioController.getUsuarioActual());
                logger.debug("Asignando usuario actual como responsable: {}", 
                    usuarioController.getUsuarioActual().getId());
            }

            // Verificar estado
            if (recepcionOriginal.getEstado() != EstadoRecepcion.PENDIENTE) {
                throw new ValidationException("estado", 
                    "Solo se pueden actualizar recepciones en estado PENDIENTE");
            }

            // Validar que exista el proveedor
            Long idProveedor = Long.parseLong(recepcion.getProveedor());
            Optional<Proveedor> proveedor = proveedorService.buscarPorId(idProveedor);
            if (!proveedor.isPresent()) {
                throw new ValidationException("proveedorId", "El proveedor seleccionado no existe");
            }

            // Actualizar solo los campos editables
            recepcionOriginal.setNumeroOrdenCompra(recepcion.getNumeroOrdenCompra());
            recepcionOriginal.setProveedor(recepcion.getProveedor());
            recepcionOriginal.setObservaciones(recepcion.getObservaciones());

            logger.debug("Actualizando recepción. Responsable final: {}", 
                recepcionOriginal.getResponsable().getId());

            // Actualizar la recepción
            recepcionService.actualizar(recepcionOriginal);
            logger.info("Recepción actualizada exitosamente: {}", 
                recepcionOriginal.getNumeroRecepcion());

        } catch (Exception e) {
            logger.error("Error al actualizar recepción", e);
            throw new RuntimeException("No se pudo actualizar la recepción: " + e.getMessage());
        }
    }
    
    public Usuario getUsuarioActual() {
      return usuarioController.getUsuarioActual();
    }
    
    public String getNombreUsuarioActual() {
        Usuario usuario = getUsuarioActual();
        if (usuario != null) {
            return String.format("%s %s", 
                usuario.getNombre() != null ? usuario.getNombre() : "",
                usuario.getApellido() != null ? usuario.getApellido() : "").trim();
        }
        return "Usuario no identificado";
    }

    /**
     * Verifica si hay un usuario activo en la sesión
     */
    public boolean hayUsuarioActivo() {
        return getUsuarioActual() != null;
    }
    
     public void eliminarRecepcion(Long recepcionId) {
        try {
            // Obtener la recepción
            Optional<RecepcionMercancia> recepcionOpt = recepcionService.buscarPorId(recepcionId);
            if (!recepcionOpt.isPresent()) {
                throw new ValidationException("id", "La recepción no existe");
            }

            RecepcionMercancia recepcion = recepcionOpt.get();

            // Verificar estado
            if (recepcion.getEstado() != EstadoRecepcion.PENDIENTE) {
                throw new ValidationException("estado", 
                    "Solo se pueden eliminar recepciones en estado PENDIENTE");
            }

            // Eliminar la recepción
            recepcionService.eliminar(recepcionId);
            logger.info("Recepción {} eliminada exitosamente", recepcionId);
            
        } catch (Exception e) {
            logger.error("Error al eliminar recepción", e);
            throw new RuntimeException("No se pudo eliminar la recepción: " + e.getMessage());
        }
    }
    
    
    
    
}
