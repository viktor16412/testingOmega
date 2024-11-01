/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.rintisa;

import static com.mysql.cj.conf.PropertyKey.logger;
import com.rintisa.controller.RolController;
import com.rintisa.controller.UsuarioController;
import com.rintisa.dao.impl.RolDao;
import com.rintisa.dao.impl.UsuarioDao;
import com.rintisa.dao.interfaces.IRolDao;
import com.rintisa.dao.interfaces.IUsuarioDao;
import com.rintisa.service.impl.RolService;
import com.rintisa.service.impl.UsuarioService;
import com.rintisa.service.interfaces.IRolService;
import com.rintisa.service.interfaces.IUsuarioService;
import com.rintisa.view.LoginView;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainApplication {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    public static void main(String[] args) {
        try {
            // Inicializar DAOs
            IRolDao rolDao = new RolDao();
            IUsuarioDao usuarioDao = new UsuarioDao();
            
            // Inicializar Servicios
            IRolService rolService = new RolService(rolDao);
            IUsuarioService usuarioService = new UsuarioService(usuarioDao);
            
            // Inicializar Controladores
            RolController rolController = new RolController(rolService);
            UsuarioController usuarioController = new UsuarioController(
                usuarioService, 
                rolService,
                rolController
            );
            
            // Configurar Look and Feel del sistema
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                logger.warn("No se pudo establecer el Look and Feel del sistema", e);
            }
            
            // Iniciar la aplicación
            SwingUtilities.invokeLater(() -> {
                try {
                    LoginView.mostrar(usuarioController);
                } catch (Exception e) {
                    logger.error("Error al iniciar la aplicación", e);
                    JOptionPane.showMessageDialog(null,
                        "Error al iniciar la aplicación: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error fatal al inicializar la aplicación", e);
            JOptionPane.showMessageDialog(null,
                "Error fatal al inicializar la aplicación: " + e.getMessage(),
                "Error Fatal",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}