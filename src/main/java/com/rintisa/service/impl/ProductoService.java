
package com.rintisa.service.impl;

import com.rintisa.dao.interfaces.IProductoDao;
import com.rintisa.service.interfaces.IProductoService;
import com.rintisa.model.Producto;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class ProductoService implements IProductoService {
    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);
    private final IProductoDao productoDao;

    public ProductoService(IProductoDao productoDao) {
        this.productoDao = productoDao;
    }

    @Override
    public Producto crear(Producto producto) throws DatabaseException, ValidationException {
        logger.debug("Creando nuevo producto: {}", producto.getCodigo());
        
        // Validar datos del producto
        validar(producto);
        
        // Verificar si ya existe el código
        if (existePorCodigo(producto.getCodigo())) {
            throw new ValidationException("codigo", "El código del producto ya está en uso");
        }
        
        try {
            Producto productoCreado = productoDao.save(producto);
            logger.info("Producto creado exitosamente: {}", producto.getCodigo());
            return productoCreado;
        } catch (DatabaseException e) {
            logger.error("Error al crear producto: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void actualizar(Producto producto) throws DatabaseException, ValidationException {
        logger.debug("Actualizando producto: {}", producto.getCodigo());
        
        // Verificar que el producto existe
        Optional<Producto> productoExistente = productoDao.findById(producto.getId());
        if (!productoExistente.isPresent()) {
            throw new ValidationException("id", "El producto no existe");
        }

        // Validar datos del producto
        validar(producto);
        
        // Verificar código duplicado solo si ha cambiado
        if (!productoExistente.get().getCodigo().equals(producto.getCodigo()) &&
            existePorCodigo(producto.getCodigo())) {
            throw new ValidationException("codigo", "El código del producto ya está en uso");
        }
        
        try {
            productoDao.update(producto);
            logger.info("Producto actualizado exitosamente: {}", producto.getCodigo());
        } catch (DatabaseException e) {
            logger.error("Error al actualizar producto: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void eliminar(Long id) throws DatabaseException {
        logger.debug("Eliminando producto con ID: {}", id);
        try {
            // Verificar que el producto existe
            Optional<Producto> producto = productoDao.findById(id);
            if (!producto.isPresent()) {
                throw new ValidationException("id", "El producto no existe");
            }
            
            // Verificar si tiene movimientos asociados antes de eliminar
            // TODO: Implementar verificación de movimientos
            
            productoDao.delete(id);
            logger.info("Producto eliminado exitosamente: {}", id);
        } catch (DatabaseException e) {
            logger.error("Error al eliminar producto: {}", e.getMessage());
            throw e;
        } catch (ValidationException ex) {
            java.util.logging.Logger.getLogger(ProductoService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) throws DatabaseException {
        logger.debug("Buscando producto por ID: {}", id);
        try {
            return productoDao.findById(id);
        } catch (DatabaseException e) {
            logger.error("Error al buscar producto por ID: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) throws DatabaseException {
        logger.debug("Buscando producto por código: {}", codigo);
        try {
            return productoDao.findByCodigo(codigo);
        } catch (DatabaseException e) {
            logger.error("Error al buscar producto por código: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Producto> listarTodos() throws DatabaseException {
        try {
            logger.debug("Listando todos los productos");
            return productoDao.findAll();
        } catch (DatabaseException e) {
            logger.error("Error al listar productos: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void actualizarStock(Long productoId, int cantidad, String tipoMovimiento) 
            throws DatabaseException, ValidationException {
        logger.debug("Actualizando stock del producto ID: {}", productoId);
        
        try {
            Optional<Producto> productoOpt = productoDao.findById(productoId);
            if (!productoOpt.isPresent()) {
                throw new ValidationException("id", "El producto no existe");
            }
            
            Producto producto = productoOpt.get();
            int nuevoStock;
            
            if ("ENTRADA".equals(tipoMovimiento)) {
                nuevoStock = producto.getStockActual() + cantidad;
            } else if ("SALIDA".equals(tipoMovimiento)) {
                if (!verificarStockSuficiente(productoId, cantidad)) {
                    throw new ValidationException("stock", "Stock insuficiente");
                }
                nuevoStock = producto.getStockActual() - cantidad;
            } else {
                throw new ValidationException("tipoMovimiento", "Tipo de movimiento inválido");
            }
            
            producto.setStockActual(nuevoStock);
            productoDao.update(producto);
            
            logger.info("Stock actualizado para producto {}: {}", producto.getCodigo(), nuevoStock);
        } catch (DatabaseException e) {
            logger.error("Error al actualizar stock: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean verificarStockSuficiente(Long productoId, int cantidad) throws DatabaseException {
        Optional<Producto> producto = buscarPorId(productoId);
        return producto.isPresent() && producto.get().getStockActual() >= cantidad;
    }

    @Override
    public void validar(Producto producto) throws ValidationException {
        ValidationException.Builder validationBuilder = 
            new ValidationException.Builder("Error de validación");
            
        // Validar código
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            validationBuilder.addError("codigo", "El código es requerido");
        } else if (producto.getCodigo().length() > 20) {
            validationBuilder.addError("codigo", "El código no puede exceder 20 caracteres");
        }
        
        // Validar nombre
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            validationBuilder.addError("nombre", "El nombre es requerido");
        } else if (producto.getNombre().length() > 100) {
            validationBuilder.addError("nombre", "El nombre no puede exceder 100 caracteres");
        }
        
        // Validar unidad de medida
        if (producto.getUnidadMedida() == null || producto.getUnidadMedida().trim().isEmpty()) {
            validationBuilder.addError("unidadMedida", "La unidad de medida es requerida");
        }
        
        // Validar precio
        if (producto.getPrecioUnitario() < 0) {
            validationBuilder.addError("precioUnitario", "El precio no puede ser negativo");
        }
        
        // Validar stocks
        if (producto.getStockMinimo() < 0) {
            validationBuilder.addError("stockMinimo", "El stock mínimo no puede ser negativo");
        }
        
        if (producto.getStockActual() < 0) {
            validationBuilder.addError("stockActual", "El stock actual no puede ser negativo");
        }
        
        validationBuilder.throwIfHasErrors();
    }

    @Override
    public boolean existePorCodigo(String codigo) throws DatabaseException {
        return buscarPorCodigo(codigo).isPresent();
    }

    @Override
    public List<Producto> buscarPorActivo(boolean activo) throws DatabaseException {
        return productoDao.findByActivo(activo);
    }

    @Override
    public List<Producto> listarProductosStockBajo() throws DatabaseException {
        logger.debug("Buscando productos con stock bajo");
        List<Producto> productosStockBajo = new ArrayList<>();
        
        try {
            List<Producto> todosProductos = listarTodos();
            for (Producto producto : todosProductos) {
                if (producto.getStockActual() <= producto.getStockMinimo()) {
                    productosStockBajo.add(producto);
                }
            }
            return productosStockBajo;
        } catch (DatabaseException e) {
            logger.error("Error al listar productos con stock bajo: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Producto> buscar(String criterio) throws DatabaseException {
        // Implementar búsqueda por criterios
        // TODO: Implementar búsqueda personalizada
        return new ArrayList<>();
    }
}
