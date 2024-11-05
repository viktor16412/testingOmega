
package com.rintisa.dao.impl;

import com.rintisa.config.DatabaseConfig;
import com.rintisa.dao.interfaces.IProductoDao;
import com.rintisa.exception.DatabaseException;
import com.rintisa.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDao implements IProductoDao {
    private static final Logger logger = LoggerFactory.getLogger(ProductoDao.class);

    @Override
    public Producto save(Producto producto) throws DatabaseException {
        String sql = "INSERT INTO productos (codigo, nombre, descripcion, unidad_medida, " +
                    "precio_unitario, stock_minimo, stock_actual, activo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setString(4, producto.getUnidadMedida());
            stmt.setDouble(5, producto.getPrecioUnitario());
            stmt.setInt(6, producto.getStockMinimo());
            stmt.setInt(7, producto.getStockActual());
            stmt.setBoolean(8, producto.isActivo());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    producto.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("Creating product failed, no ID obtained.");
                }
            }
            
            return producto;
        } catch (SQLException e) {
            logger.error("Error saving product", e);
            throw new DatabaseException("Error saving product: " + e.getMessage());
        }
    }

    @Override
    public Optional<Producto> findById(Long id) throws DatabaseException {
        String sql = "SELECT * FROM productos WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding product by ID", e);
            throw new DatabaseException("Error finding product: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> findAll() throws DatabaseException {
        String sql = "SELECT * FROM productos ORDER BY nombre";
        List<Producto> productos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                productos.add(mapResultSetToProduct(rs));
            }
            
            return productos;
        } catch (SQLException e) {
            logger.error("Error finding all products", e);
            throw new DatabaseException("Error finding all products: " + e.getMessage());
        }
    }

    @Override
    public void update(Producto producto) throws DatabaseException {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, descripcion = ?, " +
                    "unidad_medida = ?, precio_unitario = ?, stock_minimo = ?, " +
                    "stock_actual = ?, activo = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setString(4, producto.getUnidadMedida());
            stmt.setDouble(5, producto.getPrecioUnitario());
            stmt.setInt(6, producto.getStockMinimo());
            stmt.setInt(7, producto.getStockActual());
            stmt.setBoolean(8, producto.isActivo());
            stmt.setLong(9, producto.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Updating product failed, no rows affected.");
            }
        } catch (SQLException e) {
            logger.error("Error updating product", e);
            throw new DatabaseException("Error updating product: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) throws DatabaseException {
        String sql = "DELETE FROM productos WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Deleting product failed, no rows affected.");
            }
        } catch (SQLException e) {
            logger.error("Error deleting product", e);
            throw new DatabaseException("Error deleting product: " + e.getMessage());
        }
    }

    @Override
    public Optional<Producto> findByCodigo(String codigo) throws DatabaseException {
        String sql = "SELECT * FROM productos WHERE codigo = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codigo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding product by code", e);
            throw new DatabaseException("Error finding product by code: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> findByActivo(boolean activo) throws DatabaseException {
        String sql = "SELECT * FROM productos WHERE activo = ? ORDER BY nombre";
        List<Producto> productos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, activo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapResultSetToProduct(rs));
                }
            }
            
            return productos;
        } catch (SQLException e) {
            logger.error("Error finding products by active status", e);
            throw new DatabaseException("Error finding products by active status: " + e.getMessage());
        }
    }

    private Producto mapResultSetToProduct(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getLong("id"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setUnidadMedida(rs.getString("unidad_medida"));
        producto.setPrecioUnitario(rs.getDouble("precio_unitario"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setStockActual(rs.getInt("stock_actual"));
        producto.setActivo(rs.getBoolean("activo"));
        producto.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        
        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            producto.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }
        
        return producto;
    }
}