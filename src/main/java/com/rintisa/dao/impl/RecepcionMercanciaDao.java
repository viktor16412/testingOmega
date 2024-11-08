
package com.rintisa.dao.impl;

import com.rintisa.config.DatabaseConfig;
import com.rintisa.dao.interfaces.IRecepcionMercanciaDao;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import com.rintisa.model.RecepcionMercancia;
import com.rintisa.model.DetalleRecepcion;
import com.rintisa.model.Producto;
import com.rintisa.model.RecepcionMercancia.EstadoRecepcion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RecepcionMercanciaDao implements IRecepcionMercanciaDao {
    private static final Logger logger = LoggerFactory.getLogger(RecepcionMercanciaDao.class);

    @Override
    public RecepcionMercancia save(RecepcionMercancia recepcion) throws DatabaseException {
        String sql = "INSERT INTO recepciones_mercancia (numero_recepcion, fecha_recepcion, " +
                    "proveedor_id, usuario_id, numero_orden_compra, estado, observaciones) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, recepcion.getNumeroRecepcion());
            stmt.setTimestamp(2, Timestamp.valueOf(recepcion.getFechaRecepcion()));
            stmt.setString(3, recepcion.getProveedor());
            stmt.setLong(4, recepcion.getResponsable().getId());
            stmt.setString(5, recepcion.getNumeroOrdenCompra());
            stmt.setString(6, recepcion.getEstado().name());
            stmt.setString(7, recepcion.getObservaciones());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating reception failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    recepcion.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("Creating reception failed, no ID obtained.");
                }
            }

            return recepcion;
        } catch (SQLException e) {
            logger.error("Error saving reception", e);
            throw new DatabaseException("Error saving reception: " + e.getMessage());
        }
    }
    
    @Override
    public Optional<RecepcionMercancia> findById(Long id) throws DatabaseException {
        String sql = "SELECT rm.*, u.username as responsable_username, " +
                    "p.razon_social as proveedor_nombre " +
                    "FROM recepciones_mercancia rm " +
                    "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                    "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                    "WHERE rm.id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRecepcion(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding reception by ID", e);
            throw new DatabaseException("Error finding reception: " + e.getMessage());
        }
    }

    @Override
    public List<RecepcionMercancia> findAll() throws DatabaseException {
        String sql = "SELECT rm.*, p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "ORDER BY rm.fecha_recepcion DESC";
                
    List<RecepcionMercancia> recepciones = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            recepciones.add(mapResultSetToRecepcion(rs));
        }
        return recepciones;
    } catch (SQLException e) {
        logger.error("Error al obtener las recepciones", e);
        throw new DatabaseException("Error al obtener la lista de recepciones: " + e.getMessage());
    }
    }
    
    @Override
    public List<RecepcionMercancia> findByFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException {
    logger.debug("Buscando recepciones entre {} y {}", fechaInicio, fechaFin);
    
    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.fecha_recepcion BETWEEN ? AND ? " +
                "ORDER BY rm.fecha_recepcion DESC";

    List<RecepcionMercancia> recepciones = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
        stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }

        logger.debug("Se encontraron {} recepciones en el rango de fechas", recepciones.size());
        return recepciones;

    } catch (SQLException e) {
        logger.error("Error al buscar recepciones por fechas", e);
        throw new DatabaseException("Error al buscar recepciones por fechas: " + e.getMessage());
    }
}

    @Override
    public void update(RecepcionMercancia recepcion) throws DatabaseException {
          String sql = "UPDATE recepciones_mercancia SET " +
                "estado = ?, " +
                "observaciones = ?, " +
                "fecha_verificacion = ?, " +
                "fecha_finalizacion = ? " +
                "WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        // Log para debug
        logger.debug("Actualizando recepción ID: {}, Estado: {}", 
            recepcion.getId(), recepcion.getEstado());

        // Establecer parámetros
        stmt.setString(1, recepcion.getEstado().name());
        
        // Manejar observaciones que pueden ser null
        if (recepcion.getObservaciones() != null && !recepcion.getObservaciones().trim().isEmpty()) {
            stmt.setString(2, recepcion.getObservaciones().trim());
        } else {
            stmt.setNull(2, Types.VARCHAR);
        }
        
        // Manejar fecha de verificación
        if (recepcion.getFechaVerificacion() != null) {
            stmt.setTimestamp(3, Timestamp.valueOf(recepcion.getFechaVerificacion()));
        } else {
            stmt.setNull(3, Types.TIMESTAMP);
        }

        // Manejar fecha de finalización
        if (recepcion.getFechaFinalizacion() != null) {
            stmt.setTimestamp(4, Timestamp.valueOf(recepcion.getFechaFinalizacion()));
        } else {
            stmt.setNull(4, Types.TIMESTAMP);
        }
        
        // ID de la recepción
        stmt.setLong(5, recepcion.getId());

        // Ejecutar la actualización
        int affectedRows = stmt.executeUpdate();
        
        if (affectedRows == 0) {
            throw new DatabaseException("No se encontró la recepción con ID: " + recepcion.getId());
        }

        logger.debug("Recepción actualizada exitosamente. ID: {}", recepcion.getId());

    } catch (SQLException e) {
        logger.error("Error SQL al actualizar recepción: {}", e.getMessage());
        throw new DatabaseException("Error al actualizar recepción: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) throws DatabaseException {
        // Este método ahora puede llamar a deleteWithTransaction
        deleteWithTransaction(id);
    }
    
    @Override
    public List<RecepcionMercancia> findByEstado(RecepcionMercancia.EstadoRecepcion estado) 
        throws DatabaseException {
    logger.debug("Buscando recepciones por estado: {}", estado);
    
    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.estado = ? " +
                "ORDER BY rm.fecha_recepcion DESC";

    List<RecepcionMercancia> recepciones = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, estado.name());

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }

        logger.debug("Se encontraron {} recepciones en estado {}", recepciones.size(), estado);
        return recepciones;

    } catch (SQLException e) {
        logger.error("Error al buscar recepciones por estado", e);
        throw new DatabaseException("Error al buscar recepciones por estado: " + e.getMessage());
    }
}
    
    
    @Override
    public DetalleRecepcion saveDetalle(DetalleRecepcion detalle) throws DatabaseException {
    
        String sql = "INSERT INTO detalle_recepcion " +
                    "(recepcion_id, producto_id, cantidad_esperada, " +
                    "precio_unitario, estado) " +
                    "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, detalle.getRecepcion().getId());
            stmt.setLong(2, detalle.getProducto().getId());
            stmt.setInt(3, detalle.getCantidadEsperada());
            stmt.setDouble(4, detalle.getPrecioUnitario());
            stmt.setString(5, detalle.getEstado().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("No se pudo crear el detalle de recepción");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    detalle.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("No se pudo obtener el ID generado para el detalle");
                }
            }

            logger.debug("Detalle guardado con ID: {}", detalle.getId());
            return detalle;
            
        } catch (SQLException e) {
            logger.error("Error al guardar detalle de recepción", e);
            throw new DatabaseException("Error al guardar detalle: " + e.getMessage());
        }
    }

    
    @Override
    public void deleteWithTransaction(Long id) throws DatabaseException {
    Connection conn = null;
    try {
        conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);
        
        // Primero eliminar los detalles
        String sqlDetalles = "DELETE FROM detalle_recepcion WHERE recepcion_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlDetalles)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
            logger.debug("Detalles de la recepción {} eliminados", id);
        }
        
        // Luego eliminar la recepción
        String sqlRecepcion = "DELETE FROM recepciones_mercancia WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlRecepcion)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("No se pudo eliminar la recepción");
            }
            logger.debug("Recepción {} eliminada", id);
        }
        
        // Commit si todo fue exitoso
        conn.commit();
        logger.info("Recepción {} y sus detalles eliminados exitosamente", id);
        
    } catch (Exception e) {
        // Rollback en caso de error
        if (conn != null) {
            try {
                conn.rollback();
                logger.debug("Rollback ejecutado por error en eliminación");
            } catch (SQLException ex) {
                logger.error("Error al hacer rollback", ex);
            }
        }
        logger.error("Error al eliminar recepción con transacción", e);
        throw new DatabaseException("Error al eliminar recepción: " + e.getMessage());
        
    } finally {
        // Restaurar autoCommit y cerrar conexión
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error al cerrar conexión", ex);
            }
        }
    }
        }
    
    
    
    
    
    
    
    
    
    
    @Override
    public String generateNextNumeroRecepcion() throws DatabaseException {
    logger.debug("Generando nuevo número de recepción");
    
    // Obtener el año actual
    int yearActual = LocalDateTime.now().getYear();
    String prefix = "REC";
    
    Connection conn = null;
    PreparedStatement stmtSelect = null;
    PreparedStatement stmtInsert = null;
    PreparedStatement stmtUpdate = null;
    ResultSet rs = null;

    try {
        conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false); // Iniciar transacción

        // Consultar el último valor para el año actual
        String selectSql = "SELECT current_value FROM recepcion_sequence " +
                          "WHERE prefix = ? AND year = ? FOR UPDATE";
        
        stmtSelect = conn.prepareStatement(selectSql);
        stmtSelect.setString(1, prefix);
        stmtSelect.setInt(2, yearActual);
        rs = stmtSelect.executeQuery();

        int nextValue;
        
        if (rs.next()) {
            // Si existe un registro para este año, incrementar el valor
            nextValue = rs.getInt("current_value") + 1;
            
            // Actualizar el valor
            String updateSql = "UPDATE recepcion_sequence SET current_value = ? " +
                             "WHERE prefix = ? AND year = ?";
            
            stmtUpdate = conn.prepareStatement(updateSql);
            stmtUpdate.setInt(1, nextValue);
            stmtUpdate.setString(2, prefix);
            stmtUpdate.setInt(3, yearActual);
            stmtUpdate.executeUpdate();
            
        } else {
            // Si no existe registro para este año, crear uno nuevo comenzando en 1
            nextValue = 1;
            
            String insertSql = "INSERT INTO recepcion_sequence (prefix, year, current_value) " +
                             "VALUES (?, ?, ?)";
            
            stmtInsert = conn.prepareStatement(insertSql);
            stmtInsert.setString(1, prefix);
            stmtInsert.setInt(2, yearActual);
            stmtInsert.setInt(3, nextValue);
            stmtInsert.executeUpdate();
        }

        // Commit de la transacción
        conn.commit();

        // Formato: REC-YYYY-NNNNN (ejemplo: REC-2024-00001)
        String numeroRecepcion = String.format("%s-%d-%05d", prefix, yearActual, nextValue);
        
        logger.info("Número de recepción generado: {}", numeroRecepcion);
        return numeroRecepcion;

    } catch (SQLException e) {
        // Rollback en caso de error
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.error("Error al realizar rollback", ex);
            }
        }
        logger.error("Error al generar número de recepción", e);
        throw new DatabaseException("Error al generar número de recepción: " + e.getMessage());
    } finally {
        // Cerrar todos los recursos en orden inverso
        try {
            if (rs != null) rs.close();
            if (stmtSelect != null) stmtSelect.close();
            if (stmtInsert != null) stmtInsert.close();
            if (stmtUpdate != null) stmtUpdate.close();
            if (conn != null) {
                conn.setAutoCommit(true); // Restaurar autocommit
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("Error al cerrar recursos", e);
        }
    }
}
    
    
    @Override
    public Map<String, Object> getEstadisticas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException {
    logger.debug("Obteniendo estadísticas para el período {} - {}", fechaInicio, fechaFin);
    
    Map<String, Object> estadisticas = new HashMap<>();
    Connection conn = null;
    
    try {
        conn = DatabaseConfig.getConnection();
        
        // Obtener estadísticas generales
        estadisticas.putAll(getEstadisticasGenerales(conn, fechaInicio, fechaFin));
        
        // Obtener estadísticas por estado
        estadisticas.put("porEstado", getEstadisticasPorEstado(conn, fechaInicio, fechaFin));
        
        // Obtener estadísticas por proveedor
        estadisticas.put("porProveedor", getEstadisticasPorProveedor(conn, fechaInicio, fechaFin));
        
        // Obtener productos más recibidos
        estadisticas.put("productosMasRecibidos", getProductosMasRecibidos(conn, fechaInicio, fechaFin));
        
        // Obtener análisis de discrepancias
        estadisticas.put("analisisDiscrepancias", getAnalisisDiscrepancias(conn, fechaInicio, fechaFin));
        
        return estadisticas;
        
    } catch (SQLException e) {
        logger.error("Error al obtener estadísticas", e);
        throw new DatabaseException("Error al obtener estadísticas: " + e.getMessage());
    } finally {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error al cerrar conexión", e);
            }
        }
    }
    }
    
    
    @Override
    public List<RecepcionMercancia> search(String numeroRecepcion, String numeroOrdenCompra,
                                     Long proveedorId, RecepcionMercancia.EstadoRecepcion estado,
                                     LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException {
            
    logger.debug("Buscando recepciones con criterios múltiples");
    StringBuilder sql = new StringBuilder();
    List<Object> params = new ArrayList<>();
    
    sql.append("SELECT rm.*, u.username as responsable_username, ")
       .append("p.razon_social as proveedor_nombre ")
       .append("FROM recepciones_mercancia rm ")
       .append("LEFT JOIN usuarios u ON rm.usuario_id = u.id ")
       .append("LEFT JOIN proveedores p ON rm.proveedor_id = p.id ")
       .append("WHERE 1=1 ");

    // Agregar condiciones según los parámetros proporcionados
    if (numeroRecepcion != null && !numeroRecepcion.trim().isEmpty()) {
        sql.append("AND rm.numero_recepcion LIKE ? ");
        params.add("%" + numeroRecepcion.trim() + "%");
    }

    if (numeroOrdenCompra != null && !numeroOrdenCompra.trim().isEmpty()) {
        sql.append("AND rm.numero_orden_compra LIKE ? ");
        params.add("%" + numeroOrdenCompra.trim() + "%");
    }

    if (proveedorId != null) {
        sql.append("AND rm.proveedor_id = ? ");
        params.add(proveedorId);
    }

    if (estado != null) {
        sql.append("AND rm.estado = ? ");
        params.add(estado.name());
    }

    if (fechaInicio != null) {
        sql.append("AND rm.fecha_recepcion >= ? ");
        params.add(Timestamp.valueOf(fechaInicio));
    }

    if (fechaFin != null) {
        sql.append("AND rm.fecha_recepcion <= ? ");
        params.add(Timestamp.valueOf(fechaFin));
    }

    sql.append("ORDER BY rm.fecha_recepcion DESC");

    List<RecepcionMercancia> recepciones = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

        // Establecer parámetros
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }

        logger.debug("Búsqueda completada. Se encontraron {} recepciones", recepciones.size());
        return recepciones;

    } catch (SQLException e) {
        logger.error("Error en búsqueda de recepciones", e);
        throw new DatabaseException("Error al buscar recepciones: " + e.getMessage());
    }
}
    
    
    @Override
    public void anular(Long recepcionId, String motivo, Long usuarioId) throws DatabaseException {
    logger.debug("Anulando recepción ID: {}", recepcionId);
    
    Connection conn = null;
    try {
        conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);

        // Verificar estado actual
        String checkSql = "SELECT estado FROM recepciones_mercancia WHERE id = ? FOR UPDATE";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setLong(1, recepcionId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                throw new ValidationException("recepcionId", "La recepción no existe");
            }
            
            String estadoActual = rs.getString("estado");
            if ("ANULADO".equals(estadoActual)) {
                throw new ValidationException("estado", "La recepción ya está anulada");
            }
            if ("ACEPTADO".equals(estadoActual)) {
                throw new ValidationException("estado", 
                    "No se puede anular una recepción ya aceptada");
            }
        }

        // Registrar la anulación
        String insertHistorialSql = "INSERT INTO recepcion_historial " +
                                  "(recepcion_id, estado_anterior, estado_nuevo, " +
                                  "motivo, usuario_id, fecha) " +
                                  "VALUES (?, ?, 'ANULADO', ?, ?, CURRENT_TIMESTAMP)";
                                  
        try (PreparedStatement historialStmt = conn.prepareStatement(insertHistorialSql)) {
            historialStmt.setLong(1, recepcionId);
            historialStmt.setString(2, "PENDIENTE");
            historialStmt.setString(3, motivo);
            historialStmt.setLong(4, usuarioId);
            historialStmt.executeUpdate();
        }

        // Actualizar estado de la recepción
        String updateSql = "UPDATE recepciones_mercancia " +
                          "SET estado = 'ANULADO', " +
                          "fecha_anulacion = CURRENT_TIMESTAMP, " +
                          "usuario_anulacion_id = ?, " +
                          "motivo_anulacion = ? " +
                          "WHERE id = ?";
                          
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setLong(1, usuarioId);
            updateStmt.setString(2, motivo);
            updateStmt.setLong(3, recepcionId);
            updateStmt.executeUpdate();
        }

        conn.commit();
        logger.info("Recepción {} anulada exitosamente", recepcionId);

    } catch (SQLException | ValidationException e) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.error("Error al realizar rollback", ex);
            }
        }
        logger.error("Error al anular recepción", e);
        throw new DatabaseException("Error al anular recepción: " + e.getMessage());
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logger.error("Error al cerrar conexión", e);
            }
        }
    }
}   
    
    @Override
    public void deleteAllDetalles(Long recepcionId) throws DatabaseException {
        String sql = "DELETE FROM detalle_recepcion WHERE recepcion_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, recepcionId);
            stmt.executeUpdate();
            logger.debug("Detalles de la recepción {} eliminados", recepcionId);
        } catch (SQLException e) {
            logger.error("Error al eliminar detalles de recepción", e);
            throw new DatabaseException("Error al eliminar detalles: " + e.getMessage());
        }
    }
    
    

    
    
    
    
    @Override
    public void copyDetalles(Long recepcionOrigenId, Long recepcionDestinoId) 
        throws DatabaseException {
            
    logger.debug("Copiando detalles de recepción {} a recepción {}", 
        recepcionOrigenId, recepcionDestinoId);
    
    String insertSql = "INSERT INTO detalle_recepcion " +
                      "(recepcion_id, producto_id, cantidad_esperada, " +
                      "precio_unitario, estado, observaciones) " +
                      "SELECT ?, producto_id, cantidad_esperada, " +
                      "precio_unitario, 'PENDIENTE', " +
                      "'Copiado de recepción ' || ? " +
                      "FROM detalle_recepcion " +
                      "WHERE recepcion_id = ?";

    Connection conn = null;
    try {
        conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);

        // Verificar que la recepción destino existe y está en estado PENDIENTE
        String checkSql = "SELECT estado FROM recepciones_mercancia WHERE id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setLong(1, recepcionDestinoId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                throw new ValidationException("recepcionDestinoId", 
                    "La recepción destino no existe");
            }
            
            String estado = rs.getString("estado");
            if (!"PENDIENTE".equals(estado)) {
                throw new ValidationException("estado", 
                    "Solo se pueden copiar detalles a recepciones pendientes");
            }
        }

        // Copiar detalles
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setLong(1, recepcionDestinoId);
            insertStmt.setLong(2, recepcionOrigenId);
            insertStmt.setLong(3, recepcionOrigenId);
            
            int copiedRows = insertStmt.executeUpdate();
            logger.info("Se copiaron {} detalles de la recepción {} a la recepción {}", 
                copiedRows, recepcionOrigenId, recepcionDestinoId);
        }

        // Actualizar total de la recepción destino
        actualizarTotalRecepcion(recepcionDestinoId, conn);

        conn.commit();

    } catch (SQLException | ValidationException e) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.error("Error al realizar rollback", ex);
            }
        }
        logger.error("Error al copiar detalles", e);
        throw new DatabaseException("Error al copiar detalles: " + e.getMessage());
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logger.error("Error al cerrar conexión", e);
            }
        }
    }
}
    
    @Override
    public List<RecepcionMercancia> findLastRecepciones(int limit) throws DatabaseException {
    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "ORDER BY rm.fecha_recepcion DESC LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, limit);
        List<RecepcionMercancia> recepciones = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }
        
        return recepciones;
    } catch (SQLException e) {
        logger.error("Error al obtener últimas recepciones", e);
        throw new DatabaseException("Error al obtener últimas recepciones: " + e.getMessage());
    }
}
    
    @Override
    public Optional<RecepcionMercancia> findByNumeroRecepcion(String numeroRecepcion) 
        throws DatabaseException {
    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.numero_recepcion = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, numeroRecepcion);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapResultSetToRecepcion(rs));
            }
        }
        
        return Optional.empty();
    } catch (SQLException e) {
        logger.error("Error al buscar recepción por número", e);
        throw new DatabaseException("Error al buscar recepción: " + e.getMessage());
    }
}
    
    @Override
    public List<RecepcionMercancia> findByProveedor(Long proveedorId) throws DatabaseException {
    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.proveedor_id = ? " +
                "ORDER BY rm.fecha_recepcion DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setLong(1, proveedorId);
        List<RecepcionMercancia> recepciones = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }
        
        return recepciones;
    } catch (SQLException e) {
        logger.error("Error al buscar recepciones por proveedor", e);
        throw new DatabaseException("Error al buscar recepciones: " + e.getMessage());
    }
}
    
    @Override
    public List<RecepcionMercancia> findByOrdenCompra(String numeroOrdenCompra) 
        throws DatabaseException {
    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.numero_orden_compra = ? " +
                "ORDER BY rm.fecha_recepcion DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, numeroOrdenCompra);
        List<RecepcionMercancia> recepciones = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }
        
        return recepciones;
    } catch (SQLException e) {
        logger.error("Error al buscar recepciones por orden de compra", e);
        throw new DatabaseException("Error al buscar recepciones: " + e.getMessage());
    }
}
    
    @Override
    public boolean existsByNumeroRecepcion(String numeroRecepcion) throws DatabaseException {
    String sql = "SELECT COUNT(*) FROM recepciones_mercancia WHERE numero_recepcion = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, numeroRecepcion);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    } catch (SQLException e) {
        logger.error("Error al verificar existencia de número de recepción", e);
        throw new DatabaseException("Error al verificar número de recepción: " + e.getMessage());
    }
        }
    
    
    @Override
    public List<RecepcionMercancia> findVerificadasPendientes() throws DatabaseException {
    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.estado = 'VERIFICADO' " +
                "ORDER BY rm.fecha_recepcion ASC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        List<RecepcionMercancia> recepciones = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }
        return recepciones;
    } catch (SQLException e) {
        logger.error("Error al buscar recepciones verificadas pendientes", e);
        throw new DatabaseException("Error al buscar recepciones: " + e.getMessage());
    }
}
    
    
    @Override
    public List<RecepcionMercancia> findPendientesByProveedor(Long proveedorId) 
        throws DatabaseException {
    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.proveedor_id = ? AND rm.estado = 'PENDIENTE' " +
                "ORDER BY rm.fecha_recepcion ASC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setLong(1, proveedorId);
        List<RecepcionMercancia> recepciones = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }
        return recepciones;
    } catch (SQLException e) {
        logger.error("Error al buscar recepciones pendientes por proveedor", e);
        throw new DatabaseException("Error al buscar recepciones: " + e.getMessage());
    }
}
    
   @Override
    public List<RecepcionMercancia> findWithDiscrepancias() throws DatabaseException {
    String sql = "SELECT DISTINCT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "JOIN detalle_recepcion dr ON rm.id = dr.recepcion_id " +
                "WHERE dr.cantidad_recibida IS NOT NULL " +
                "AND dr.cantidad_recibida != dr.cantidad_esperada " +
                "ORDER BY rm.fecha_recepcion DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        List<RecepcionMercancia> recepciones = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }
        return recepciones;
    } catch (SQLException e) {
        logger.error("Error al buscar recepciones con discrepancias", e);
        throw new DatabaseException("Error al buscar recepciones: " + e.getMessage());
    }
} 
    
    
    @Override
    public List<RecepcionMercancia.EstadoRecepcion> getHistorialEstados(Long recepcionId) 
        throws DatabaseException {
    String sql = "SELECT estado_anterior, estado_nuevo, fecha " +
                "FROM recepcion_historial " +
                "WHERE recepcion_id = ? " +
                "ORDER BY fecha ASC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setLong(1, recepcionId);
        List<RecepcionMercancia.EstadoRecepcion> historial = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                historial.add(RecepcionMercancia.EstadoRecepcion.valueOf(
                    rs.getString("estado_nuevo")));
            }
        }
        return historial;
    } catch (SQLException e) {
        logger.error("Error al obtener historial de estados", e);
        throw new DatabaseException("Error al obtener historial: " + e.getMessage());
    }
}
    
    
    @Override
    public void updateEstado(Long recepcionId, RecepcionMercancia.EstadoRecepcion estado, String observaciones) 
        throws DatabaseException {
    Connection conn = null;
    try {
        conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);

        // Obtener estado actual
        String selectSql = "SELECT estado FROM recepciones_mercancia WHERE id = ? FOR UPDATE";
        RecepcionMercancia.EstadoRecepcion estadoAnterior;
        
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setLong(1, recepcionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseException("Recepción no encontrada");
                }
                estadoAnterior = RecepcionMercancia.EstadoRecepcion.valueOf(rs.getString("estado"));
            }
        }

        // Registrar cambio en historial
        String insertHistorialSql = "INSERT INTO recepcion_historial " +
                                  "(recepcion_id, estado_anterior, estado_nuevo, observaciones, fecha) " +
                                  "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = conn.prepareStatement(insertHistorialSql)) {
            stmt.setLong(1, recepcionId);
            stmt.setString(2, estadoAnterior.name());
            stmt.setString(3, estado.name());
            stmt.setString(4, observaciones);
            stmt.executeUpdate();
        }

        // Actualizar estado
        String updateSql = "UPDATE recepciones_mercancia " +
                         "SET estado = ?, observaciones = ?, fecha_modificacion = CURRENT_TIMESTAMP " +
                         "WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setString(1, estado.name());
            stmt.setString(2, observaciones);
            stmt.setLong(3, recepcionId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("No se pudo actualizar el estado de la recepción");
            }
        }

        conn.commit();
    } catch (Exception e) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.error("Error al realizar rollback", ex);
            }
        }
        throw new DatabaseException("Error al actualizar estado: " + e.getMessage());
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logger.error("Error al cerrar conexión", e);
            }
        }
    }
}
    
    
    @Override
    public void updateDetalle(DetalleRecepcion detalle) throws DatabaseException {
        logger.debug("Iniciando actualización de detalle. ID: {}, Estado: {}", 
        detalle.getId(), 
        detalle.getEstado());

    String sql = "UPDATE detalle_recepcion SET " +
                "cantidad_recibida = ?, " +
                "estado = ?, " +
                "observaciones = ? " +
                "WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        // Establecer cantidad recibida
        stmt.setInt(1, detalle.getCantidadRecibida());

        // Establecer estado (usar toString del enum que devuelve el name())
        stmt.setString(2, detalle.getEstado().toString());

        // Establecer observaciones (puede ser null)
        if (detalle.getObservaciones() != null && !detalle.getObservaciones().trim().isEmpty()) {
            stmt.setString(3, detalle.getObservaciones().trim());
        } else {
            stmt.setNull(3, Types.VARCHAR);
        }

        // Establecer ID
        stmt.setLong(4, detalle.getId());

        // Ejecutar la actualización
        int affectedRows = stmt.executeUpdate();
        
        // Verificar resultado
        if (affectedRows == 0) {
            throw new DatabaseException("No se encontró el detalle con ID: " + detalle.getId());
        }

        logger.debug("Detalle actualizado exitosamente. ID: {}", detalle.getId());

    } catch (SQLException e) {
        logger.error("Error SQL al actualizar detalle: {}", e.getMessage());
        throw new DatabaseException("Error al actualizar detalle: " + e.getMessage());
    }
}
    
    
    
    
    
    
    
    @Override
    public void deleteDetalle(Long detalleId) throws DatabaseException {
    Connection conn = null;
    try {
        conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);

        // Obtener ID de la recepción antes de eliminar
        Long recepcionId;
        String selectSql = "SELECT recepcion_id FROM detalle_recepcion WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setLong(1, detalleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseException("Detalle no encontrado");
                }
                recepcionId = rs.getLong("recepcion_id");
            }
        }

        // Eliminar el detalle
        String deleteSql = "DELETE FROM detalle_recepcion WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setLong(1, detalleId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("No se pudo eliminar el detalle");
            }
        }

        // Actualizar el total de la recepción
        actualizarTotalRecepcion(recepcionId, conn);

        conn.commit();
    } catch (Exception e) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.error("Error al realizar rollback", ex);
            }
        }
        throw new DatabaseException("Error al eliminar detalle: " + e.getMessage());
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logger.error("Error al cerrar conexión", e);
            }
        }
    }
}
    
    @Override
    public int getTotalRecibido(Long productoId, LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException {
    String sql = "SELECT COALESCE(SUM(dr.cantidad_recibida), 0) as total " +
                "FROM detalle_recepcion dr " +
                "JOIN recepciones_mercancia rm ON dr.recepcion_id = rm.id " +
                "WHERE dr.producto_id = ? " +
                "AND rm.fecha_recepcion BETWEEN ? AND ? " +
                "AND rm.estado = 'ACEPTADO'";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setLong(1, productoId);
        stmt.setTimestamp(2, Timestamp.valueOf(fechaInicio));
        stmt.setTimestamp(3, Timestamp.valueOf(fechaFin));

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    } catch (SQLException e) {
        logger.error("Error al obtener total recibido", e);
        throw new DatabaseException("Error al obtener total recibido: " + e.getMessage());
    }
}
    
    // Estadísticas generales
    private Map<String, Object> getEstadisticasGenerales(Connection conn, 
        LocalDateTime fechaInicio, LocalDateTime fechaFin) throws SQLException {
    
    String sql = "SELECT " +
                "COUNT(*) as total_recepciones, " +
                "COUNT(DISTINCT proveedor_id) as total_proveedores, " +
                "SUM(CASE WHEN estado = 'ACEPTADO' THEN total ELSE 0 END) as monto_total_aceptado, " +
                "AVG(CASE WHEN estado = 'ACEPTADO' THEN total ELSE NULL END) as promedio_por_recepcion, " +
                "MIN(total) as monto_minimo, " +
                "MAX(total) as monto_maximo " +
                "FROM recepciones_mercancia " +
                "WHERE fecha_recepcion BETWEEN ? AND ?";

    Map<String, Object> estadisticas = new HashMap<>();
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
        stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                estadisticas.put("totalRecepciones", rs.getInt("total_recepciones"));
                estadisticas.put("totalProveedores", rs.getInt("total_proveedores"));
                estadisticas.put("montoTotalAceptado", rs.getDouble("monto_total_aceptado"));
                estadisticas.put("promedioPorRecepcion", rs.getDouble("promedio_por_recepcion"));
                estadisticas.put("montoMinimo", rs.getDouble("monto_minimo"));
                estadisticas.put("montoMaximo", rs.getDouble("monto_maximo"));
            }
        }
    }
    
    return estadisticas;
}

    // Estadísticas por estado
    private Map<String, Object> getEstadisticasPorEstado(Connection conn, 
        LocalDateTime fechaInicio, LocalDateTime fechaFin) throws SQLException {
    
    String sql = "SELECT estado, " +
                "COUNT(*) as cantidad, " +
                "SUM(total) as monto_total, " +
                "AVG(total) as promedio " +
                "FROM recepciones_mercancia " +
                "WHERE fecha_recepcion BETWEEN ? AND ? " +
                "GROUP BY estado";

    Map<String, Object> estadisticas = new HashMap<>();
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
        stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> estadoInfo = new HashMap<>();
                estadoInfo.put("cantidad", rs.getInt("cantidad"));
                estadoInfo.put("montoTotal", rs.getDouble("monto_total"));
                estadoInfo.put("promedio", rs.getDouble("promedio"));
                
                estadisticas.put(rs.getString("estado"), estadoInfo);
            }
        }
    }
    
    return estadisticas;
}

    // Estadísticas por proveedor
    private List<Map<String, Object>> getEstadisticasPorProveedor(Connection conn, 
        LocalDateTime fechaInicio, LocalDateTime fechaFin) throws SQLException {
    
    String sql = "SELECT p.razon_social, " +
                "COUNT(*) as total_recepciones, " +
                "SUM(rm.total) as monto_total, " +
                "AVG(rm.total) as promedio, " +
                "COUNT(CASE WHEN rm.estado = 'RECHAZADO' THEN 1 END) as rechazos " +
                "FROM recepciones_mercancia rm " +
                "JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.fecha_recepcion BETWEEN ? AND ? " +
                "GROUP BY p.id, p.razon_social " +
                "ORDER BY monto_total DESC";

    List<Map<String, Object>> estadisticas = new ArrayList<>();
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
        stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> proveedorInfo = new HashMap<>();
                proveedorInfo.put("razonSocial", rs.getString("razon_social"));
                proveedorInfo.put("totalRecepciones", rs.getInt("total_recepciones"));
                proveedorInfo.put("montoTotal", rs.getDouble("monto_total"));
                proveedorInfo.put("promedio", rs.getDouble("promedio"));
                proveedorInfo.put("rechazos", rs.getInt("rechazos"));
                
                estadisticas.add(proveedorInfo);
            }
        }
    }
    
    return estadisticas;
}

    // Productos más recibidos
    private List<Map<String, Object>> getProductosMasRecibidos(Connection conn, 
        LocalDateTime fechaInicio, LocalDateTime fechaFin) throws SQLException {
    
    String sql = "SELECT p.codigo, p.nombre, " +
                "SUM(dr.cantidad_recibida) as cantidad_total, " +
                "COUNT(DISTINCT rm.id) as total_recepciones, " +
                "SUM(dr.cantidad_recibida * dr.precio_unitario) as monto_total " +
                "FROM detalle_recepcion dr " +
                "JOIN recepciones_mercancia rm ON dr.recepcion_id = rm.id " +
                "JOIN productos p ON dr.producto_id = p.id " +
                "WHERE rm.fecha_recepcion BETWEEN ? AND ? " +
                "AND rm.estado = 'ACEPTADO' " +
                "GROUP BY p.id, p.codigo, p.nombre " +
                "ORDER BY cantidad_total DESC " +
                "LIMIT 10";

    List<Map<String, Object>> productos = new ArrayList<>();
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
        stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("codigo", rs.getString("codigo"));
                producto.put("nombre", rs.getString("nombre"));
                producto.put("cantidadTotal", rs.getInt("cantidad_total"));
                producto.put("totalRecepciones", rs.getInt("total_recepciones"));
                producto.put("montoTotal", rs.getDouble("monto_total"));
                
                productos.add(producto);
            }
        }
    }
    
    return productos;
}

    // Análisis de discrepancias
    private Map<String, Object> getAnalisisDiscrepancias(Connection conn, 
        LocalDateTime fechaInicio, LocalDateTime fechaFin) throws SQLException {
    
    String sql = "SELECT " +
                "COUNT(*) as total_detalles, " +
                "COUNT(CASE WHEN cantidad_recibida <> cantidad_esperada THEN 1 END) " +
                "   as total_discrepancias, " +
                "AVG(CASE WHEN cantidad_recibida <> cantidad_esperada " +
                "   THEN ABS(cantidad_recibida - cantidad_esperada) END) as promedio_diferencia, " +
                "SUM(CASE WHEN cantidad_recibida < cantidad_esperada " +
                "   THEN (cantidad_esperada - cantidad_recibida) * precio_unitario END) " +
                "   as perdida_total " +
                "FROM detalle_recepcion dr " +
                "JOIN recepciones_mercancia rm ON dr.recepcion_id = rm.id " +
                "WHERE rm.fecha_recepcion BETWEEN ? AND ? " +
                "AND rm.estado = 'ACEPTADO'";

    Map<String, Object> discrepancias = new HashMap<>();
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
        stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                discrepancias.put("totalDetalles", rs.getInt("total_detalles"));
                discrepancias.put("totalDiscrepancias", rs.getInt("total_discrepancias"));
                discrepancias.put("promedioDiferencia", rs.getDouble("promedio_diferencia"));
                discrepancias.put("perdidaTotal", rs.getDouble("perdida_total"));
                
                // Calcular porcentaje de discrepancias
                int totalDetalles = rs.getInt("total_detalles");
                int totalDiscrepancias = rs.getInt("total_discrepancias");
                double porcentajeDiscrepancias = totalDetalles > 0 ? 
                    (totalDiscrepancias * 100.0) / totalDetalles : 0;
                discrepancias.put("porcentajeDiscrepancias", porcentajeDiscrepancias);
            }
        }
    }
    
    return discrepancias;
}
    
    
    
    
    // Método auxiliar para actualizar el total de la recepción
    private void actualizarTotalRecepcion(Long recepcionId, Connection conn) throws SQLException {
    String sql = "UPDATE recepciones_mercancia rm " +
                "SET total = (SELECT COALESCE(SUM(cantidad_esperada * precio_unitario), 0) " +
                "            FROM detalle_recepcion " +
                "            WHERE recepcion_id = ?) " +
                "WHERE rm.id = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setLong(1, recepcionId);
        stmt.setLong(2, recepcionId);
        stmt.executeUpdate();
    }
    }
    
    
    // Método auxiliar para verificar si un número de recepción ya existe
    private boolean existeNumeroRecepcion(String numeroRecepcion, Connection conn) 
        throws SQLException {
    String sql = "SELECT COUNT(*) FROM recepciones_mercancia WHERE numero_recepcion = ?";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, numeroRecepcion);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
    }
    return false;
}

    // Método para obtener el último número de recepción generado
    public String getUltimoNumeroRecepcion() throws DatabaseException {
    String sql = "SELECT numero_recepcion FROM recepciones_mercancia " +
                "ORDER BY fecha_recepcion DESC LIMIT 1";
                
    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        if (rs.next()) {
            return rs.getString("numero_recepcion");
        }
        return null;
    } catch (SQLException e) {
        logger.error("Error al obtener último número de recepción", e);
        throw new DatabaseException("Error al obtener último número de recepción: " + e.getMessage());
    }
}
    
    private void validarDetalle(DetalleRecepcion detalle) throws ValidationException {
    List<String> errores = new ArrayList<>();

    if (detalle.getRecepcion() == null || detalle.getRecepcion().getId() == null) {
        errores.add("La recepción es requerida");
    }

    if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
        errores.add("El producto es requerido");
    }

    if (detalle.getCantidadEsperada() <= 0) {
        errores.add("La cantidad esperada debe ser mayor a cero");
    }

    if (detalle.getPrecioUnitario() <= 0) {
        errores.add("El precio unitario debe ser mayor a cero");
    }

    if (!errores.isEmpty()) {
        throw new ValidationException("validación", String.join(", ", errores));
    }
}

    // Método para verificar si existe duplicado
    private boolean existeDetalleDuplicado(DetalleRecepcion detalle, Connection conn) 
        throws SQLException {
    String sql = "SELECT COUNT(*) FROM detalle_recepcion " +
                "WHERE recepcion_id = ? AND producto_id = ? AND id != ?";
                
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setLong(1, detalle.getRecepcion().getId());
        stmt.setLong(2, detalle.getProducto().getId());
        stmt.setLong(3, detalle.getId() != null ? detalle.getId() : -1);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
    }
    return false;
}

    // Método para obtener totales de un detalle
    public Map<String, Object> obtenerTotalesDetalle(Long detalleId) throws DatabaseException {
    String sql = "SELECT " +
                "d.cantidad_esperada * d.precio_unitario as total_esperado, " +
                "COALESCE(d.cantidad_recibida * d.precio_unitario, 0) as total_recibido, " +
                "CASE " +
                "  WHEN d.cantidad_recibida IS NOT NULL " +
                "  THEN ABS(d.cantidad_esperada - d.cantidad_recibida) * d.precio_unitario " +
                "  ELSE 0 " +
                "END as diferencia " +
                "FROM detalle_recepcion d " +
                "WHERE d.id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setLong(1, detalleId);
        
        try (ResultSet rs = stmt.executeQuery()) {
            Map<String, Object> totales = new HashMap<>();
            if (rs.next()) {
                totales.put("totalEsperado", rs.getDouble("total_esperado"));
                totales.put("totalRecibido", rs.getDouble("total_recibido"));
                totales.put("diferencia", rs.getDouble("diferencia"));
            }
            return totales;
        }
    } catch (SQLException e) {
        logger.error("Error al obtener totales del detalle", e);
        throw new DatabaseException("Error al obtener totales: " + e.getMessage());
    }
}
    
    
   

    private RecepcionMercancia mapResultSetToRecepcion(ResultSet rs) throws SQLException {
         RecepcionMercancia recepcion = new RecepcionMercancia();
    recepcion.setId(rs.getLong("id"));
    recepcion.setNumeroRecepcion(rs.getString("numero_recepcion"));
    recepcion.setFechaRecepcion(rs.getTimestamp("fecha_recepcion").toLocalDateTime());
    recepcion.setProveedor(rs.getString("proveedor_id"));
    recepcion.setNumeroOrdenCompra(rs.getString("numero_orden_compra"));
    recepcion.setEstado(EstadoRecepcion.valueOf(rs.getString("estado")));
    recepcion.setObservaciones(rs.getString("observaciones"));
    
    // Guardar también el nombre del proveedor si está disponible
    String proveedorNombre = rs.getString("proveedor_nombre");
    if (proveedorNombre != null) {
        recepcion.setProveedorNombre(proveedorNombre);  // Necesitarás agregar este campo a la clase RecepcionMercancia
    }
    
    return recepcion;
    }

    // Métodos específicos para la gestión de recepciones
    public List<DetalleRecepcion> findDetallesByRecepcionId(Long recepcionId) throws DatabaseException {
        String sql = "SELECT dr.*, " +
                "p.id as producto_id, p.codigo as producto_codigo, " +
                "p.nombre as producto_nombre, p.unidad_medida, " +
                "p.precio_unitario as producto_precio " +
                "FROM detalle_recepcion dr " +
                "INNER JOIN productos p ON dr.producto_id = p.id " +
                "WHERE dr.recepcion_id = ? " +
                "ORDER BY p.codigo";
                
    List<DetalleRecepcion> detalles = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
         
        stmt.setLong(1, recepcionId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                DetalleRecepcion detalle = new DetalleRecepcion();
                detalle.setId(rs.getLong("id"));
                
                // Crear y configurar el producto
                Producto producto = new Producto();
                producto.setId(rs.getLong("producto_id"));
                producto.setCodigo(rs.getString("producto_codigo"));
                producto.setNombre(rs.getString("producto_nombre"));
                producto.setUnidadMedida(rs.getString("unidad_medida"));
                producto.setPrecioUnitario(rs.getDouble("precio_unitario"));
                
                detalle.setProducto(producto);
                detalle.setCantidadEsperada(rs.getInt("cantidad_esperada"));
                
                // Manejar cantidad_recibida que puede ser null
                Object cantidadRecibida = rs.getObject("cantidad_recibida");
                if (cantidadRecibida != null) {
                    detalle.setCantidadRecibida(rs.getInt("cantidad_recibida"));
                }
                
                detalle.setPrecioUnitario(rs.getDouble("precio_unitario"));
                
                // Obtener observaciones (puede ser null)
                String observaciones = rs.getString("observaciones");
                if (observaciones != null) {
                    detalle.setObservaciones(observaciones);
                }
                
                // Establecer estado
                String estadoStr = rs.getString("estado");
                if (estadoStr != null) {
                    detalle.setEstado(DetalleRecepcion.EstadoDetalle.valueOf(estadoStr));
                }
                
                detalles.add(detalle);
                logger.debug("Detalle cargado - ID: {}, Producto: {}, Cantidad Esp: {}, Cantidad Rec: {}", 
                    detalle.getId(), 
                    producto.getCodigo(), 
                    detalle.getCantidadEsperada(),
                    detalle.getCantidadRecibida());
            }
        }
        
        return detalles;
    } catch (SQLException e) {
        logger.error("Error al obtener detalles de recepción: {}", e.getMessage());
        throw new DatabaseException("Error al obtener detalles: " + e.getMessage());
    }
    }

    private DetalleRecepcion mapResultSetToDetalle(ResultSet rs) throws SQLException {
        DetalleRecepcion detalle = new DetalleRecepcion();
        detalle.setId(rs.getLong("id"));
        detalle.setCantidadEsperada(rs.getInt("cantidad_esperada"));
        detalle.setCantidadRecibida(rs.getInt("cantidad_recibida"));
        detalle.setPrecioUnitario(rs.getDouble("precio_unitario"));
        detalle.setEstado(DetalleRecepcion.EstadoDetalle.valueOf(rs.getString("estado")));
        detalle.setObservaciones(rs.getString("observaciones"));
        
        return detalle;
    }
    
        // Método adicional para búsqueda más flexible con fechas opcionales
    public List<RecepcionMercancia> findByFechasOptional(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException {
    logger.debug("Buscando recepciones con fechas opcionales: inicio={}, fin={}", fechaInicio, fechaFin);
    
    StringBuilder sql = new StringBuilder(
        "SELECT rm.*, u.username as responsable_username, " +
        "p.razon_social as proveedor_nombre " +
        "FROM recepciones_mercancia rm " +
        "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
        "LEFT JOIN proveedores p ON rm.proveedor_id = p.id WHERE 1=1");
    
    List<Object> params = new ArrayList<>();

    if (fechaInicio != null) {
        sql.append(" AND rm.fecha_recepcion >= ?");
        params.add(Timestamp.valueOf(fechaInicio));
    }

    if (fechaFin != null) {
        sql.append(" AND rm.fecha_recepcion <= ?");
        params.add(Timestamp.valueOf(fechaFin));
    }

    sql.append(" ORDER BY rm.fecha_recepcion DESC");

    List<RecepcionMercancia> recepciones = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

        // Establecer parámetros
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }

        logger.debug("Se encontraron {} recepciones", recepciones.size());
        return recepciones;

    } catch (SQLException e) {
        logger.error("Error al buscar recepciones por fechas", e);
        throw new DatabaseException("Error al buscar recepciones por fechas: " + e.getMessage());
    }
}
    
    
    public List<RecepcionMercancia> findByEstados(List<RecepcionMercancia.EstadoRecepcion> estados) 
        throws DatabaseException {
    logger.debug("Buscando recepciones por estados: {}", estados);
    
    if (estados == null || estados.isEmpty()) {
        return new ArrayList<>();
    }

    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.estado IN (" + 
                String.join(",", Collections.nCopies(estados.size(), "?")) + 
                ") ORDER BY rm.fecha_recepcion DESC";

    List<RecepcionMercancia> recepciones = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        // Establecer parámetros para cada estado
        for (int i = 0; i < estados.size(); i++) {
            stmt.setString(i + 1, estados.get(i).name());
        }

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }

        logger.debug("Se encontraron {} recepciones para los estados {}", 
            recepciones.size(), estados);
        return recepciones;

    } catch (SQLException e) {
        logger.error("Error al buscar recepciones por estados", e);
        throw new DatabaseException("Error al buscar recepciones por estados: " + e.getMessage());
    }
}

    // Método para obtener conteo por estado
    public Map<RecepcionMercancia.EstadoRecepcion, Integer> getConteosPorEstado() 
        throws DatabaseException {
    logger.debug("Obteniendo conteos por estado");
    
    String sql = "SELECT estado, COUNT(*) as conteo " +
                "FROM recepciones_mercancia " +
                "GROUP BY estado";

    Map<RecepcionMercancia.EstadoRecepcion, Integer> conteos = new EnumMap<>(
        RecepcionMercancia.EstadoRecepcion.class);

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            RecepcionMercancia.EstadoRecepcion estado = 
                RecepcionMercancia.EstadoRecepcion.valueOf(rs.getString("estado"));
            conteos.put(estado, rs.getInt("conteo"));
        }

        // Asegurar que todos los estados tengan un valor
        for (RecepcionMercancia.EstadoRecepcion estado : 
             RecepcionMercancia.EstadoRecepcion.values()) {
            conteos.putIfAbsent(estado, 0);
        }

        return conteos;

    } catch (SQLException e) {
        logger.error("Error al obtener conteos por estado", e);
        throw new DatabaseException("Error al obtener conteos por estado: " + e.getMessage());
    }
}

    // Método para búsqueda combinada por estado y fechas
    public List<RecepcionMercancia> findByEstadoYFechas(
        RecepcionMercancia.EstadoRecepcion estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin) throws DatabaseException {
    
    logger.debug("Buscando recepciones por estado {} entre {} y {}", 
        estado, fechaInicio, fechaFin);
    
    String sql = "SELECT rm.*, u.username as responsable_username, " +
                "p.razon_social as proveedor_nombre " +
                "FROM recepciones_mercancia rm " +
                "LEFT JOIN usuarios u ON rm.usuario_id = u.id " +
                "LEFT JOIN proveedores p ON rm.proveedor_id = p.id " +
                "WHERE rm.estado = ? " +
                "AND rm.fecha_recepcion BETWEEN ? AND ? " +
                "ORDER BY rm.fecha_recepcion DESC";

    List<RecepcionMercancia> recepciones = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, estado.name());
        stmt.setTimestamp(2, Timestamp.valueOf(fechaInicio));
        stmt.setTimestamp(3, Timestamp.valueOf(fechaFin));

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recepciones.add(mapResultSetToRecepcion(rs));
            }
        }

        return recepciones;

    } catch (SQLException e) {
        logger.error("Error al buscar recepciones por estado y fechas", e);
        throw new DatabaseException("Error al buscar recepciones: " + e.getMessage());
    }
}
              
    
    // Método adicional para búsqueda por fecha específica
    public List<RecepcionMercancia> findByFechaExacta(LocalDateTime fecha) throws DatabaseException {
    logger.debug("Buscando recepciones para la fecha {}", fecha);
    
    // Calcular inicio y fin del día
    LocalDateTime inicioDia = fecha.toLocalDate().atStartOfDay();
    LocalDateTime finDia = inicioDia.plusDays(1).minusNanos(1);
    
    return findByFechas(inicioDia, finDia);
    }
    
    // Método para obtener estadísticas por fechas
    public Map<String, Object> getEstadisticasPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws DatabaseException {
    logger.debug("Obteniendo estadísticas entre {} y {}", fechaInicio, fechaFin);
    
    String sql = "SELECT " +
                "COUNT(*) as total_recepciones, " +
                "SUM(CASE WHEN estado = 'ACEPTADO' THEN 1 ELSE 0 END) as aceptadas, " +
                "SUM(CASE WHEN estado = 'RECHAZADO' THEN 1 ELSE 0 END) as rechazadas, " +
                "SUM(CASE WHEN estado = 'PENDIENTE' THEN 1 ELSE 0 END) as pendientes, " +
                "SUM(CASE WHEN estado = 'VERIFICADO' THEN 1 ELSE 0 END) as verificadas " +
                "FROM recepciones_mercancia " +
                "WHERE fecha_recepcion BETWEEN ? AND ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
        stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));

        try (ResultSet rs = stmt.executeQuery()) {
            Map<String, Object> estadisticas = new HashMap<>();
            if (rs.next()) {
                estadisticas.put("totalRecepciones", rs.getInt("total_recepciones"));
                estadisticas.put("recepcionesAceptadas", rs.getInt("aceptadas"));
                estadisticas.put("recepcionesRechazadas", rs.getInt("rechazadas"));
                estadisticas.put("recepcionesPendientes", rs.getInt("pendientes"));
                estadisticas.put("recepcionesVerificadas", rs.getInt("verificadas"));
            }
            return estadisticas;
        }

    } catch (SQLException e) {
        logger.error("Error al obtener estadísticas por fechas", e);
        throw new DatabaseException("Error al obtener estadísticas por fechas: " + e.getMessage());
    }
        }
    
     @Override
    public void eliminar(Long id) throws DatabaseException {
        try {
            deleteWithTransaction(id);
        } catch (Exception e) {
            logger.error("Error al eliminar recepción", e);
            throw new DatabaseException("Error al eliminar recepción: " + e.getMessage());
        }
    }
    
     
    

    

}
