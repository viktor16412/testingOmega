
package com.rintisa.service.interfaces;

import com.rintisa.model.Producto;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IProductoService {
    /**
     * Crea un nuevo producto
     */
    Producto crear(Producto producto) throws DatabaseException, ValidationException;
    
    /**
     * Actualiza un producto existente
     */
    void actualizar(Producto producto) throws DatabaseException, ValidationException;
    
    /**
     * Elimina un producto por su ID
     */
    void eliminar(Long id) throws DatabaseException;
    
    /**
     * Busca un producto por su ID
     */
    Optional<Producto> buscarPorId(Long id) throws DatabaseException;
    
    /**
     * Busca un producto por su código
     */
    Optional<Producto> buscarPorCodigo(String codigo) throws DatabaseException;
    
    /**
     * Lista todos los productos
     */
    List<Producto> listarTodos() throws DatabaseException;
    
    /**
     * Lista productos por estado activo/inactivo
     */
    List<Producto> buscarPorActivo(boolean activo) throws DatabaseException;
    
    /**
     * Actualiza el stock de un producto
     */
    void actualizarStock(Long productoId, int cantidad, String tipoMovimiento) 
        throws DatabaseException, ValidationException;
    
    /**
     * Verifica si hay stock suficiente
     */
    boolean verificarStockSuficiente(Long productoId, int cantidad) throws DatabaseException;
    
    /**
     * Lista productos con stock bajo (menor al mínimo)
     */
    List<Producto> listarProductosStockBajo() throws DatabaseException;
    
    /**
     * Busca productos por criterios múltiples
     */
    List<Producto> buscar(String criterio) throws DatabaseException;
    
    /**
     * Verifica si existe un producto con el código dado
     */
    boolean existePorCodigo(String codigo) throws DatabaseException;
    
    /**
     * Valida los datos de un producto
     */
    void validar(Producto producto) throws ValidationException;
    
    List<Producto> buscarPorFiltros(Map<String, Object> filtros) 
            throws DatabaseException, ValidationException;
}