
package com.rintisa.service.impl;


import com.rintisa.dao.impl.RecepcionMercanciaDao;
import com.rintisa.dao.interfaces.IRecepcionMercanciaDao;
import com.rintisa.dao.interfaces.IProductoDao;
import com.rintisa.service.interfaces.IRecepcionMercanciaService;
import com.rintisa.model.RecepcionMercancia;
import com.rintisa.model.DetalleRecepcion;
import com.rintisa.model.Producto;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

public class RecepcionMercanciaService implements IRecepcionMercanciaService {
    private static final Logger logger = LoggerFactory.getLogger(RecepcionMercanciaService.class);
    
    private final IRecepcionMercanciaDao recepcionDao;
    private final IProductoDao productoDao;

    public RecepcionMercanciaService(IRecepcionMercanciaDao recepcionDao, IProductoDao productoDao) {
        this.recepcionDao = recepcionDao;
        this.productoDao = productoDao;
    }

    public RecepcionMercanciaService(RecepcionMercanciaDao recepcionDao) {
         this(recepcionDao, null);// Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public RecepcionMercancia crear(RecepcionMercancia recepcion) throws DatabaseException, ValidationException {
        logger.debug("Creando nueva recepción de mercancía");
        
        // Validar datos
        validar(recepcion);
        
        // Generar número de recepción
        recepcion.setNumeroRecepcion(recepcionDao.generateNextNumeroRecepcion());
        
        // Establecer estado inicial
        recepcion.setEstado(RecepcionMercancia.EstadoRecepcion.PENDIENTE);
        recepcion.setFechaRecepcion(LocalDateTime.now());
        
        try {
            RecepcionMercancia recepcionCreada = recepcionDao.save(recepcion);
            logger.info("Recepción creada exitosamente: {}", recepcionCreada.getNumeroRecepcion());
            return recepcionCreada;
        } catch (DatabaseException e) {
            logger.error("Error al crear recepción", e);
            throw e;
        }
    }

    @Override
    public void verificarRecepcion(Long recepcionId, String observaciones) 
            throws DatabaseException, ValidationException {
        logger.debug("Verificando recepción: {}", recepcionId);
        
        Optional<RecepcionMercancia> recepcionOpt = recepcionDao.findById(recepcionId);
        if (!recepcionOpt.isPresent()) {
            throw new ValidationException("recepcionId", "La recepción no existe");
        }
        
        RecepcionMercancia recepcion = recepcionOpt.get();
        
        // Validar estado
        if (recepcion.getEstado() != RecepcionMercancia.EstadoRecepcion.PENDIENTE) {
            throw new ValidationException("estado", 
                "Solo se pueden verificar recepciones en estado pendiente");
        }

        // Obtener detalles
        List<DetalleRecepcion> detalles = recepcionDao.findDetallesByRecepcionId(recepcionId);
        if (detalles.isEmpty()) {
            throw new ValidationException("detalles", 
                "La recepción debe tener al menos un detalle para ser verificada");
        }
        
        // Verificar cantidades recibidas
        for (DetalleRecepcion detalle : detalles) {
            
            Integer cantidadRecibida = detalle.getCantidadRecibida();
                if (cantidadRecibida == null) {
                throw new ValidationException("cantidadRecibida", 
            "Todos los detalles deben tener cantidad recibida registrada");
    }
        }
        
        // Actualizar estado
        recepcion.setEstado(RecepcionMercancia.EstadoRecepcion.VERIFICADO);
        recepcion.setFechaVerificacion(LocalDateTime.now());
        recepcion.setObservaciones(observaciones);
        
        recepcionDao.update(recepcion);
        logger.info("Recepción {} verificada exitosamente", recepcionId);
    }

    @Override
    public void aceptarRecepcion(Long recepcionId, String observaciones) 
            throws DatabaseException, ValidationException {
        logger.debug("Aceptando recepción: {}", recepcionId);
        
        Optional<RecepcionMercancia> recepcionOpt = recepcionDao.findById(recepcionId);
        if (!recepcionOpt.isPresent()) {
            throw new ValidationException("recepcionId", "La recepción no existe");
        }
        
        RecepcionMercancia recepcion = recepcionOpt.get();
        
        // Validar estado
        if (recepcion.getEstado() != RecepcionMercancia.EstadoRecepcion.VERIFICADO) {
            throw new ValidationException("estado", 
                "Solo se pueden aceptar recepciones verificadas");
        }
        
        // Actualizar inventario
        List<DetalleRecepcion> detalles = recepcionDao.findDetallesByRecepcionId(recepcionId);
        for (DetalleRecepcion detalle : detalles) {
            Producto producto = productoDao.findById(detalle.getProducto().getId())
                .orElseThrow(() -> new ValidationException("productoId", "Producto no encontrado"));
            
            int nuevoStock = producto.getStockActual() + detalle.getCantidadRecibida();
            producto.setStockActual(nuevoStock);
            productoDao.update(producto);
        }
        
        // Actualizar estado
        recepcion.setEstado(RecepcionMercancia.EstadoRecepcion.ACEPTADO);
        recepcion.setFechaFinalizacion(LocalDateTime.now());
        recepcion.setObservaciones(observaciones);
        
        recepcionDao.update(recepcion);
        logger.info("Recepción {} aceptada exitosamente", recepcionId);
    }

