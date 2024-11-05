package com.rintisa.service.interfaces;

import com.rintisa.model.Rol;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import java.util.List;
import java.util.Optional;

public interface IRolService {
    Rol crear(Rol rol) throws DatabaseException, ValidationException;
    
    void actualizar(Rol rol) throws DatabaseException, ValidationException;
    
    void eliminar(Long id) throws DatabaseException;
    
    Optional<Rol> buscarPorId(Long id) throws DatabaseException;
    
    Optional<Rol> buscarPorNombre(String nombre) throws DatabaseException;
    
    List<Rol> listarTodos() throws DatabaseException;
    
    List<Rol> buscarPorActivo(boolean activo) throws DatabaseException;
    
    List<Rol> buscar(String criterio) throws DatabaseException;
      // Nuevos métodos para permisos
    boolean tienePermiso(Long rolId, String codigoPermiso) throws DatabaseException;
    List<String> obtenerPermisos(Long rolId) throws DatabaseException;
    boolean tieneAccesoAlmacen(Long rolId) throws DatabaseException;
    
    /**
     * Asigna un permiso a un rol
     */
    void asignarPermiso(Long rolId, String codigoPermiso, Long usuarioId) 
        throws DatabaseException, ValidationException;
    
    /**
     * Revoca un permiso de un rol
     */
    void revocarPermiso(Long rolId, String codigoPermiso, Long usuarioId) 
        throws DatabaseException, ValidationException;
    
    /**
     * Verifica si un rol tiene un conjunto de permisos
     */
    boolean tienePermisos(Long rolId, List<String> codigosPermisos) throws DatabaseException;

}