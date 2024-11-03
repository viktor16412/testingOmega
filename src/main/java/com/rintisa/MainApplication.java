package com.rintisa;

import com.rintisa.controller.RolController;
import com.rintisa.controller.UsuarioController;
import com.rintisa.dao.impl.RolDao;
import com.rintisa.dao.impl.UsuarioDao;
import com.rintisa.service.impl.RolService;
import com.rintisa.service.impl.UsuarioService;
import com.rintisa.view.LoginView;
import com.rintisa.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rintisa.util.IconManager;
import com.rintisa.view.UsuariosView;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainApplication {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    public static void main(String[] args) {
        try {
            
            // Verificar recursos al inicio
            logger.info("Verificando recursos de la aplicación...");
            IconManager.listAvailableResources();
            
            // Crear directorio de logs si no existe
            Path logPath = Paths.get("logs");
            if (!Files.exists(logPath)) {
                Files.createDirectories(logPath);
            }

            // Configurar Look and Feel del sistema
            configureLookAndFeel();

            // Iniciar la aplicación
            logger.info("Iniciando Sistema RINTISA");
            logger.info("JVM: {}", System.getProperty("java.version"));
            logger.info("OS: {} {}", 
                System.getProperty("os.name"), 
                System.getProperty("os.version"));

            // Verificar conexión a base de datos
            if (DatabaseConfig.testConnection()) {
                logger.info("Conexión a base de datos establecida");
            } else {
                throw new Exception("No se pudo conectar a la base de datos");
            }

            // Inicializar la aplicación
            initializeApplication();

        } catch (Exception e) {
            logger.error("Error fatal al iniciar la aplicación", e);
            showErrorDialog("Error Fatal", e);
            System.exit(1);
        }
    }

    private static void configureLookAndFeel() {
        try {
            // Intentar usar FlatLaf si está disponible
            try {
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
                logger.info("FlatLaf configurado como Look and Feel");
            } catch (ClassNotFoundException e) {
                // Si FlatLaf no está disponible, usar el Look and Feel del sistema
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                logger.info("Look and Feel del sistema configurado");
            }
        } catch (Exception e) {
            logger.warn("No se pudo establecer el Look and Feel", e);
        }
    }

    private static void initializeApplication() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Inicializar DAOs
                RolDao rolDao = new RolDao();
                UsuarioDao usuarioDao = new UsuarioDao();
                logger.info("DAOs inicializados");

                // Inicializar Servicios
                RolService rolService = new RolService(rolDao);
                UsuarioService usuarioService = new UsuarioService(usuarioDao);
                logger.info("Servicios inicializados");

                // Inicializar Controladores
                RolController rolController = new RolController(rolService);
                UsuarioController usuarioController = new UsuarioController(
                    usuarioService, 
                    rolService,
                    rolController
                );
                logger.info("Controladores inicializados");

                // Mostrar ventana de login
                LoginView.mostrar(usuarioController);
                logger.info("Ventana de login mostrada");

            } catch (Exception e) {
                logger.error("Error durante la inicialización", e);
                showErrorDialog("Error de Inicialización", e);
                System.exit(1);
            }
        });
    }
    
    private static void showErrorDialog(String title, Exception e) {
        String message = String.format("%s%n%n%s", e.getMessage(), 
            "Revise los logs para más detalles.");
            
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
        } else {
            System.err.println(title + ": " + message);
        }
    }
}