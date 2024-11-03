package com.rintisa.service.impl;

import com.rintisa.dao.interfaces.IRolDao;
import com.rintisa.service.interfaces.IRolService;
import com.rintisa.model.Rol;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import com.rintisa.util.ValidationUtils;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class RolService implements IRolService {
    
    private static final Logger logger = LoggerFactory.getLogger(RolService.class);
    private final IRolDao rolDao;
    
    public RolService(IRolDao rolDao) {
        this.rolDao = rolDao;
    }
    
    @Override
    public Rol crear(Rol rol) throws DatabaseException, ValidationException {
        logger.debug("Creando nuevo rol: {}", rol.getNombre());
        
        // Validar datos del rol
        ValidationUtils.validarRol(rol.getNombre(), rol.getDescripcion());
        
        // Verificar si ya existe un rol con el mismo nombre
        if (rolDao.existsByNombre(rol.getNombre())) {
            throw new ValidationException("nombre", 
                "Ya existe un rol con el nombre: " + rol.getNombre());
        }
        
        // Establecer estado activo por defecto
        rol.setActivo(true);
        
        try {
            Rol rolCreado = rolDao.save(rol);
            logger.info("Rol creado exitosamente: {}", rol.getNombre());
            return rolCreado;
        } catch (DatabaseException e) {
            logger.error("Error al crear rol: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void actualizar(Rol rol) throws DatabaseException, ValidationException {
        logger.debug("Actualizando rol: {}", rol.getNombre());
        
        // Verificar que el rol existe
        Optional<Rol> rolExistente = rolDao.findById(rol.getId());
        if (!rolExistente.isPresent()) {
            throw new ValidationException("id", "El rol no existe");
        }
        
        // Validar datos del rol
        ValidationUtils.validarRol(rol.getNombre(), rol.getDescripcion());
        
        // Verificar nombre duplicado solo si ha cambiado
        if (!rolExistente.get().getNombre().equals(rol.getNombre()) && 
            rolDao.existsByNombre(rol.getNombre())) {
            throw new ValidationException("nombre", 
                "Ya existe un rol con el nombre: " + rol.getNombre());
        }
        
        try {
            rolDao.update(rol);
            logger.info("Rol actualizado exitosamente: {}", rol.getNombre());
        } catch (DatabaseException e) {
            logger.error("Error al actualizar rol: {}", e.getMessage());
            throw e;
        }
    }

@Override
    public Optional<Rol> buscarPorId(Long id) throws DatabaseException {
        logger.debug("Buscando rol por ID: {}", id);
        try {
            return rolDao.findById(id);
        } catch (DatabaseException e) {
            logger.error("Error al buscar rol por ID: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Rol> buscarPorNombre(String nombre) throws DatabaseException {
        logger.debug("Buscando rol por nombre: {}", nombre);
        try {
            return rolDao.findByNombre(nombre);
        } catch (DatabaseException e) {
            logger.error("Error al buscar rol por nombre: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Rol> listarTodos() throws DatabaseException {
        
        try {
            logger.debug("Listando todos los roles");
            return rolDao.findAll();
        } catch (DatabaseException e) {
            logger.error("Error al listar roles: {}", e.getMessage());
            return new ArrayList<>(); // Retorna lista vacía en caso de error
        }
    }

    @Override
    public List<Rol> buscarPorActivo(boolean activo) throws DatabaseException {
        logger.debug("Buscando roles por estado activo: {}", activo);
        try {
            return rolDao.findByActivo(activo);
        } catch (DatabaseException e) {
            logger.error("Error al buscar roles por estado activo: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Rol> buscar(String criterio) throws DatabaseException {
        logger.debug("Buscando roles con criterio: {}", criterio);
        
        try {
            if (criterio == null || criterio.trim().isEmpty()) {
                return listarTodos();
            }
            
            
            String criterioProcesado = sanitizarCriterioBusqueda(criterio);
            
            
            if (criterioProcesado.isEmpty()) {
                logger.warn("Criterio de búsqueda inválido después de sanitizar: {}", criterio);
                return listarTodos();
            }
            
            return rolDao.search(criterioProcesado);
            
        } catch (DatabaseException e) {
            logger.error("Error al buscar roles con criterio '{}': {}", criterio, e.getMessage());
            throw e;
        }
    }

@Override
    public void eliminar(Long id) throws DatabaseException {
        logger.debug("Eliminando rol con ID: {}", id);
        
        try {
            // Verificar que el rol existe
            Optional<Rol> rol = rolDao.findById(id);
            if (!rol.isPresent()) {
                logger.warn("Intento de eliminar rol inexistente: {}", id);
                //throw new ValidationException("id", "El rol no existe");
            }
            
            // La lógica de verificación de usuarios asociados está en el DAO
            rolDao.delete(id);
            logger.info("Rol eliminado exitosamente: {}", rol.get().getNombre());
            
        } catch (DatabaseException e) {
            logger.error("Error al eliminar rol: {}", e.getMessage());
            throw e;
        }
    }
    
     private String sanitizarCriterioBusqueda(String criterio) {
        if (criterio == null) return "";
        
        return criterio.trim()
                      // Eliminar caracteres especiales y SQL injection básica
                      .replaceAll("[;'\"]", "")
                      // Convertir múltiples espacios en uno solo
                      .replaceAll("\\s+", " ")
                      // Limitar la longitud del criterio
                      .substring(0, Math.min(criterio.length(), 50));
    }
    
    
    private void validarRolExistente(Long id) throws ValidationException, DatabaseException {
        if (!rolDao.findById(id).isPresent()) {
            throw new ValidationException("id", "El rol no existe");
        }
    }
    
    private void validarNombreUnico(String nombre, Long excludeId) 
            throws ValidationException, DatabaseException {
        Optional<Rol> existente = rolDao.findByNombre(nombre);
        if (existente.isPresent() && !existente.get().getId().equals(excludeId)) {
            throw new ValidationException("nombre", 
                "Ya existe un rol con el nombre: " + nombre);
        }
    }
    
    
    
    public boolean esNombreValido(String nombre) {
        return nombre != null && 
               nombre.length() >= 3 && 
               nombre.length() <= 50 && 
               nombre.matches("^[a-zA-Z0-9_\\s-]+$");
    }
    
    public boolean esDescripcionValida(String descripcion) {
        return descripcion != null && 
               !descripcion.trim().isEmpty() && 
               descripcion.length() <= 255;
    }
    
    /**
     * elimina caracteres especiales y espacios extras.
     */
    private String sanitizarNombre(String nombre) {
        if (nombre == null) return null;
        // Eliminar espacios al inicio y final, y reemplazar múltiples espacios por uno solo
        return nombre.trim().replaceAll("\\s+", " ")
                   // Eliminar caracteres especiales excepto guiones y guiones bajos
                   .replaceAll("[^a-zA-Z0-9_\\s-]", "");
    }
    
    private String sanitizarDescripcion(String descripcion) {
        if (descripcion == null) return null;
        // Eliminar espacios al inicio y final, y reemplazar múltiples espacios por uno solo
        return descripcion.trim().replaceAll("\\s+", " ")
                        // Eliminar caracteres potencialmente peligrosos
                        .replaceAll("[<>&\"]", "");
    }
    
    
    private void prepararRol(Rol rol) throws ValidationException {
        
        rol.setNombre(sanitizarNombre(rol.getNombre()));
        rol.setDescripcion(sanitizarDescripcion(rol.getDescripcion()));
        
        // Validar después de sanitizar
        if (!esNombreValido(rol.getNombre())) {
            throw new ValidationException("nombre", 
                "El nombre del rol contiene caracteres no válidos o longitud incorrecta");
        }
        
        if (!esDescripcionValida(rol.getDescripcion())) {
            throw new ValidationException("descripcion", 
                "La descripción del rol es inválida o excede la longitud máxima");
        }
    }
}

