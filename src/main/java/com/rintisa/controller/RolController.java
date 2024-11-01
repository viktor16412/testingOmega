/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.controller;

import com.rintisa.model.Rol;
import com.rintisa.service.interfaces.IRolService;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class RolController {
    
    private static final Logger logger = LoggerFactory.getLogger(RolController.class);
    
    private final IRolService rolService;
    
    // Constructor
    public RolController(IRolService rolService) {
        this.rolService = rolService;
    }
    
    
     public Rol crear(Rol rol) {
        try {
            validarRol(rol);
            return rolService.crear(rol);
        } catch (ValidationException | DatabaseException e) {
            logger.error("Error al crear rol: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear el rol: " + e.getMessage(), e);
        }
    }
    public void actualizar(Rol rol) {
        try {
            validarRol(rol);
            rolService.actualizar(rol);
        } catch (ValidationException | DatabaseException e) {
            logger.error("Error al actualizar rol: {}", e.getMessage());
            throw new RuntimeException("No se pudo actualizar el rol: " + e.getMessage(), e);
        }
    }
    public void eliminar(Long id) {
        try {
            rolService.eliminar(id);
        } catch (DatabaseException e) {
            logger.error("Error al eliminar rol: {}", e.getMessage());
            throw new RuntimeException("No se pudo eliminar el rol: " + e.getMessage(), e);
        }
    }
    public Optional<Rol> buscarRol(Long id) {
        try {
            return rolService.buscarPorId(id);
        } catch (DatabaseException e) {
            logger.error("Error al buscar rol: {}", e.getMessage());
            throw new RuntimeException("Error al buscar el rol: " + e.getMessage(), e);
        }
    }
    public List<Rol> listarRoles() {
        try {
            return rolService.listarTodos();
        } catch (DatabaseException e) {
            logger.error("Error al listar roles: {}", e.getMessage());
            throw new RuntimeException("Error al obtener la lista de roles: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica si existe un rol con el mismo nombre
     */
    public boolean existeNombre(String nombre, Long idExcluir) {
        try {
            Optional<Rol> rolExistente = rolService.buscarPorNombre(nombre);
            return rolExistente.isPresent() && 
                   !rolExistente.get().getId().equals(idExcluir);
        } catch (DatabaseException e) {
            logger.error("Error al verificar nombre de rol: {}", e.getMessage());
            throw new RuntimeException("Error al verificar el nombre del rol: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca roles que coincidan con el criterio
     */
    public List<Rol> buscar(String criterio) {
        try {
            return rolService.buscar(criterio);
        } catch (DatabaseException e) {
            logger.error("Error al buscar roles: {}", e.getMessage());
            throw new RuntimeException("Error al buscar roles: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida los datos de un rol
     */
    private void validarRol(Rol rol) throws ValidationException {
        ValidationException.Builder validationBuilder = 
            new ValidationException.Builder("Error de validación");
            
        // Validar nombre
        if (rol.getNombre() == null || rol.getNombre().trim().isEmpty()) {
            validationBuilder.addError("nombre", "El nombre del rol es requerido");
        } else if (rol.getNombre().length() < 3) {
            validationBuilder.addError("nombre", 
                "El nombre del rol debe tener al menos 3 caracteres");
        } else if (rol.getNombre().length() > 50) {
            validationBuilder.addError("nombre", 
                "El nombre del rol no puede exceder los 50 caracteres");
        }
        
        // Validar descripción
        if (rol.getDescripcion() == null || rol.getDescripcion().trim().isEmpty()) {
            validationBuilder.addError("descripcion", 
                "La descripción del rol es requerida");
        } else if (rol.getDescripcion().length() > 255) {
            validationBuilder.addError("descripcion", 
                "La descripción no puede exceder los 255 caracteres");
        }
        
        // Lanzar excepción si hay errores
        validationBuilder.throwIfHasErrors();
    }
    
    /**
     * Verifica si es el rol de administrador
     */
    public boolean esRolAdmin(Rol rol) {
        return rol != null && "ADMIN".equals(rol.getNombre());
    }
    
    /**
     * Verifica si un rol puede ser eliminado
     */
    public boolean puedeSerEliminado(Long rolId) {
        try {
            Optional<Rol> rol = buscarRol(rolId);
            if (!rol.isPresent()) {
                return false;
            }
            
            // No permitir eliminar el rol ADMIN
            if (esRolAdmin(rol.get())) {
                return false;
            }
            
            // Aquí podrías agregar más validaciones
            // Por ejemplo, verificar si hay usuarios usando este rol
            
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar si el rol puede ser eliminado: {}", 
                        e.getMessage());
            return false;
        }
    }
    
    
    /**
     * Crea un nuevo rol
     */
    public Rol crearRol(String nombre, String descripcion) {
        logger.info("Intentando crear nuevo rol: {}", nombre);
        try {
            // Validar datos
            validarDatosRol(nombre, descripcion);
            
            // Crear el rol
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre(nombre.toUpperCase().trim());
            nuevoRol.setDescripcion(descripcion.trim());
            nuevoRol.setActivo(true);
            
            Rol rolCreado = rolService.crear(nuevoRol);
            logger.info("Rol creado exitosamente: {}", rolCreado.getNombre());
            return rolCreado;
            
        } catch (ValidationException e) {
            logger.warn("Error de validación al crear rol: {}", e.getMessage());
            manejarExcepcionValidacion(e, "crear");
            return null; // Nunca se alcanza, pero el compilador lo necesita
        } catch (DatabaseException e) {
            logger.error("Error de base de datos al crear rol: {}", e.getMessage());
            manejarExcepcionBD(e, "crear");
            return null; // Nunca se alcanza, pero el compilador lo necesita
        }
    }
    
    /**
     * Actualiza un rol existente
     */
    public void actualizarRol(Long id, String nombre, String descripcion, boolean activo) {
        logger.info("Intentando actualizar rol ID {}: {}", id, nombre);
        try {
            // Verificar que el rol existe
            Optional<Rol> rolExistente = rolService.buscarPorId(id);
            if (!rolExistente.isPresent()) {
                throw new ValidationException("id", "El rol no existe");
            }
            
            // Validar datos
            validarDatosRol(nombre, descripcion);
            
            // Actualizar el rol
            Rol rol = rolExistente.get();
            rol.setNombre(nombre.toUpperCase().trim());
            rol.setDescripcion(descripcion.trim());
            rol.setActivo(activo);
            
            rolService.actualizar(rol);
            logger.info("Rol actualizado exitosamente: {}", rol.getNombre());
            
        } catch (ValidationException e) {
            logger.warn("Error de validación al actualizar rol: {}", e.getMessage());
            manejarExcepcionValidacion(e, "actualizar");
        } catch (DatabaseException e) {
            logger.error("Error de base de datos al actualizar rol: {}", e.getMessage());
            manejarExcepcionBD(e, "actualizar");
        }
    
    }


    /*public List<Rol> listarRoles() {
    try {
        return rolService.listarTodos();
    } catch (DatabaseException e) {
        logger.error("Error al listar roles: {}", e.getMessage());
        return new ArrayList<>(); // Retorna lista vacía en lugar de lanzar excepción
    }
}*/
    
       
    /**
     * Busca un rol por ID
     */
   /* public Optional<Rol> buscarRol(Long id) {
        logger.debug("Buscando rol con ID: {}", id);
        try {
            return rolService.buscarPorId(id);
        } catch (DatabaseException e) {
            logger.error("Error al buscar rol {}: {}", id, e.getMessage());
            throw new RuntimeException("No se pudo obtener el rol", e);
        }
    }*/
    
    /**
     * Busca roles por estado activo/inactivo
     */
    public List<Rol> buscarPorEstado(boolean activo) {
        logger.debug("Buscando roles por estado activo: {}", activo);
        try {
            return rolService.buscarPorActivo(activo);
        } catch (DatabaseException e) {
            logger.error("Error al buscar roles por estado: {}", e.getMessage());
            throw new RuntimeException("No se pudieron obtener los roles", e);
        }
    }
    
    /**
     * Busca roles por nombre
     */
    public Optional<Rol> buscarPorNombre(String nombre) {
        logger.debug("Buscando rol por nombre: {}", nombre);
        try {
            return rolService.buscarPorNombre(nombre.toUpperCase().trim());
        } catch (DatabaseException e) {
            logger.error("Error al buscar rol por nombre {}: {}", nombre, e.getMessage());
            throw new RuntimeException("No se pudo obtener el rol", e);
        }
    }
    
    /**
     * Realiza una búsqueda de roles por criterio
     */
 /*   public List<Rol> buscar(String criterio) {
        logger.debug("Buscando roles con criterio: {}", criterio);
        try {
            if (criterio == null || criterio.trim().isEmpty()) {
                return listarRoles();
            }
            return rolService.buscar(criterio.trim());
        } catch (DatabaseException e) {
            logger.error("Error al buscar roles con criterio {}: {}", criterio, e.getMessage());
            throw new RuntimeException("No se pudo realizar la búsqueda", e);
        }
    }*/
 /**
     * Elimina un rol
     */
    public void eliminarRol(Long id) {
        logger.info("Intentando eliminar rol ID: {}", id);
        try {
            // Verificar que el rol existe
            Optional<Rol> rol = rolService.buscarPorId(id);
            if (!rol.isPresent()) {
                throw new ValidationException("id", "El rol no existe");
            }
            
            rolService.eliminar(id);
            logger.info("Rol eliminado exitosamente: {}", rol.get().getNombre());
            
        } catch (ValidationException e) {
            logger.warn("Error de validación al eliminar rol: {}", e.getMessage());
            manejarExcepcionValidacion(e, "eliminar");
        } catch (DatabaseException e) {
            logger.error("Error de base de datos al eliminar rol: {}", e.getMessage());
            manejarExcepcionBD(e, "eliminar");
        }
    }

    /**
     * Valida los datos de un rol
     */
    private void validarDatosRol(String nombre, String descripcion) throws ValidationException {
        ValidationException.Builder validationBuilder = 
            new ValidationException.Builder("Error de validación");
        
        // Validar nombre
        if (nombre == null || nombre.trim().isEmpty()) {
            validationBuilder.addError("nombre", "El nombre del rol es requerido");
        } else if (nombre.length() < 3 || nombre.length() > 50) {
            validationBuilder.addError("nombre", 
                "El nombre del rol debe tener entre 3 y 50 caracteres");
        } else if (!nombre.matches("^[A-Za-z0-9_\\s-]+$")) {
            validationBuilder.addError("nombre", 
                "El nombre del rol solo puede contener letras, números, guiones y espacios");
        }
        
        // Validar descripción
        if (descripcion == null || descripcion.trim().isEmpty()) {
            validationBuilder.addError("descripcion", "La descripción del rol es requerida");
        } else if (descripcion.length() > 255) {
            validationBuilder.addError("descripcion", 
                "La descripción del rol no puede exceder los 255 caracteres");
        }
        
        validationBuilder.throwIfHasErrors();
    }
    
    /**
     * Método de utilidad para verificar si un rol puede ser eliminado
     */
    /*public boolean puedeSerEliminado(Long rolId) {
        try {
            // Verificar que el rol existe
            Optional<Rol> rol = rolService.buscarPorId(rolId);
            if (!rol.isPresent()) {
                return false;
            }
            
            // Aquí podrías agregar más validaciones según tus reglas de negocio
            // Por ejemplo, verificar si tiene usuarios asignados
            
            return true;
        } catch (DatabaseException e) {
            logger.error("Error al verificar si el rol puede ser eliminado: {}", e.getMessage());
            return false;
        }
    }*/
    
    /**
     * Sanitiza el nombre del rol
     */
    private String sanitizarNombre(String nombre) {
        if (nombre == null) return null;
        return nombre.trim()
                    .toUpperCase()
                    .replaceAll("\\s+", " ")
                    .replaceAll("[^A-Z0-9_\\s-]", "");
    }
    
    /**
     * Sanitiza la descripción del rol
     */
    private String sanitizarDescripcion(String descripcion) {
        if (descripcion == null) return null;
        return descripcion.trim()
                         .replaceAll("\\s+", " ")
                         .replaceAll("[<>&\"]", "");
    }
    
    /**
     * Método para manejar excepciones de base de datos
     */
    private void manejarExcepcionBD(DatabaseException e, String operacion) {
        String mensaje = String.format("Error al %s rol: %s", operacion, e.getMessage());
        logger.error(mensaje, e);
        throw new RuntimeException(mensaje, e);
    }
    
    /**
     * Método para manejar excepciones de validación
     */
    private void manejarExcepcionValidacion(ValidationException e, String operacion) {
        String mensaje = String.format("Error al %s rol: %s", operacion, e.getMessage());
        logger.warn(mensaje);
        throw new RuntimeException(mensaje, e);
    }
    
    /**
     * Verifica si un rol es el rol de administrador
     */
   /* public boolean esRolAdmin(Rol rol) {
        return rol != null && "ADMIN".equals(rol.getNombre());
    }*/
    
    /**
     * Obtiene una lista de nombres de roles para mostrar en un combo box
     */
    public List<String> obtenerNombresRoles() {
        try {
            return rolService.listarTodos().stream()
                           .map(Rol::getNombre)
                           .sorted()
                           .collect(java.util.stream.Collectors.toList());
        } catch (DatabaseException e) {
            logger.error("Error al obtener nombres de roles: {}", e.getMessage());
            throw new RuntimeException("No se pudieron obtener los nombres de roles", e);
        }
    }           
}