    @Override
    public void rechazarRecepcion(Long recepcionId, String motivo) 
            throws DatabaseException, ValidationException {
        logger.debug("Rechazando recepción: {}", recepcionId);
        
        Optional<RecepcionMercancia> recepcionOpt = recepcionDao.findById(recepcionId);
        if (!recepcionOpt.isPresent()) {
            throw new ValidationException("recepcionId", "La recepción no existe");
        }
        
        RecepcionMercancia recepcion = recepcionOpt.get();
        
        // Validar estado
        if (recepcion.getEstado() == RecepcionMercancia.EstadoRecepcion.ACEPTADO ||
            recepcion.getEstado() == RecepcionMercancia.EstadoRecepcion.RECHAZADO) {
            throw new ValidationException("estado", 
                "No se puede rechazar una recepción ya finalizada");
        }
        
        // Actualizar estado
        recepcion.setEstado(RecepcionMercancia.EstadoRecepcion.RECHAZADO);
        recepcion.setFechaFinalizacion(LocalDateTime.now());
        recepcion.setObservaciones(motivo);
        
        recepcionDao.update(recepcion);
        logger.info("Recepción {} rechazada exitosamente", recepcionId);
    }

    @Override
    public void agregarDetalle(Long recepcionId, DetalleRecepcion detalle) 
            throws DatabaseException, ValidationException {
        logger.debug("Agregando detalle a recepción: {}", recepcionId);
        
        // Validar recepción
        Optional<RecepcionMercancia> recepcionOpt = recepcionDao.findById(recepcionId);
        if (!recepcionOpt.isPresent()) {
            throw new ValidationException("recepcionId", "La recepción no existe");
        }
        
        RecepcionMercancia recepcion = recepcionOpt.get();
        
        // Validar estado
        if (recepcion.getEstado() != RecepcionMercancia.EstadoRecepcion.PENDIENTE) {
            throw new ValidationException("estado", 
                "Solo se pueden agregar detalles a recepciones pendientes");
        }
        
        // Validar producto
        if (!productoDao.findById(detalle.getProducto().getId()).isPresent()) {
            throw new ValidationException("productoId", "El producto no existe");
        }
        
        // Validar cantidades
        if (detalle.getCantidadEsperada() <= 0) {
            throw new ValidationException("cantidadEsperada", 
                "La cantidad esperada debe ser mayor a cero");
        }
        
        detalle.setEstado(DetalleRecepcion.EstadoDetalle.PENDIENTE);
        recepcionDao.saveDetalle(detalle);
        logger.info("Detalle agregado exitosamente a recepción {}", recepcionId);
    }

    @Override
    public List<DetalleRecepcion> listarDetalles(Long recepcionId) throws DatabaseException {
        return recepcionDao.findDetallesByRecepcionId(recepcionId);
    }

    @Override
    public Map<String, Object> obtenerEstadisticas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
            throws DatabaseException {
        return recepcionDao.getEstadisticas(fechaInicio, fechaFin);
    }

    @Override
    public String generarNumeroRecepcion() throws DatabaseException {
        return recepcionDao.generateNextNumeroRecepcion();
    }
    
    // Métodos privados de utilidad
    private void validar(RecepcionMercancia recepcion) throws ValidationException {
        List<String> errores = new ArrayList<>();
        
        if (recepcion.getNumeroOrdenCompra() == null || 
            recepcion.getNumeroOrdenCompra().trim().isEmpty()) {
            errores.add("El número de orden de compra es requerido");
        }
        
        if (recepcion.getProveedor() == null) {
            errores.add("El proveedor es requerido");
        }
        
        if (recepcion.getResponsable() == null) {
            errores.add("El responsable es requerido");
        }
        
        if (!errores.isEmpty()) {
            throw new ValidationException("validación", String.join(", ", errores));
        }
    }

    // Implementar otros métodos según necesidades específicas
    @Override
    public List<RecepcionMercancia> listarPorEstado(RecepcionMercancia.EstadoRecepcion estado) 
            throws DatabaseException {
        return recepcionDao.findByEstado(estado);
    }

    @Override
    public List<RecepcionMercancia> listarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
            throws DatabaseException {
        return recepcionDao.findByFechas(fechaInicio, fechaFin);
    }

    @Override
    public List<RecepcionMercancia> listarTodas() throws DatabaseException {
        return recepcionDao.findAll();
    }

    @Override
    public Optional<RecepcionMercancia> buscarPorId(Long id) throws DatabaseException {
        return recepcionDao.findById(id);
    }

    @Override
    public void actualizar(RecepcionMercancia recepcion) throws DatabaseException, ValidationException {
        validar(recepcion);
        recepcionDao.update(recepcion);
    }

    @Override
    public String exportarRecepcion(Long recepcionId, String formato) 
            throws DatabaseException, ValidationException {
        // Implementar exportación según formato (PDF, Excel)
        throw new UnsupportedOperationException("Método no implementado");
    }
}
