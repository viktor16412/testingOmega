package com.rintisa.dao.interfaces;

import com.rintisa.model.Proveedor;
import com.rintisa.exception.DatabaseException;
import java.util.List;
import java.util.Optional;

public interface IProveedorDao {
    List<Proveedor> findAll() throws DatabaseException;
    Optional<Proveedor> findById(Long id) throws DatabaseException;
    Optional<Proveedor> findByCodigo(String codigo) throws DatabaseException;
    Optional<Proveedor> findByRuc(String ruc) throws DatabaseException;
    List<Proveedor> findByActivo(boolean activo) throws DatabaseException;
    Proveedor save(Proveedor proveedor) throws DatabaseException;
    void update(Proveedor proveedor) throws DatabaseException;
    void delete(Long id) throws DatabaseException;
}