package com.rintisa.service.impl;

import com.rintisa.dao.interfaces.IProveedorDao;
import com.rintisa.service.interfaces.IProveedorService;
import com.rintisa.model.Proveedor;
import com.rintisa.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class ProveedorService implements IProveedorService {
    private static final Logger logger = LoggerFactory.getLogger(ProveedorService.class);
    private final IProveedorDao proveedorDao;

    public ProveedorService(IProveedorDao proveedorDao) {
        this.proveedorDao = proveedorDao;
    }

    @Override
    public List<Proveedor> listarTodos() throws DatabaseException {
        try {
            return proveedorDao.findAll();
        } catch (Exception e) {
            logger.error("Error al listar proveedores", e);
            throw new DatabaseException("Error al obtener la lista de proveedores: " + e.getMessage());
        }
    }

    @Override
    public Optional<Proveedor> buscarPorId(Long id) throws DatabaseException {
        try {
            return proveedorDao.findById(id);
        } catch (Exception e) {
            logger.error("Error al buscar proveedor por ID: {}", id, e);
            throw new DatabaseException("Error al buscar proveedor: " + e.getMessage());
        }
    }

    @Override
    public Optional<Proveedor> buscarPorRuc(String ruc) throws DatabaseException {
        try {
            return proveedorDao.findByRuc(ruc);
        } catch (Exception e) {
            logger.error("Error al buscar proveedor por RUC: {}", ruc, e);
            throw new DatabaseException("Error al buscar proveedor: " + e.getMessage());
        }
    }

    @Override
    public List<Proveedor> buscarPorActivo(boolean activo) throws DatabaseException {
        try {
            return proveedorDao.findByActivo(activo);
        } catch (Exception e) {
            logger.error("Error al buscar proveedores por estado activo: {}", activo, e);
            throw new DatabaseException("Error al buscar proveedores: " + e.getMessage());
        }
    }
}
