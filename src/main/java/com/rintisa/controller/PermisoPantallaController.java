
package com.rintisa.controller;

import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ReportException;
import com.rintisa.exception.ValidationException;        
import com.rintisa.model.Pantalla;
import com.rintisa.model.PermisosPantalla;
import com.rintisa.model.Rol;
import com.rintisa.model.Usuario;
import com.rintisa.service.impl.PermisosPantallaService;
import com.rintisa.service.impl.RolService;
import com.rintisa.service.interfaces.IPermisosPantallaService;
import com.rintisa.service.interfaces.IRolService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermisoPantallaController {
    private final IPermisosPantallaService permisosService;
    private final IRolService rolService;
    private final Usuario usuarioActual;
    private static final Logger logger = LoggerFactory.getLogger(PermisoPantallaController.class);


   public PermisoPantallaController(IPermisosPantallaService permisosService, 
                                   IRolService rolService,
                                   Usuario usuarioActual) {
        this.permisosService = permisosService;
        this.rolService = rolService;
        this.usuarioActual = usuarioActual;
    }

    public PermisoPantallaController(PermisosPantallaService permisosPantallaService, RolService rolService) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    
    public void actualizarPermiso(String rolNombre, String pantallaNombre, int tipoPermiso, boolean valor) 
            throws DatabaseException {
         try {
            logger.debug("Actualizando permiso - Rol: {}, Pantalla: {}, Tipo: {}, Valor: {}", 
                rolNombre, pantallaNombre, tipoPermiso, valor);

            // Validar parámetros
            if (rolNombre == null || pantallaNombre == null) {
                throw new ValidationException("Los parámetros no pueden ser null");
            }

            // Convertir nombre de pantalla a enum
            Pantalla pantalla = Pantalla.valueOf(pantallaNombre);

            // Obtener o crear el permiso
            PermisosPantalla permiso = permisosService.obtenerPorRolYPantalla(rolNombre, pantalla);
            if (permiso == null) {
                permiso = new PermisosPantalla();
                permiso.setRolNombre(rolNombre);
                permiso.setPantalla(pantalla);
            }

            // Establecer el usuario de modificación
            permiso.setUsuarioModificacion(usuarioActual.getId());
            permiso.setFechaModificacion(LocalDateTime.now());

            // Actualizar el permiso según el tipo
            switch (tipoPermiso) {
                case 0: // Acceso
                    permiso.setAcceso(valor);
                    if (!valor) {
                        // Si se quita el acceso, quitar también edición y eliminación
                        permiso.setEdicion(false);
                        permiso.setEliminacion(false);
                    }
                    break;
                case 1: // Edición
                    permiso.setEdicion(valor);
                    if (valor) {
                        // Si se da permiso de edición, asegurar que tenga acceso
                        permiso.setAcceso(true);
                    }
                    break;
                case 2: // Eliminación
                    permiso.setEliminacion(valor);
                    if (valor) {
                        // Si se da permiso de eliminación, asegurar que tenga acceso
                        permiso.setAcceso(true);
                    }
                    break;
                default:
                    throw new ValidationException("Tipo de permiso inválido: " + tipoPermiso);
            }

            // Guardar el permiso
            permisosService.actualizar(permiso);

            logger.info("Permiso actualizado exitosamente");
        } catch (Exception e) {
            logger.error("Error al actualizar permiso", e);
            throw new DatabaseException("Error al actualizar permiso: " + e.getMessage());
        }
    }
      
    public List<Rol> getRoles() throws DatabaseException {
         try {
            return rolService.listarTodos();
        } catch (Exception e) {
            logger.error("Error al obtener roles", e);
            throw new DatabaseException("Error al obtener roles: " + e.getMessage());
        }
    }

    public List<PermisosPantalla> obtenerTodosLosPermisos() throws DatabaseException {
        try {
            return permisosService.obtenerTodos();
        } catch (Exception e) {
            logger.error("Error al obtener todos los permisos", e);
            throw new DatabaseException("Error al obtener permisos: " + e.getMessage());
        }
    }

    public List<PermisosPantalla> obtenerPermisosPorRol(String rolNombre) throws DatabaseException {
         try {
            return permisosService.obtenerPorRol(rolNombre);
        } catch (Exception e) {
            logger.error("Error al obtener permisos por rol", e);
            throw new DatabaseException("Error al obtener permisos: " + e.getMessage());
        }
    }

    

    public void guardarCambios() throws DatabaseException {
        try {
            logger.debug("Guardando cambios en permisos");
            permisosService.guardarCambios();
            logger.info("Cambios guardados exitosamente");
        } catch (Exception e) {
            logger.error("Error al guardar cambios", e);
            throw new DatabaseException("Error al guardar cambios: " + e.getMessage());
        }
    }
    
     public Map<Pantalla, Set<String>> obtenerPermisosUsuario(String rolNombre) throws DatabaseException {
        try {
            Map<Pantalla, Set<String>> permisosUsuario = new HashMap<>();
            List<PermisosPantalla> permisos = permisosService.obtenerPorRol(rolNombre);
            
            for (PermisosPantalla permiso : permisos) {
                Set<String> tiposPermiso = new HashSet<>();
                if (permiso.isAcceso()) {
                    tiposPermiso.add("acceso");
                }
                if (permiso.isEdicion()) {
                    tiposPermiso.add("edicion");
                }
                if (permiso.isEliminacion()) {
                    tiposPermiso.add("eliminacion");
                }
                permisosUsuario.put(permiso.getPantalla(), tiposPermiso);
            }
            
            return permisosUsuario;
        } catch (Exception e) {
            logger.error("Error al obtener permisos del usuario", e);
            throw new DatabaseException("Error al obtener permisos: " + e.getMessage());
        }
    }
}