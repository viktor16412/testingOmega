
package com.rintisa.security;

import com.rintisa.model.Pantalla;
import com.rintisa.service.interfaces.IPermisosPantallaService;
import com.rintisa.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PermisosPantallaManager {
    private static final Logger logger = LoggerFactory.getLogger(PermisosPantallaManager.class);
    private final IPermisosPantallaService permisosPantallaService;
    private final Map<String, Set<String>> permisosCache;

    public PermisosPantallaManager(IPermisosPantallaService permisosPantallaService) {
        this.permisosPantallaService = permisosPantallaService;
        this.permisosCache = new ConcurrentHashMap<>();
    }

    public boolean tieneAcceso(String rolNombre, Pantalla pantalla) {
        try {
            return verificarPermiso(rolNombre, pantalla, "acceso");
        } catch (DatabaseException e) {
            logger.error("Error al verificar acceso para rol {} en pantalla {}", 
                rolNombre, pantalla, e);
            return false;
        }
    }

    public boolean puedeEditar(String rolNombre, Pantalla pantalla) {
        try {
            return verificarPermiso(rolNombre, pantalla, "edicion");
        } catch (DatabaseException e) {
            logger.error("Error al verificar permiso de edición para rol {} en pantalla {}", 
                rolNombre, pantalla, e);
            return false;
        }
    }

    public boolean puedeEliminar(String rolNombre, Pantalla pantalla) {
        try {
            return verificarPermiso(rolNombre, pantalla, "eliminacion");
        } catch (DatabaseException e) {
            logger.error("Error al verificar permiso de eliminación para rol {} en pantalla {}", 
                rolNombre, pantalla, e);
            return false;
        }
    }

    private boolean verificarPermiso(String rolNombre, Pantalla pantalla, String tipoPermiso) 
        throws DatabaseException {
        // Verificar cache
        String key = String.format("%s_%s_%s", rolNombre, pantalla.name(), tipoPermiso);
        if (permisosCache.containsKey(key)) {
            return permisosCache.get(key).contains(tipoPermiso);
        }

        // Si no está en cache, consultar BD
        boolean tienePermiso = permisosPantallaService.verificarPermiso(
            rolNombre, pantalla, tipoPermiso);
        
        // Guardar en cache
        Set<String> permisos = permisosCache.computeIfAbsent(key, k -> new HashSet<>());
        if (tienePermiso) {
            permisos.add(tipoPermiso);
        }

        return tienePermiso;
    }

    public void limpiarCache() {
        permisosCache.clear();
    }
}
