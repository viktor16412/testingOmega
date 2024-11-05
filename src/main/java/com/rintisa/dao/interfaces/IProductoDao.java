
package com.rintisa.dao.interfaces;

import com.rintisa.model.Producto;
import com.rintisa.exception.DatabaseException;
import java.util.List;
import java.util.Optional;

public interface IProductoDao extends IGenericDao<Producto, Long> {
    Optional<Producto> findByCodigo(String codigo) throws DatabaseException;
    List<Producto> findByActivo(boolean activo) throws DatabaseException;
}
