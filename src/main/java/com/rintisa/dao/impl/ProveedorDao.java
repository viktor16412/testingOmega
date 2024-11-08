
package com.rintisa.dao.impl;

import com.rintisa.config.DatabaseConfig;
import com.rintisa.dao.interfaces.IProveedorDao;
import com.rintisa.model.Proveedor;
import com.rintisa.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProveedorDao implements IProveedorDao {
    private static final Logger logger = LoggerFactory.getLogger(ProveedorDao.class);

    @Override
    public List<Proveedor> findAll() throws DatabaseException {
        String sql = "SELECT * FROM proveedores ORDER BY razon_social";
        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                proveedores.add(mapResultSetToProveedor(rs));
            }
            return proveedores;
        } catch (SQLException e) {
            logger.error("Error al obtener todos los proveedores", e);
            throw new DatabaseException("Error al obtener la lista de proveedores: " + e.getMessage());
        }
    }

    @Override
    public Optional<Proveedor> findById(Long id) throws DatabaseException {
        String sql = "SELECT * FROM proveedores WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProveedor(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error al buscar proveedor por ID: {}", id, e);
            throw new DatabaseException("Error al buscar proveedor: " + e.getMessage());
        }
    }

    @Override
    public Optional<Proveedor> findByCodigo(String codigo) throws DatabaseException {
        String sql = "SELECT * FROM proveedores WHERE codigo = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProveedor(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error al buscar proveedor por código: {}", codigo, e);
            throw new DatabaseException("Error al buscar proveedor: " + e.getMessage());
        }
    }

    @Override
    public Optional<Proveedor> findByRuc(String ruc) throws DatabaseException {
        String sql = "SELECT * FROM proveedores WHERE ruc = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ruc);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProveedor(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error al buscar proveedor por RUC: {}", ruc, e);
            throw new DatabaseException("Error al buscar proveedor: " + e.getMessage());
        }
    }

    @Override
    public List<Proveedor> findByActivo(boolean activo) throws DatabaseException {
        String sql = "SELECT * FROM proveedores WHERE activo = ? ORDER BY razon_social";
        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, activo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(mapResultSetToProveedor(rs));
                }
            }
            return proveedores;
        } catch (SQLException e) {
            logger.error("Error al buscar proveedores por estado activo: {}", activo, e);
            throw new DatabaseException("Error al buscar proveedores: " + e.getMessage());
        }
    }

    @Override
    public Proveedor save(Proveedor proveedor) throws DatabaseException {
        String sql = "INSERT INTO proveedores (codigo, razon_social, ruc, direccion, telefono, email, activo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, proveedor.getCodigo());
            stmt.setString(2, proveedor.getRazonSocial());
            stmt.setString(3, proveedor.getRuc());
            stmt.setString(4, proveedor.getDireccion());
            stmt.setString(5, proveedor.getTelefono());
            stmt.setString(6, proveedor.getEmail());
            stmt.setBoolean(7, proveedor.isActivo());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("No se pudo guardar el proveedor");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    proveedor.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("No se pudo obtener el ID generado para el proveedor");
                }
            }
            return proveedor;
        } catch (SQLException e) {
            logger.error("Error al guardar proveedor", e);
            throw new DatabaseException("Error al guardar proveedor: " + e.getMessage());
        }
    }

    @Override
    public void update(Proveedor proveedor) throws DatabaseException {
        String sql = "UPDATE proveedores SET codigo = ?, razon_social = ?, ruc = ?, " +
                    "direccion = ?, telefono = ?, email = ?, activo = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, proveedor.getCodigo());
            stmt.setString(2, proveedor.getRazonSocial());
            stmt.setString(3, proveedor.getRuc());
            stmt.setString(4, proveedor.getDireccion());
            stmt.setString(5, proveedor.getTelefono());
            stmt.setString(6, proveedor.getEmail());
            stmt.setBoolean(7, proveedor.isActivo());
            stmt.setLong(8, proveedor.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("No se pudo actualizar el proveedor");
            }
        } catch (SQLException e) {
            logger.error("Error al actualizar proveedor", e);
            throw new DatabaseException("Error al actualizar proveedor: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) throws DatabaseException {
        String sql = "DELETE FROM proveedores WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("No se pudo eliminar el proveedor");
            }
        } catch (SQLException e) {
            logger.error("Error al eliminar proveedor", e);
            throw new DatabaseException("Error al eliminar proveedor: " + e.getMessage());
        }
    }

    private Proveedor mapResultSetToProveedor(ResultSet rs) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(rs.getLong("id"));
        proveedor.setCodigo(rs.getString("codigo"));
        proveedor.setRazonSocial(rs.getString("razon_social"));
        proveedor.setRuc(rs.getString("ruc"));
        proveedor.setDireccion(rs.getString("direccion"));
        proveedor.setTelefono(rs.getString("telefono"));
        proveedor.setEmail(rs.getString("email"));
        proveedor.setActivo(rs.getBoolean("activo"));
        return proveedor;
    }
}