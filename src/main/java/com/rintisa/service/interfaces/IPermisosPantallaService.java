
package com.rintisa.service.interfaces;

import com.rintisa.model.PermisosPantalla;
import com.rintisa.model.Pantalla;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import java.util.List;
import java.util.Map;

public interface IPermisosPantallaService {
    // Métodos básicos CRUD
    PermisosPantalla crear(PermisosPantalla permiso) throws DatabaseException, ValidationException;
    void actualizar(PermisosPantalla permiso) throws DatabaseException, ValidationException;
    void eliminar(Long id) throws DatabaseException;
    List<PermisosPantalla> obtenerTodos() throws DatabaseException;
    
    // Métodos de búsqueda específicos
    List<PermisosPantalla> obtenerPorRol(String rolNombre) throws DatabaseException;
    PermisosPantalla obtenerPorRolYPantalla(String rolNombre, Pantalla pantalla) 
            throws DatabaseException;
    
    // Métodos de verificación de permisos
    boolean verificarPermiso(String rolNombre, Pantalla pantalla, String tipoPermiso) 
            throws DatabaseException;
    Map<Pantalla, Map<String, Boolean>> obtenerMatrizPermisos(String rolNombre) 
            throws DatabaseException;
    
    // Métodos de actualización de permisos
    void actualizarPermiso(String rolNombre, String pantallaNombre, int tipoPermiso, boolean valor) 
            throws DatabaseException;
    void actualizarPermisosRol(String rolNombre, Map<Pantalla, Map<String, Boolean>> permisos) 
            throws DatabaseException;
    
    // Métodos de gestión de cambios
    void guardarCambios() throws DatabaseException;
    void descartarCambios();
    
    // Métodos de validación
    void validarPermisos(PermisosPantalla permiso) throws ValidationException;
}