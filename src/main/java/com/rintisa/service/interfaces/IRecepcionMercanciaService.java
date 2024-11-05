/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.rintisa.service.interfaces;

import com.rintisa.model.RecepcionMercancia;
import com.rintisa.model.DetalleRecepcion;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IRecepcionMercanciaService {
    /**
     * Crea una nueva recepción de mercancía
     * @param recepcion La recepción a crear
     * @return La recepción creada con su ID asignado
     */
    RecepcionMercancia crear(RecepcionMercancia recepcion) throws DatabaseException, ValidationException;

    /**
     * Actualiza una recepción existente
     * @param recepcion La recepción con los datos actualizados
     */
    void actualizar(RecepcionMercancia recepcion) throws DatabaseException, ValidationException;

    /**
     * Verifica una recepción
     * @param recepcionId ID de la recepción a verificar
     * @param observaciones Observaciones de la verificación
     */
    void verificarRecepcion(Long recepcionId, String observaciones) 
        throws DatabaseException, ValidationException;

    /**
     * Acepta una recepción y actualiza el inventario
     * @param recepcionId ID de la recepción a aceptar
     * @param observaciones Observaciones de la aceptación
     */
    void aceptarRecepcion(Long recepcionId, String observaciones) 
        throws DatabaseException, ValidationException;

    /**
     * Rechaza una recepción
     * @param recepcionId ID de la recepción a rechazar
     * @param motivo Motivo del rechazo
     */
    void rechazarRecepcion(Long recepcionId, String motivo) 
        throws DatabaseException, ValidationException;

    /**
     * Busca una recepción por su ID
     * @param id ID de la recepción
     * @return Optional con la recepción si existe
     */
    Optional<RecepcionMercancia> buscarPorId(Long id) throws DatabaseException;

    /**
     * Lista todas las recepciones
     * @return Lista de todas las recepciones
     */
    List<RecepcionMercancia> listarTodas() throws DatabaseException;

    /**
     * Lista las recepciones por estado
     * @param estado Estado de las recepciones a buscar
     * @return Lista de recepciones en el estado especificado
     */
    List<RecepcionMercancia> listarPorEstado(RecepcionMercancia.EstadoRecepcion estado) 
        throws DatabaseException;

    /**
     * Lista las recepciones por rango de fechas
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return Lista de recepciones en el rango de fechas
     */
    List<RecepcionMercancia> listarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException;

    /**
     * Agrega un detalle a una recepción
     * @param recepcionId ID de la recepción
     * @param detalle Detalle a agregar
     */
    void agregarDetalle(Long recepcionId, DetalleRecepcion detalle) 
        throws DatabaseException, ValidationException;

    /**
     * Lista los detalles de una recepción
     * @param recepcionId ID de la recepción
     * @return Lista de detalles de la recepción
     */
    List<DetalleRecepcion> listarDetalles(Long recepcionId) throws DatabaseException;

    /**
     * Genera un nuevo número de recepción
     * @return Número de recepción generado
     */
    String generarNumeroRecepcion() throws DatabaseException;

    /**
     * Obtiene estadísticas de recepciones por período
     * @param fechaInicio Fecha inicial del período
     * @param fechaFin Fecha final del período
     * @return Mapa con estadísticas
     */
    Map<String, Object> obtenerEstadisticas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException;

    /**
     * Exporta una recepción a formato PDF o Excel
     * @param recepcionId ID de la recepción
     * @param formato Formato de exportación ("PDF" o "EXCEL")
     * @return Ruta del archivo generado
     */
    String exportarRecepcion(Long recepcionId, String formato) 
        throws DatabaseException, ValidationException;

    /**
     * Busca recepciones con criterios múltiples
     * @param criterios Mapa de criterios de búsqueda
     * @return Lista de recepciones que cumplen los criterios
     */
    default List<RecepcionMercancia> buscar(Map<String, Object> criterios) throws DatabaseException {
        throw new UnsupportedOperationException("Búsqueda por criterios no implementada");
    }

    /**
     * Obtiene el flujo de trabajo de una recepción
     * @param recepcionId ID de la recepción
     * @return Lista de estados por los que ha pasado la recepción
     */
    default List<Map<String, Object>> obtenerFlujoTrabajo(Long recepcionId) throws DatabaseException {
        throw new UnsupportedOperationException("Obtención de flujo de trabajo no implementada");
    }

    /**
     * Verifica si una recepción se puede modificar
     * @param recepcionId ID de la recepción
     * @return true si la recepción se puede modificar
     */
    default boolean esModificable(Long recepcionId) throws DatabaseException {
        Optional<RecepcionMercancia> recepcion = buscarPorId(recepcionId);
        return recepcion.map(r -> r.getEstado() == RecepcionMercancia.EstadoRecepcion.PENDIENTE)
                       .orElse(false);
    }

    /**
     * Valida el acceso a una recepción
     * @param recepcionId ID de la recepción
     * @param usuarioId ID del usuario
     * @return true si el usuario tiene acceso
     */
    default boolean validarAcceso(Long recepcionId, Long usuarioId) throws DatabaseException {
        throw new UnsupportedOperationException("Validación de acceso no implementada");
    }

    /**
     * Notifica sobre cambios en una recepción
     * @param recepcionId ID de la recepción
     * @param evento Tipo de evento
     * @param detalles Detalles del evento
     */
    default void notificarCambio(Long recepcionId, String evento, Map<String, Object> detalles) 
        throws DatabaseException {
        throw new UnsupportedOperationException("Notificación de cambios no implementada");
    }
}