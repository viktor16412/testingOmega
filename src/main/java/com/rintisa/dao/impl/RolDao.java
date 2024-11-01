/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.dao.impl;

import com.rintisa.config.DatabaseConfig;
import com.rintisa.dao.interfaces.IRolDao;
import com.rintisa.exception.DatabaseException;
import com.rintisa.model.Rol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RolDao implements IRolDao {
    private static final Logger logger = LoggerFactory.getLogger(RolDao.class);

    @Override
    public Rol save(Rol rol) throws DatabaseException {
        String sql = "INSERT INTO roles (nombre, descripcion, activo) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            stmt.setBoolean(3, rol.isActivo());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Creating role failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rol.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("Creating role failed, no ID obtained.");
                }
            }
            
            return rol;
        } catch (SQLException e) {
            logger.error("Error saving role", e);
            throw new DatabaseException("Error saving role: " + e.getMessage());
        }
    }

    @Override
    public Optional<Rol> findById(Long id) throws DatabaseException {
        String sql = "SELECT * FROM roles WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRol(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding role by ID", e);
            throw new DatabaseException("Error finding role: " + e.getMessage());
        }
    }

    private Rol mapResultSetToRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setId(rs.getLong("id"));
        rol.setNombre(rs.getString("nombre"));
        rol.setDescripcion(rs.getString("descripcion"));
        rol.setActivo(rs.getBoolean("activo"));
        return rol;
    }
    @Override
    public List<Rol> findAll() throws DatabaseException {
        String sql = "SELECT * FROM roles";
        List<Rol> roles = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                roles.add(mapResultSetToRol(rs));
            }
            
            return roles;
        } catch (SQLException e) {
            logger.error("Error finding all roles", e);
            throw new DatabaseException("Error finding all roles: " + e.getMessage());
        }
    }

    @Override
    public void update(Rol rol) throws DatabaseException {
        String sql = "UPDATE roles SET nombre = ?, descripcion = ?, activo = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            stmt.setBoolean(3, rol.isActivo());
            stmt.setLong(4, rol.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Updating role failed, no rows affected.");
            }
        } catch (SQLException e) {
            logger.error("Error updating role", e);
            throw new DatabaseException("Error updating role: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) throws DatabaseException {
        // Primero verificamos si hay usuarios asociados a este rol
        if (hasAssociatedUsers(id)) {
            throw new DatabaseException("Cannot delete role: There are users associated with this role.");
        }

        String sql = "DELETE FROM roles WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Deleting role failed, no rows affected.");
            }
        } catch (SQLException e) {
            logger.error("Error deleting role", e);
            throw new DatabaseException("Error deleting role: " + e.getMessage());
        }
    }

    private boolean hasAssociatedUsers(Long rolId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, rolId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error checking for associated users", e);
            throw new DatabaseException("Error checking for associated users: " + e.getMessage());
        }
    }
     @Override
    public Optional<Rol> findByNombre(String nombre) throws DatabaseException {
        String sql = "SELECT * FROM roles WHERE nombre = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nombre);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRol(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding role by name", e);
            throw new DatabaseException("Error finding role by name: " + e.getMessage());
        }
    }

    @Override
    public List<Rol> findByActivo(boolean activo) throws DatabaseException {
        String sql = "SELECT * FROM roles WHERE activo = ?";
        List<Rol> roles = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, activo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(mapResultSetToRol(rs));
                }
            }
            
            return roles;
        } catch (SQLException e) {
            logger.error("Error finding roles by active status", e);
            throw new DatabaseException("Error finding roles by active status: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByNombre(String nombre) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM roles WHERE nombre = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nombre);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error checking role name existence", e);
            throw new DatabaseException("Error checking role name existence: " + e.getMessage());
        }
    }

    public List<Rol> search(String criteria) throws DatabaseException {
        String sql = "SELECT * FROM roles WHERE nombre LIKE ? OR descripcion LIKE ?";
        List<Rol> roles = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + criteria + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(mapResultSetToRol(rs));
                }
            }
            
            return roles;
        } catch (SQLException e) {
            logger.error("Error searching roles", e);
            throw new DatabaseException("Error searching roles: " + e.getMessage());
        }
    }
    
}