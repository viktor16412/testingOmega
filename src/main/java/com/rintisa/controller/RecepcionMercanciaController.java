package com.rintisa.controller;

import com.rintisa.model.RecepcionMercancia;
import com.rintisa.model.DetalleRecepcion;
import com.rintisa.model.Producto;
import com.rintisa.service.interfaces.IRecepcionMercanciaService;
import com.rintisa.service.interfaces.IProductoService;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
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

    public RecepcionMercanciaController(
            IRecepcionMercanciaService recepcionService,
            IProductoService productoService,
            UsuarioController usuarioController) {
        this.recepcionService = recepcionService;
        this.productoService = productoService;
        this.usuarioController = usuarioController;
    }

    /**
     * Crea una nueva recepción de mercancía
     */
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
            Optional<RecepcionMercancia> recepcionOpt = recepcionService.buscarPorId(recepcionId);
            if (!recepcionOpt.isPresent()) {
                throw new ValidationException("recepcionId", "La recepción no existe");
            }

            RecepcionMercancia recepcion = recepcionOpt.get();
            
            // Verificar estado
            if (recepcion.getEstado() != RecepcionMercancia.EstadoRecepcion.PENDIENTE) {
                throw new ValidationException("estado", 
                    "Solo se pueden verificar recepciones en estado pendiente");
            }

            // Registrar cantidades recibidas
            List<DetalleRecepcion> detalles = recepcionService.listarDetalles(recepcionId);
            for (DetalleRecepcion detalle : detalles) {
                Integer cantidadRecibida = cantidadesRecibidas.get(detalle.getProducto().getId());
                if (cantidadRecibida != null) {
                    detalle.setCantidadRecibida(cantidadRecibida);
                }
            }

            // Verificar recepción
            recepcionService.verificarRecepcion(recepcionId, observaciones);
            logger.info("Recepción {} verificada", recepcionId);

        } catch (Exception e) {
            logger.error("Error al verificar recepción", e);
            throw new RuntimeException("No se pudo verificar la recepción: " + e.getMessage());
        }
    }

    /**
     * Acepta una recepción verificada
     */
    public void aceptarRecepcion(Long recepcionId, String observaciones) {
        try {
            Optional<RecepcionMercancia> recepcionOpt = recepcionService.buscarPorId(recepcionId);
            if (!recepcionOpt.isPresent()) {
                throw new ValidationException("recepcionId", "La recepción no existe");
            }

            RecepcionMercancia recepcion = recepcionOpt.get();
            
            // Verificar estado
            if (recepcion.getEstado() != RecepcionMercancia.EstadoRecepcion.VERIFICADO) {
                throw new ValidationException("estado", 
                    "Solo se pueden aceptar recepciones verificadas");
            }

            // Aceptar recepción
            recepcionService.aceptarRecepcion(recepcionId, observaciones);
            logger.info("Recepción {} aceptada", recepcionId);

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
            Optional<RecepcionMercancia> recepcionOpt = recepcionService.buscarPorId(recepcionId);
            if (!recepcionOpt.isPresent()) {
                throw new ValidationException("recepcionId", "La recepción no existe");
            }

            RecepcionMercancia recepcion = recepcionOpt.get();
            
            // Verificar estado
            if (recepcion.getEstado() == RecepcionMercancia.EstadoRecepcion.ACEPTADO ||
                recepcion.getEstado() == RecepcionMercancia.EstadoRecepcion.RECHAZADO) {
                throw new ValidationException("estado", 
                    "No se puede rechazar una recepción ya finalizada");
            }

            // Rechazar recepción
            recepcionService.rechazarRecepcion(recepcionId, motivo);
            logger.info("Recepción {} rechazada", recepcionId);

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
     * Obtiene los detalles de una recepción
     */
    public List<DetalleRecepcion> obtenerDetalles(Long recepcionId) {
        try {
            return recepcionService.listarDetalles(recepcionId);
        } catch (DatabaseException e) {
            logger.error("Error al obtener detalles de recepción", e);
            throw new RuntimeException("Error al obtener detalles: " + e.getMessage());
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
}
