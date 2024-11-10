
package com.rintisa.dao.interfaces;

import com.rintisa.model.Producto;
import com.rintisa.exception.DatabaseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IProductoDao extends IGenericDao<Producto, Long> {
    Optional<Producto> findByCodigo(String codigo) throws DatabaseException;
    List<Producto> findByActivo(boolean activo) throws DatabaseException;
    List<Producto> buscar(String criterio) throws DatabaseException;
    List<Producto> buscarPorFiltros(Map<String, Object> filtros) throws DatabaseException;
    //Optional<Producto> buscarPorCodigo(String codigo) throws DatabaseException;
    //List<Producto> buscarPorActivo(boolean activo) throws DatabaseException;
 
}
