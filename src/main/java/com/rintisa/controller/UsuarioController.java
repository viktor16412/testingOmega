/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.controller;

import com.rintisa.model.Usuario;
import com.rintisa.model.Rol;
import com.rintisa.service.interfaces.IUsuarioService;
import com.rintisa.service.interfaces.IRolService;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public class UsuarioController {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    
    private final IUsuarioService usuarioService;
    private final RolController rolController;
    private final IRolService rolService;
    private Usuario usuarioActual;
    
    
    // Constructor
    public UsuarioController(IUsuarioService usuarioService, IRolService rolService, RolController rolController) {
        
        this.usuarioService = usuarioService;
        this.rolService = rolService;
        this.rolController = rolController;
    }
      // Método getter para RolController
    public RolController getRolController() {
        return rolController;
    }
    
    /**
     * Crea un nuevo usuario
     */
    public Usuario crearUsuario(String username, String password, String nombre, 
                              String apellido, String email, Long rolId) {
        try {
            // Validar que el rol existe
            Optional<Rol> rol = rolService.buscarPorId(rolId);
            if (!rol.isPresent()) {
                throw new ValidationException("rolId", "El rol seleccionado no existe");
            }
            
            // Crear el usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(username);
            nuevoUsuario.setPassword(password);
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellido(apellido);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setRol(rol.get());
            
            return usuarioService.crear(nuevoUsuario);
            
        } catch (ValidationException | DatabaseException e) {
            logger.error("Error al crear usuario: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear el usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza un usuario existente
     */
    public void actualizarUsuario(Long id, String nombre, String apellido, 
                                String email, Long rolId, boolean activo) {
        try {
            // Buscar usuario existente
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                throw new ValidationException("id", "El usuario no existe");
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Validar que el rol existe
            if (rolId != null && !rolId.equals(usuario.getRol().getId())) {
                Optional<Rol> rol = rolService.buscarPorId(rolId);
                if (!rol.isPresent()) {
                    throw new ValidationException("rolId", "El rol seleccionado no existe");
                }
                usuario.setRol(rol.get());
            }
            
            // Actualizar datos
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(email);
            usuario.setActivo(activo);
            
            usuarioService.actualizar(usuario);
            
        } catch (ValidationException | DatabaseException e) {
            logger.error("Error al actualizar usuario: {}", e.getMessage());
            throw new RuntimeException("No se pudo actualizar el usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene todos los usuarios
     */
    public List<Usuario> listarUsuarios() {
        try {
            return usuarioService.listarTodos();
        } catch (DatabaseException e) {
            logger.error("Error al listar usuarios: {}", e.getMessage());
            throw new RuntimeException("No se pudieron obtener los usuarios", e);
        }
    }
    
    /**
     * Busca un usuario por ID
     */
    public Optional<Usuario> buscarUsuario(Long id) {
        try {
            return usuarioService.buscarPorId(id);
        } catch (DatabaseException e) {
            logger.error("Error al buscar usuario {}: {}", id, e.getMessage());
            throw new RuntimeException("No se pudo obtener el usuario", e);
        }
    }
    
    /**
     * Busca usuarios por estado activo/inactivo
     */
    public List<Usuario> buscarPorEstado(boolean activo) {
        try {
            return usuarioService.buscarPorActivo(activo);
        } catch (DatabaseException e) {
            logger.error("Error al buscar usuarios por estado: {}", e.getMessage());
            throw new RuntimeException("No se pudieron obtener los usuarios", e);
        }
    }
    
    /**
     * Verifica las credenciales de un usuario
     */
    public boolean autenticar(String username, String password) {
        try {
            return usuarioService.autenticar(username, password);
        } catch (DatabaseException e) {
            logger.error("Error durante la autenticación: {}", e.getMessage());
            throw new RuntimeException("Error en la autenticación", e);
        }
    }
    
    /**
     * Busca un usuario por nombre de usuario
     */
    public Optional<Usuario> buscarPorUsername(String username) {
        try {
            return usuarioService.buscarPorUsername(username);
        } catch (DatabaseException e) {
            logger.error("Error al buscar usuario por username {}: {}", username, e.getMessage());
            throw new RuntimeException("No se pudo obtener el usuario", e);
        }
    }
   
    public void actualizarUltimoAcceso(Long userId) {
        try {
            usuarioService.actualizarUltimoAcceso(userId);
        } catch (DatabaseException e) {
            logger.error("Error al actualizar último acceso para usuario {}: {}", 
                        userId, e.getMessage());
            // No relanzamos la excepción ya que este error no debería interrumpir el flujo
        }
    }
    
    
   public void setUsuarioActual(Usuario usuario) {
    this.usuarioActual = usuario;
    try {
        // Actualizar último acceso
        if (usuario != null) {
            actualizarUltimoAcceso(usuario.getId());
        }
    } catch (Exception e) {
        logger.warn("No se pudo actualizar el último acceso del usuario", e);
        }
    }
 
   
   
   public Usuario getUsuarioActual() {
    return usuarioActual;
    }
   
   public boolean login(String username, String password) {
    try {
        if (autenticar(username, password)) {
            Optional<Usuario> usuario = buscarPorUsername(username);
            if (usuario.isPresent()) {
                setUsuarioActual(usuario.get());
                return true;
            }
        }
        return false;
    } catch (Exception e) {
        logger.error("Error durante el login", e);
        throw new RuntimeException("Error durante el login: " + e.getMessage());
    }
}

/**
 * Cierra la sesión del usuario actual
 */
public void logout() {
    usuarioActual = null;
}
    
    
    
    public void eliminarUsuario(Long id) {
        try {
            usuarioService.eliminar(id);
        } catch (DatabaseException e) {
            logger.error("Error al eliminar usuario {}: {}", id, e.getMessage());
            throw new RuntimeException("No se pudo eliminar el usuario", e);
        }
    }
    
    /**
     * Cambia la contraseña de un usuario
     */
    public void cambiarPassword(Long userId, String oldPassword, String newPassword) {
        try {
            usuarioService.cambiarPassword(userId, oldPassword, newPassword);
        } catch (ValidationException | DatabaseException e) {
            logger.error("Error al cambiar contraseña del usuario {}: {}", userId, e.getMessage());
            throw new RuntimeException("No se pudo cambiar la contraseña: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza el último acceso del usuario
     */
    public void registrarAcceso(Long userId) {
        try {
            usuarioService.actualizarUltimoAcceso(userId);
        } catch (DatabaseException e) {
            logger.error("Error al registrar acceso del usuario {}: {}", userId, e.getMessage());
            // No lanzamos la excepción ya que esto no debería interrumpir el flujo normal
        }
    }

    /**
     * Valida los datos de un usuario antes de crearlo o actualizarlo
     */
    private void validarDatosUsuario(String username, String password, String email) 
            throws ValidationException {
        ValidationException.Builder validationBuilder = 
            new ValidationException.Builder("Error de validación");

        // Validar username
        if (username == null || username.trim().isEmpty()) {
            validationBuilder.addError("username", "El nombre de usuario es requerido");
        } else if (username.length() < 4 || username.length() > 20) {
            validationBuilder.addError("username", 
                "El nombre de usuario debe tener entre 4 y 20 caracteres");
        }

        // Validar password si está presente (puede ser null en actualizaciones)
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 8) {
                validationBuilder.addError("password", 
                    "La contraseña debe tener al menos 8 caracteres");
            }
        }

        // Validar email
        if (email == null || email.trim().isEmpty()) {
            validationBuilder.addError("email", "El email es requerido");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            validationBuilder.addError("email", "El formato del email no es válido");
        }

        validationBuilder.throwIfHasErrors();
    }

    /**
     * Método de utilidad para construir un mensaje de error
     */
    private String construirMensajeError(String operacion, String detalle) {
        return String.format("Error al %s usuario: %s", operacion, detalle);
    }

    /**
     * Método para manejar excepciones de base de datos
     */
    private void manejarExcepcionBD(DatabaseException e, String operacion) {
        String mensaje = construirMensajeError(operacion, e.getMessage());
        logger.error(mensaje, e);
        throw new RuntimeException(mensaje, e);
    }

    /**
     * Método para manejar excepciones de validación
     */
    private void manejarExcepcionValidacion(ValidationException e, String operacion) {
        String mensaje = construirMensajeError(operacion, e.getMessage());
        logger.warn(mensaje);
        throw new RuntimeException(mensaje, e);
    }

    /**
     * Valida si un usuario tiene permisos de administrador
     */
    public boolean esAdministrador(Usuario usuario) {
        return usuario != null && 
               usuario.getRol() != null && 
               "ADMIN".equalsIgnoreCase(usuario.getRol().getNombre());
    } 
    
    
    
}
