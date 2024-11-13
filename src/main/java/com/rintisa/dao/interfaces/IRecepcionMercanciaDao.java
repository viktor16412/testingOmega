
package com.rintisa.dao.interfaces;

import com.rintisa.model.RecepcionMercancia;
import com.rintisa.model.DetalleRecepcion;
import com.rintisa.exception.DatabaseException;
import com.rintisa.model.enums.EstadoRecepcion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IRecepcionMercanciaDao extends IGenericDao<RecepcionMercancia, Long> {
    
    
    //Busca una recepción por su número
    Optional<RecepcionMercancia> findByNumeroRecepcion(String numeroRecepcion) throws DatabaseException;
   
    //Lista las recepciones por proveedor
    List<RecepcionMercancia> findByProveedor(Long proveedorId) throws DatabaseException;
    
    //Lista las recepciones por orden de compra
    List<RecepcionMercancia> findByOrdenCompra(String numeroOrdenCompra) throws DatabaseException;
    
    //Actualiza un detalle de recepción
    void updateDetalle(DetalleRecepcion detalle) throws DatabaseException;
    
    
    //Elimina un detalle de recepción
    void deleteDetalle(Long detalleId) throws DatabaseException; 
    
    //Verifica si existe una recepción con el número dado
    boolean existsByNumeroRecepcion(String numeroRecepcion) throws DatabaseException;
    
    
    //Lista las recepciones pendientes de un proveedor
    List<RecepcionMercancia> findPendientesByProveedor(Long proveedorId) throws DatabaseException;
    
    
    //Lista las recepciones verificadas pendientes de aceptación/rechazo
    List<RecepcionMercancia> findVerificadasPendientes() throws DatabaseException;
    
    //Lista las recepciones que tienen discrepancias
  //(cantidad esperada != cantidad recibida)
    List<RecepcionMercancia> findWithDiscrepancias() throws DatabaseException;
    
    
    //Obtiene el historial de estados de una recepción
    List<EstadoRecepcion> getHistorialEstados(Long recepcionId) throws DatabaseException;
    
    
    
    //Actualiza el estado de una recepción 
    void updateEstado(Long recepcionId, EstadoRecepcion estado, String observaciones) 
        throws DatabaseException;
    
    
    //Obtiene la cantidad total recibida de un producto en un período
   int getTotalRecibido(Long productoId, LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException;
    
    //Lista las últimas recepciones (con límite)   
   List<RecepcionMercancia> findLastRecepciones(int limit) throws DatabaseException;
    
    
    //Busca recepciones con criterios múltiples  
    List<RecepcionMercancia> search(
        String numeroRecepcion,
        String numeroOrdenCompra,
        Long proveedorId,
        EstadoRecepcion estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
    ) throws DatabaseException;
    
    
    //Marca una recepción como anulada
    void anular(Long recepcionId, String motivo, Long usuarioId) throws DatabaseException;
    
    
    //Obtiene el conteo de recepciones por estado
    java.util.Map<EstadoRecepcion, Integer> getConteosPorEstado() 
       throws DatabaseException;
    
    
    //Limpia los detalles de una recepción
    void deleteAllDetalles(Long recepcionId) throws DatabaseException;
    
    
    //Copia los detalles de una recepción a otra
   void copyDetalles(Long recepcionOrigenId, Long recepcionDestinoId) throws DatabaseException;

    
     //Obtiene los detalles de una recepción
    List<DetalleRecepcion> findDetallesByRecepcionId(Long recepcionId) throws DatabaseException;
    
      //Obtiene estadísticas de recepciones para un período
    java.util.Map<String, Object> getEstadisticas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException;
    
    //Genera el siguiente número de recepción
    String generateNextNumeroRecepcion() throws DatabaseException;
    
     //Guarda un detalle de recepción
    DetalleRecepcion saveDetalle(DetalleRecepcion detalle) throws DatabaseException;
    
     //Lista las recepciones por estado
    List<RecepcionMercancia> findByEstado(EstadoRecepcion estado) throws DatabaseException;
    
    //Lista las recepciones por rango de fechas
    List<RecepcionMercancia> findByFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException;
    
    void eliminar(Long id) throws DatabaseException;
    
    void delete(Long id) throws DatabaseException;
    
    void deleteWithTransaction(Long id) throws DatabaseException;
    
    RecepcionMercancia save(RecepcionMercancia recepcion) throws DatabaseException;
    Optional<RecepcionMercancia> findById(Long id) throws DatabaseException;
    List<RecepcionMercancia> findAll() throws DatabaseException;
    void update(RecepcionMercancia recepcion) throws DatabaseException;
    
    
        
    /**
     * Busca recepciones por número de documento
     * @param numeroDocumento Número de documento a buscar
     * @return Lista de recepciones que coinciden con el número de documento
     * @throws DatabaseException si ocurre un error en la base de datos
     */
     RecepcionMercancia findByNumeroDocumento(String numeroDocumento) throws DatabaseException;

    
    
    /**
     * Encuentra las recepciones dentro de un rango de fechas.
     *
     * @param fechaInicio la fecha de inicio del rango
     * @param fechaFin la fecha de fin del rango
     * @return una lista de recepciones dentro del rango de fechas
     * @throws DatabaseException si ocurre un error en la consulta a la base de datos
     */
    List<RecepcionMercancia> findByDateRange(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws DatabaseException;


}