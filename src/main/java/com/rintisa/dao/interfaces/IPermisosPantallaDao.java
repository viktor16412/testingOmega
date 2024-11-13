package com.rintisa.dao.interfaces;

import com.rintisa.model.PermisosPantalla;
import com.rintisa.model.Pantalla;
import com.rintisa.exception.DatabaseException;
import java.util.List;

public interface IPermisosPantallaDao extends IGenericDao<PermisosPantalla, Long> {
    List<PermisosPantalla> findByRol(String rolNombre) throws DatabaseException;
    PermisosPantalla findByRolAndPantalla(String rolNombre, Pantalla pantalla) throws DatabaseException;
    void updatePermisosByRol(String rolNombre, List<PermisosPantalla> permisos) throws DatabaseException;
    boolean existePermiso(String rolNombre, Pantalla pantalla) throws DatabaseException;
}