
package com.rintisa.util;

import com.rintisa.controller.PermisoPantallaController;
import com.rintisa.controller.UsuarioController;
import com.rintisa.model.Pantalla;
import com.rintisa.model.PermisosPantalla;
import com.rintisa.model.Usuario;
import com.rintisa.service.impl.ProductoService;
import com.rintisa.service.interfaces.IPermisosPantallaService;
import com.rintisa.service.interfaces.IRecepcionMercanciaService;
import com.rintisa.view.MainView;
import com.rintisa.view.PermisosPantallaView;
import com.rintisa.view.RecepcionMercanciaView;
import com.rintisa.view.RolesView;
import com.rintisa.view.UsuariosView;
import java.awt.Window;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rintisa.model.enums.EstadoRecepcion;

public class PermissionHandler {
    private static final Logger logger = LoggerFactory.getLogger(PermissionHandler.class);
    
    /**
     * Valida los permisos del usuario y redirige a la pantalla correspondiente
     */
    public static void validarYRedirigir(
            Usuario usuario,
            UsuarioController userController,
            ProductoService productoService,
            PermisoPantallaController permisoController,
            IPermisosPantallaService permisoService,
            IRecepcionMercanciaService recepcionService,
            Window parentWindow) {
        try {
            if (usuario == null || usuario.getRol() == null) {
                throw new IllegalStateException("Usuario o rol no válido");
            }

            // Obtener los permisos del usuario a través del servicio
            List<PermisosPantalla> permisosLista = permisoService.obtenerPorRol(usuario.getRol().getNombre());
            
            // Convertir la lista de permisos a un Map para fácil acceso
            Map<Pantalla, Map<String, Boolean>> permisosUsuario = new HashMap<>();
            for (PermisosPantalla permiso : permisosLista) {
                Map<String, Boolean> permisosPantalla = new HashMap<>();
                permisosPantalla.put("acceso", permiso.isAcceso());
                permisosPantalla.put("edicion", permiso.isEdicion());
                permisosPantalla.put("eliminacion", permiso.isEliminacion());
                permisosUsuario.put(permiso.getPantalla(), permisosPantalla);
            }

            // Validar que el usuario tenga al menos un permiso
            if (permisosUsuario.isEmpty()) {
                throw new IllegalStateException("El usuario no tiene permisos asignados");
            }

            // Determinar la pantalla inicial según el rol
            Pantalla pantallaInicial = determinarPantallaInicial(usuario.getRol().getNombre(), permisosUsuario);

            // Verificar acceso a la pantalla inicial
            Map<String, Boolean> permisosIniciales = permisosUsuario.get(pantallaInicial);
            if (permisosIniciales == null || !permisosIniciales.get("acceso")) {
                throw new IllegalStateException("No tiene acceso a la pantalla inicial");
            }

            // Crear y mostrar MainView con los controladores necesarios
            SwingUtilities.invokeLater(() -> {
                try {
                    MainView mainView = new MainView(
                        userController,
                        productoService,
                        permisoController,
                        permisoService,
                        recepcionService
                    );

                    mainView.setVisible(true);

                    // Después de mostrar MainView, navegar a la pantalla inicial
                    mainView.navegarAPantalla(pantallaInicial);

                    // Cerrar la ventana anterior si existe
                    if (parentWindow != null) {
                        parentWindow.dispose();
                    }

                } catch (Exception e) {
                    logger.error("Error al crear vista principal", e);
                    mostrarError(parentWindow, "Error al cargar la pantalla: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Error en validación de permisos", e);
            mostrarError(parentWindow, "Error de permisos: " + e.getMessage());
        }
    }

    /**
     * Determina la pantalla inicial según el rol del usuario
     */
    private static Pantalla determinarPantallaInicial(String rolNombre, 
            Map<Pantalla, Map<String, Boolean>> permisos) {
        // Por defecto, retornar la primera pantalla a la que tenga acceso
        if ("ADMIN".equalsIgnoreCase(rolNombre)) {
            if (permisos.containsKey(Pantalla.USUARIOS) && 
                permisos.get(Pantalla.USUARIOS).get("acceso")) {
                return Pantalla.USUARIOS;
            }
        } else if ("ALMACEN".equalsIgnoreCase(rolNombre)) {
            if (permisos.containsKey(Pantalla.RECEPCION) && 
                permisos.get(Pantalla.RECEPCION).get("acceso")) {
                return Pantalla.RECEPCION;
            }
        }

        // Si no se encuentra una pantalla específica para el rol, buscar la primera con acceso
        return permisos.entrySet().stream()
            .filter(e -> e.getValue().get("acceso"))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No se encontró una pantalla válida"));
    }

    private static void mostrarError(Window parent, String mensaje) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(parent, mensaje, "Error", 
                JOptionPane.ERROR_MESSAGE));
    }
}