
package com.rintisa.dao.impl;

import com.rintisa.config.DatabaseConfig;
import com.rintisa.dao.interfaces.IPermisosPantallaDao;
import com.rintisa.model.PermisosPantalla;
import com.rintisa.model.Pantalla;
import com.rintisa.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermisosPantallaDao implements IPermisosPantallaDao {
    private static final Logger logger = LoggerFactory.getLogger(PermisosPantallaDao.class);

    @Override
    public PermisosPantalla save(PermisosPantalla permiso) throws DatabaseException {
        String sql = "INSERT INTO permisos_pantalla (rol_nombre, pantalla, acceso, edicion, eliminacion, " +
                    "fecha_modificacion, usuario_modificacion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, permiso.getRolNombre());
            stmt.setString(2, permiso.getPantalla().name());
            stmt.setBoolean(3, permiso.isAcceso());
            stmt.setBoolean(4, permiso.isEdicion());
            stmt.setBoolean(5, permiso.isEliminacion());
            stmt.setTimestamp(6, Timestamp.valueOf(permiso.getFechaModificacion()));
            stmt.setLong(7, permiso.getUsuarioModificacion());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("La creación del permiso falló, no se insertaron registros.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    permiso.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("La creación del permiso falló, no se obtuvo el ID.");
                }
            }

            return permiso;
        } catch (SQLException e) {
            logger.error("Error al guardar permiso de pantalla", e);
            throw new DatabaseException("Error al guardar permiso: " + e.getMessage());
        }
    }

    @Override
    public Optional<PermisosPantalla> findById(Long id) throws DatabaseException {
        String sql = "SELECT * FROM permisos_pantalla WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPermisosPantalla(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error al buscar permiso por ID", e);
            throw new DatabaseException("Error al buscar permiso: " + e.getMessage());
        }
    }

    @Override
    public List<PermisosPantalla> findAll() throws DatabaseException {
        String sql = "SELECT * FROM permisos_pantalla ORDER BY rol_nombre, pantalla";
        List<PermisosPantalla> permisos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                permisos.add(mapResultSetToPermisosPantalla(rs));
            }
            return permisos;
        } catch (SQLException e) {
            logger.error("Error al obtener todos los permisos", e);
            throw new DatabaseException("Error al obtener permisos: " + e.getMessage());
        }
    }

    @Override
    public void update(PermisosPantalla permiso) throws DatabaseException {
        String sql = "UPDATE permisos_pantalla SET acceso = ?, edicion = ?, eliminacion = ?, " +
                    "fecha_modificacion = ?, usuario_modificacion = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, permiso.isAcceso());
            stmt.setBoolean(2, permiso.isEdicion());
            stmt.setBoolean(3, permiso.isEliminacion());
            stmt.setTimestamp(4, Timestamp.valueOf(permiso.getFechaModificacion()));
            stmt.setLong(5, permiso.getUsuarioModificacion());
            stmt.setLong(6, permiso.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("La actualización del permiso falló, no se encontró el registro.");
            }
        } catch (SQLException e) {
            logger.error("Error al actualizar permiso", e);
            throw new DatabaseException("Error al actualizar permiso: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) throws DatabaseException {
        String sql = "DELETE FROM permisos_pantalla WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("La eliminación del permiso falló, no se encontró el registro.");
            }
        } catch (SQLException e) {
            logger.error("Error al eliminar permiso", e);
            throw new DatabaseException("Error al eliminar permiso: " + e.getMessage());
        }
    }

    @Override
    public List<PermisosPantalla> findByRol(String rolNombre) throws DatabaseException {
        String sql = "SELECT * FROM permisos_pantalla WHERE rol_nombre = ? ORDER BY pantalla";
        List<PermisosPantalla> permisos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, rolNombre);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapResultSetToPermisosPantalla(rs));
                }
            }
            return permisos;
        } catch (SQLException e) {
            logger.error("Error al buscar permisos por rol", e);
            throw new DatabaseException("Error al buscar permisos: " + e.getMessage());
        }
    }

    @Override
    public PermisosPantalla findByRolAndPantalla(String rolNombre, Pantalla pantalla) 
            throws DatabaseException {
        String sql = "SELECT * FROM permisos_pantalla WHERE rol_nombre = ? AND pantalla = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, rolNombre);
            stmt.setString(2, pantalla.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPermisosPantalla(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            logger.error("Error al buscar permiso específico", e);
            throw new DatabaseException("Error al buscar permiso: " + e.getMessage());
        }
    }

    @Override
    public void updatePermisosByRol(String rolNombre, List<PermisosPantalla> permisos) 
            throws DatabaseException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Primero eliminar permisos existentes
                String deleteSql = "DELETE FROM permisos_pantalla WHERE rol_nombre = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setString(1, rolNombre);
                    deleteStmt.executeUpdate();
                }

                // Luego insertar los nuevos permisos
                String insertSql = "INSERT INTO permisos_pantalla (rol_nombre, pantalla, acceso, " +
                                 "edicion, eliminacion, fecha_modificacion, usuario_modificacion) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (PermisosPantalla permiso : permisos) {
                        insertStmt.setString(1, rolNombre);
                        insertStmt.setString(2, permiso.getPantalla().name());
                        insertStmt.setBoolean(3, permiso.isAcceso());
                        insertStmt.setBoolean(4, permiso.isEdicion());
                        insertStmt.setBoolean(5, permiso.isEliminacion());
                        insertStmt.setTimestamp(6, Timestamp.valueOf(permiso.getFechaModificacion()));
                        insertStmt.setLong(7, permiso.getUsuarioModificacion());
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error al actualizar permisos por rol", e);
            throw new DatabaseException("Error al actualizar permisos: " + e.getMessage());
        }
    }

    @Override
    public boolean existePermiso(String rolNombre, Pantalla pantalla) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM permisos_pantalla WHERE rol_nombre = ? AND pantalla = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, rolNombre);
            stmt.setString(2, pantalla.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de permiso", e);
            throw new DatabaseException("Error al verificar permiso: " + e.getMessage());
        }
    }

    private PermisosPantalla mapResultSetToPermisosPantalla(ResultSet rs) throws SQLException {
        PermisosPantalla permiso = new PermisosPantalla();
        permiso.setId(rs.getLong("id"));
        permiso.setRolNombre(rs.getString("rol_nombre"));
        permiso.setPantalla(Pantalla.valueOf(rs.getString("pantalla")));
        permiso.setAcceso(rs.getBoolean("acceso"));
        permiso.setEdicion(rs.getBoolean("edicion"));
        permiso.setEliminacion(rs.getBoolean("eliminacion"));
        permiso.setFechaModificacion(rs.getTimestamp("fecha_modificacion").toLocalDateTime());
        permiso.setUsuarioModificacion(rs.getLong("usuario_modificacion"));
        return permiso;
    }
}
