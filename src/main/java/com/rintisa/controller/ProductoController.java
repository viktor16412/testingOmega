
package com.rintisa.controller;

import com.rintisa.exception.ValidationException;
import com.rintisa.model.Producto;
import com.rintisa.service.interfaces.IProductoService;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductoController {
    private final IProductoService productoService;
    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    public ProductoController(IProductoService productoService) {
        if (productoService == null) {
            throw new IllegalArgumentException("productoService no puede ser null");
        }
        this.productoService = productoService;
    }

    public List<Producto> listarProductos() {
        try {
            logger.debug("Listando todos los productos");
            return productoService.listarTodos();
        } catch (Exception e) {
            logger.error("Error al listar productos", e);
            throw new RuntimeException("Error al obtener lista de productos: " + e.getMessage());
        }
    }

    public List<Producto> buscarProductos(String criterio) {
        try {
            logger.debug("Buscando productos con criterio: {}", criterio);
            if (criterio == null || criterio.trim().isEmpty()) {
                return listarProductos();
            }
            return productoService.buscar(criterio);
        } catch (Exception e) {
            logger.error("Error al buscar productos", e);
            throw new RuntimeException("Error al buscar productos: " + e.getMessage());
        }
    }

    public void crearProducto(Producto producto) throws ValidationException {
        try {
            logger.debug("Creando nuevo producto: {}", producto.getCodigo());
            
            // Validar producto
            validarProducto(producto);
            
            // Verificar si ya existe el código
            if (productoService.existePorCodigo(producto.getCodigo())) {
                throw new ValidationException("codigo", "El código ya existe");
            }
            
            productoService.crear(producto);
            logger.info("Producto creado exitosamente: {}", producto.getCodigo());
            
        } catch (ValidationException e) {
            logger.warn("Error de validación al crear producto: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear producto", e);
            throw new RuntimeException("Error al crear producto: " + e.getMessage());
        }
    }

    public void actualizarProducto(Producto producto) throws ValidationException {
        try {
            logger.debug("Actualizando producto: {}", producto.getCodigo());
            
            // Validar producto
            validarProducto(producto);
            
            productoService.actualizar(producto);
            logger.info("Producto actualizado exitosamente: {}", producto.getCodigo());
            
        } catch (ValidationException e) {
            logger.warn("Error de validación al actualizar producto: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar producto", e);
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage());
        }
    }

    public void eliminarProducto(Long id) {
        try {
            logger.debug("Eliminando producto con ID: {}", id);
            productoService.eliminar(id);
            logger.info("Producto eliminado exitosamente: {}", id);
        } catch (Exception e) {
            logger.error("Error al eliminar producto", e);
            throw new RuntimeException("Error al eliminar producto: " + e.getMessage());
        }
    }

    private void validarProducto(Producto producto) throws ValidationException {
        List<String> errores = new ArrayList<>();

        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            errores.add("El código es requerido");
        } else if (producto.getCodigo().length() > 20) {
            errores.add("El código no puede exceder 20 caracteres");
        }

        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            errores.add("El nombre es requerido");
        } else if (producto.getNombre().length() > 100) {
            errores.add("El nombre no puede exceder 100 caracteres");
        }

        if (producto.getUnidadMedida() == null || producto.getUnidadMedida().trim().isEmpty()) {
            errores.add("La unidad de medida es requerida");
        }

        if (producto.getPrecioUnitario() < 0) {
            errores.add("El precio no puede ser negativo");
        }

        if (producto.getStockMinimo() < 0) {
            errores.add("El stock mínimo no puede ser negativo");
        }

        if (producto.getStockActual() < 0) {
            errores.add("El stock actual no puede ser negativo");
        }

        if (!errores.isEmpty()) {
            throw new ValidationException("validación", String.join(", ", errores));
        }
    }
    
    public List<Producto> buscarProductosPorFiltros(Map<String, Object> filtros) {
        try {
            logger.debug("Buscando productos con filtros: {}", filtros);
            return productoService.buscarPorFiltros(filtros);
        } catch (Exception e) {
            logger.error("Error al buscar productos con filtros", e);
            throw new RuntimeException("Error al buscar productos: " + e.getMessage());
        }
    }
    
    
}
