package com.rintisa.dao.interfaces;

import com.rintisa.model.Rol;
import com.rintisa.exception.DatabaseException;

import java.util.List;
import java.util.Optional;

public interface IRolDao extends IGenericDao<Rol, Long> {
    Optional<Rol> findByNombre(String nombre) throws DatabaseException;
    List<Rol> findByActivo(boolean activo) throws DatabaseException;
    boolean existsByNombre(String nombre) throws DatabaseException;
     List<Rol> search(String criterio) throws DatabaseException;
}