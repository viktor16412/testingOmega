
package com.rintisa.model;

import com.rintisa.dao.impl.ProveedorDao;
import com.rintisa.dao.impl.UsuarioDao;
import com.rintisa.exception.DatabaseException;
import java.sql.SQLException;


public class RecepcionMercanciaHelper {
    private final ProveedorDao proveedorDao;
    private final UsuarioDao usuarioDao;

    public RecepcionMercanciaHelper(ProveedorDao proveedorDao, UsuarioDao usuarioDao) {
        this.proveedorDao = proveedorDao;
        this.usuarioDao = usuarioDao;
    }

    /**
     * Crea una nueva recepción con la información básica
     */
    public RecepcionMercancia crearRecepcion(
            String numeroDocumento, 
            String numeroOrdenCompra, 
            String numeroGuiaRemision, 
            Long proveedorId, 
            Long usuarioId,
            Long responsableId) throws DatabaseException {
        
        // Buscar proveedor
        Proveedor proveedor = proveedorDao.findById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + proveedorId));
        // Buscar usuario
        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
        // Buscar responsable
        Usuario responsable = usuarioDao.findById(responsableId)
                .orElseThrow(() -> new IllegalArgumentException("Responsable no encontrado: " + responsableId));
        // Crear recepción
        RecepcionMercancia recepcion = new RecepcionMercancia();
        recepcion.generarNumeroRecepcion();
        recepcion.setNumeroDocumento(numeroDocumento);
        recepcion.setNumeroOrdenCompra(numeroOrdenCompra);
        recepcion.setNumeroGuiaRemision(numeroGuiaRemision);
        recepcion.setProveedor(proveedor);
        recepcion.setUsuario(usuario);
        recepcion.setResponsable(responsable);
        return recepcion;
    }

      // Cargar información completa del responsable
    public void cargarResponsable(RecepcionMercancia recepcion) throws DatabaseException {
        if (recepcion.getResponsableId() != null) {
            usuarioDao.findById(recepcion.getResponsableId())
                    .ifPresent(recepcion::setResponsable);
        }
    }
    
    
    /**
     * Carga la información completa del proveedor
     */
    public void cargarProveedor(RecepcionMercancia recepcion) throws DatabaseException {
        if (recepcion.getProveedorId() != null) {
            proveedorDao.findById(recepcion.getProveedorId())
                    .ifPresent(recepcion::setProveedor);
        }
    }

    /**
     * Carga la información completa del usuario
     */
    public void cargarUsuario(RecepcionMercancia recepcion) throws DatabaseException {
        if (recepcion.getUsuarioId() != null) {
            usuarioDao.findById(recepcion.getUsuarioId())
                    .ifPresent(recepcion::setUsuario);
        }
    }
    
}
