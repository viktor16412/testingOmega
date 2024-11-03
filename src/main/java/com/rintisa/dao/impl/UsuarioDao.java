package com.rintisa.dao.impl;

import com.rintisa.config.DatabaseConfig;
import com.rintisa.dao.interfaces.IUsuarioDao;
import com.rintisa.exception.DatabaseException;
import com.rintisa.model.Usuario;
import com.rintisa.model.Rol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDao implements IUsuarioDao {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDao.class);

    @Override
    public Usuario save(Usuario usuario) throws DatabaseException {
        String sql = "INSERT INTO usuarios (username, password, nombre, apellido, email, activo, fecha_creacion, rol_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getPassword());
            stmt.setString(3, usuario.getNombre());
            stmt.setString(4, usuario.getApellido());
            stmt.setString(5, usuario.getEmail());
            stmt.setBoolean(6, usuario.isActivo());
            stmt.setTimestamp(7, Timestamp.valueOf(usuario.getFechaCreacion()));
            stmt.setLong(8, usuario.getRol().getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("Creating user failed, no ID obtained.");
                }
            }
            
            return usuario;
        } catch (SQLException e) {
            logger.error("Error saving user", e);
            throw new DatabaseException("Error saving user: " + e.getMessage());
        }
    }

    @Override
    public Optional<Usuario> findById(Long id) throws DatabaseException {
        String sql = "SELECT u.*, r.id as rol_id, r.nombre as rol_nombre, r.descripcion as rol_descripcion " +
                    "FROM usuarios u " +
                    "LEFT JOIN roles r ON u.rol_id = r.id " +
                    "WHERE u.id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by ID", e);
            throw new DatabaseException("Error finding user: " + e.getMessage());
        }
    }

    @Override
    public List<Usuario> findAll() throws DatabaseException {
        String sql = "SELECT u.*, r.id as rol_id, r.nombre as rol_nombre, r.descripcion as rol_descripcion " +
                    "FROM usuarios u " +
                    "LEFT JOIN roles r ON u.rol_id = r.id";
        
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
            
            return usuarios;
        } catch (SQLException e) {
            logger.error("Error finding all users", e);
            throw new DatabaseException("Error finding all users: " + e.getMessage());
        }
    }

    @Override
    public void update(Usuario usuario) throws DatabaseException {
        String sql = "UPDATE usuarios SET username = ?, password = ?, nombre = ?, apellido = ?, " +
                    "email = ?, activo = ?, rol_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getPassword());
            stmt.setString(3, usuario.getNombre());
            stmt.setString(4, usuario.getApellido());
            stmt.setString(5, usuario.getEmail());
            stmt.setBoolean(6, usuario.isActivo());
            stmt.setLong(7, usuario.getRol().getId());
            stmt.setLong(8, usuario.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Updating user failed, no rows affected.");
            }
        } catch (SQLException e) {
            logger.error("Error updating user", e);
            throw new DatabaseException("Error updating user: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) throws DatabaseException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Deleting user failed, no rows affected.");
            }
        } catch (SQLException e) {
            logger.error("Error deleting user", e);
            throw new DatabaseException("Error deleting user: " + e.getMessage());
        }
    }

    @Override
    public Optional<Usuario> findByUsername(String username) throws DatabaseException {
        String sql = "SELECT u.*, r.id as rol_id, r.nombre as rol_nombre, r.descripcion as rol_descripcion " +
                    "FROM usuarios u " +
                    "LEFT JOIN roles r ON u.rol_id = r.id " +
                    "WHERE u.username = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by username", e);
            throw new DatabaseException("Error finding user by username: " + e.getMessage());
        }
    }

    @Override
    public List<Usuario> findByActivo(boolean activo) throws DatabaseException {
        String sql = "SELECT u.*, r.id as rol_id, r.nombre as rol_nombre, r.descripcion as rol_descripcion " +
                    "FROM usuarios u " +
                    "LEFT JOIN roles r ON u.rol_id = r.id " +
                    "WHERE u.activo = ?";
        
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, activo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
            
            return usuarios;
        } catch (SQLException e) {
            logger.error("Error finding users by active status", e);
            throw new DatabaseException("Error finding users by active status: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByUsername(String username) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error checking username existence", e);
            throw new DatabaseException("Error checking username existence: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByEmail(String email) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error checking email existence", e);
            throw new DatabaseException("Error checking email existence: " + e.getMessage());
        }
    }
    
    
    public void actualizarUltimoAcceso(Long userId, LocalDateTime fecha) throws DatabaseException {
        String sql = "UPDATE usuarios SET ultimo_acceso = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(fecha));
            stmt.setLong(2, userId);
            
            int filasActualizadas = stmt.executeUpdate();
            
            if (filasActualizadas == 0) {
                throw new DatabaseException("No se pudo actualizar el último acceso: usuario no encontrado");
            }
            
            logger.debug("Último acceso actualizado para usuario ID: {}", userId);
            
        } catch (SQLException e) {
            logger.error("Error al actualizar último acceso del usuario {}", userId, e);
            throw new DatabaseException("Error al actualizar último acceso: " + e.getMessage());
        }
    }

    /**
     * Obtiene la fecha del último acceso del usuario
     */
    public Optional<LocalDateTime> obtenerUltimoAcceso(Long userId) throws DatabaseException {
        String sql = "SELECT ultimo_acceso FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("ultimo_acceso");
                    return Optional.ofNullable(timestamp != null ? timestamp.toLocalDateTime() : null);
                }
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener último acceso del usuario {}", userId, e);
            throw new DatabaseException("Error al obtener último acceso: " + e.getMessage());
        }
    }
    
    
    
    

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(rs.getString("password"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setEmail(rs.getString("email"));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        
        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");
        if (ultimoAcceso != null) {
            usuario.setUltimoAcceso(ultimoAcceso.toLocalDateTime());
        }

        // Mapear el rol si existe
        Long rolId = rs.getLong("rol_id");
        if (!rs.wasNull()) {
            Rol rol = new Rol();
            rol.setId(rolId);
            rol.setNombre(rs.getString("rol_nombre"));
            rol.setDescripcion(rs.getString("rol_descripcion"));
            usuario.setRol(rol);
        }

        return usuario;
    }
}