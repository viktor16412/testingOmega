package com.rintisa.service.interfaces;


import com.rintisa.model.Proveedor;
import com.rintisa.exception.DatabaseException;
import java.util.List;
import java.util.Optional;

public interface IProveedorService {
    List<Proveedor> listarTodos() throws DatabaseException;
    Optional<Proveedor> buscarPorId(Long id) throws DatabaseException;
    Optional<Proveedor> buscarPorRuc(String ruc) throws DatabaseException;
    List<Proveedor> buscarPorActivo(boolean activo) throws DatabaseException;
}