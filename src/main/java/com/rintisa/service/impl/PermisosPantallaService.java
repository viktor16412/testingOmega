
package com.rintisa.service.impl;

import com.rintisa.dao.interfaces.IPermisosPantallaDao;
import com.rintisa.service.interfaces.IPermisosPantallaService;
import com.rintisa.model.PermisosPantalla;
import com.rintisa.model.Pantalla;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class PermisosPantallaService implements IPermisosPantallaService {
    private static final Logger logger = LoggerFactory.getLogger(PermisosPantallaService.class);
    
    private final IPermisosPantallaDao permisosPantallaDao;
    private final Map<String, List<PermisosPantalla>> cambiosPendientes;

    public PermisosPantallaService(IPermisosPantallaDao permisosPantallaDao) {
        this.permisosPantallaDao = permisosPantallaDao;
        this.cambiosPendientes = new ConcurrentHashMap<>();
    }

    @Override
    public PermisosPantalla crear(PermisosPantalla permiso) throws DatabaseException, ValidationException {
        logger.debug("Creando nuevo permiso de pantalla para rol: {}", permiso.getRolNombre());
        
        validarPermisos(permiso);
        return permisosPantallaDao.save(permiso);
    }

    @Override
    public void actualizar(PermisosPantalla permiso) throws DatabaseException, ValidationException {
         try {
            logger.debug("Actualizando permiso para rol {} y pantalla {}", 
                permiso.getRolNombre(), permiso.getPantalla());

            // Obtener o crear lista de cambios pendientes para el rol
            List<PermisosPantalla> permisos = cambiosPendientes.computeIfAbsent(
                permiso.getRolNombre(), 
                k -> new ArrayList<>()
            );

            // Actualizar o agregar el permiso
            boolean encontrado = false;
            for (int i = 0; i < permisos.size(); i++) {
                PermisosPantalla p = permisos.get(i);
                if (p.getPantalla() == permiso.getPantalla()) {
                    permisos.set(i, permiso);
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                permisos.add(permiso);
            }

            logger.debug("Permiso actualizado en memoria: {}", permiso);
        } catch (Exception e) {
            logger.error("Error al actualizar permiso", e);
            throw new DatabaseException("Error al actualizar permiso: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(Long id) throws DatabaseException {
        logger.debug("Eliminando permiso de pantalla ID: {}", id);
        permisosPantallaDao.delete(id);
    }

    @Override
    public List<PermisosPantalla> obtenerTodos() throws DatabaseException {
         try {
            return permisosPantallaDao.findAll();
        } catch (Exception e) {
            logger.error("Error al obtener todos los permisos", e);
            throw new DatabaseException("Error al obtener permisos: " + e.getMessage());
        }
    }

    @Override
    public List<PermisosPantalla> obtenerPorRol(String rolNombre) throws DatabaseException {
        logger.debug("Obteniendo permisos para rol: {}", rolNombre);
        
       try {
            // Verificar si hay cambios pendientes
            if (cambiosPendientes.containsKey(rolNombre)) {
                return new ArrayList<>(cambiosPendientes.get(rolNombre));
            }

            // Si no hay cambios pendientes, obtener de la base de datos
            List<PermisosPantalla> permisos = permisosPantallaDao.findByRol(rolNombre);
            
            // Almacenar en caché
            cambiosPendientes.put(rolNombre, new ArrayList<>(permisos));
            
            return permisos;
        } catch (Exception e) {
            logger.error("Error al obtener permisos por rol", e);
            throw new DatabaseException("Error al obtener permisos: " + e.getMessage());
        }
    }

    @Override
    public PermisosPantalla obtenerPorRolYPantalla(String rolNombre, Pantalla pantalla) 
            throws DatabaseException {
        logger.debug("Obteniendo permiso específico para rol: {} y pantalla: {}", 
                    rolNombre, pantalla);
        
        try {
            // Verificar cambios pendientes
            if (cambiosPendientes.containsKey(rolNombre)) {
                return cambiosPendientes.get(rolNombre).stream()
                    .filter(p -> p.getPantalla() == pantalla)
                    .findFirst()
                    .orElse(null);
            }

            return permisosPantallaDao.findByRolAndPantalla(rolNombre, pantalla);
        } catch (Exception e) {
            logger.error("Error al obtener permiso por rol y pantalla", e);
            throw new DatabaseException("Error al obtener permiso: " + e.getMessage());
        }
    }

    @Override
    public boolean verificarPermiso(String rolNombre, Pantalla pantalla, String tipoPermiso) 
            throws DatabaseException {
        try {
            PermisosPantalla permiso = obtenerPorRolYPantalla(rolNombre, pantalla);
            if (permiso == null) {
                return false;
            }

            switch (tipoPermiso.toLowerCase()) {
                case "acceso":
                    return permiso.isAcceso();
                case "edicion":
                    return permiso.isEdicion();
                case "eliminacion":
                    return permiso.isEliminacion();
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error al verificar permiso", e);
            throw new DatabaseException("Error al verificar permiso: " + e.getMessage());
        }
    }

    @Override
    public Map<Pantalla, Map<String, Boolean>> obtenerMatrizPermisos(String rolNombre) 
            throws DatabaseException {
        List<PermisosPantalla> permisos = obtenerPorRol(rolNombre);
        Map<Pantalla, Map<String, Boolean>> matriz = new EnumMap<>(Pantalla.class);

        for (PermisosPantalla permiso : permisos) {
            Map<String, Boolean> permisosDetalle = new HashMap<>();
            permisosDetalle.put("acceso", permiso.isAcceso());
            permisosDetalle.put("edicion", permiso.isEdicion());
            permisosDetalle.put("eliminacion", permiso.isEliminacion());
            
            matriz.put(permiso.getPantalla(), permisosDetalle);
        }

        return matriz;
    }

    @Override
    public void actualizarPermiso(String rolNombre, String pantallaNombre, int tipoPermiso, 
                                 boolean valor) throws DatabaseException {
        logger.debug("Actualizando permiso - Rol: {}, Pantalla: {}, Tipo: {}, Valor: {}", 
                    rolNombre, pantallaNombre, tipoPermiso, valor);

        try {
            // Obtener o crear lista de permisos para el rol
            List<PermisosPantalla> permisosRol = cambiosPendientes.computeIfAbsent(
                rolNombre,
                k -> {
                    try {
                        return new ArrayList<>(obtenerPorRol(k));
                    } catch (DatabaseException e) {
                        logger.error("Error al obtener permisos del rol: {}", k, e);
                        return new ArrayList<>();
                    }
                }
            );

            // Buscar el permiso específico
            PermisosPantalla permiso = permisosRol.stream()
                .filter(p -> p.getPantalla().getNombre().equals(pantallaNombre))
                .findFirst()
                .orElseGet(() -> {
                    // Si no existe, crear nuevo
                    PermisosPantalla nuevo = new PermisosPantalla();
                    nuevo.setRolNombre(rolNombre);
                    nuevo.setPantalla(Pantalla.valueOf(pantallaNombre));
                    permisosRol.add(nuevo);
                    return nuevo;
                });

            // Actualizar el permiso específico
            switch (tipoPermiso) {
                case 0: // acceso
                    permiso.setAcceso(valor);
                    break;
                case 1: // edición
                    permiso.setEdicion(valor);
                    break;
                case 2: // eliminación
                    permiso.setEliminacion(valor);
                    break;
                default:
                    throw new ValidationException("tipoPermiso", 
                        "Tipo de permiso inválido: " + tipoPermiso);
            }

            permiso.actualizarFechaModificacion();

        } catch (Exception e) {
            throw new DatabaseException("Error al actualizar permiso: " + e.getMessage());
        }
    }

    // Método auxiliar para manejar excepciones en el lambda
    private List<PermisosPantalla> obtenerPermisosRolSeguro(String rolNombre) {
        try {
            return obtenerPorRol(rolNombre);
        } catch (DatabaseException e) {
            logger.error("Error al obtener permisos del rol: {}", rolNombre, e);
            return new ArrayList<>();
        }
    }


    @Override
    public void actualizarPermisosRol(String rolNombre, 
                                     Map<Pantalla, Map<String, Boolean>> permisos) 
            throws DatabaseException {
        List<PermisosPantalla> nuevosPermisos = new ArrayList<>();

        permisos.forEach((pantalla, valores) -> {
            PermisosPantalla permiso = new PermisosPantalla();
            permiso.setRolNombre(rolNombre);
            permiso.setPantalla(pantalla);
            permiso.setAcceso(valores.get("acceso"));
            permiso.setEdicion(valores.get("edicion"));
            permiso.setEliminacion(valores.get("eliminacion"));
            nuevosPermisos.add(permiso);
        });

        cambiosPendientes.put(rolNombre, nuevosPermisos);
    }

    @Override
    public void guardarCambios() throws DatabaseException {
        logger.debug("Guardando cambios pendientes en permisos");
        
         try {
            logger.debug("Guardando cambios pendientes");
            
            // Verificar si hay cambios pendientes
            if (cambiosPendientes.isEmpty()) {
                logger.debug("No hay cambios pendientes para guardar");
                return;
            }

            // Guardar cambios por rol
            for (Map.Entry<String, List<PermisosPantalla>> entry : cambiosPendientes.entrySet()) {
                String rolNombre = entry.getKey();
                List<PermisosPantalla> permisos = entry.getValue();

                // Actualizar permisos en la base de datos
                permisosPantallaDao.updatePermisosByRol(rolNombre, permisos);
                logger.debug("Guardados permisos para rol: {}", rolNombre);
            }

            // Limpiar cambios pendientes
            cambiosPendientes.clear();
            logger.info("Todos los cambios guardados exitosamente");
        } catch (Exception e) {
            logger.error("Error al guardar cambios", e);
            throw new DatabaseException("Error al guardar cambios: " + e.getMessage());
        }
    }

    @Override
    public void descartarCambios() {
        logger.debug("Descartando cambios pendientes en permisos");
        cambiosPendientes.clear();
    }

    @Override
    public void validarPermisos(PermisosPantalla permiso) throws ValidationException {
        logger.debug("Validando permisos para: {}", permiso);
        
        List<String> errores = new ArrayList<>();

        if (permiso.getRolNombre() == null || permiso.getRolNombre().trim().isEmpty()) {
            errores.add("El rol es requerido");
        }

        if (permiso.getPantalla() == null) {
            errores.add("La pantalla es requerida");
        }

        // Validar dependencias de permisos
        if (permiso.isEdicion() && !permiso.isAcceso()) {
            errores.add("No se puede tener permiso de edición sin permiso de acceso");
        }

        if (permiso.isEliminacion() && !permiso.isAcceso()) {
            errores.add("No se puede tener permiso de eliminación sin permiso de acceso");
        }

        if (!errores.isEmpty()) {
            throw new ValidationException("Errores de validación: " + String.join(", ", errores));
        }
    }
}