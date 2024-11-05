
package com.rintisa.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogUtils {
    private static final Logger logger = LoggerFactory.getLogger(LogUtils.class);
    private static boolean initialized = false;

    public static void initializeLogging() {
        if (initialized) {
            return;
        }

        try {
            // Crear directorio de logs
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            // Verificar permisos
            File logFile = new File("logs/rintisa.log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            if (!logFile.canWrite()) {
                System.err.println("ERROR: No se puede escribir en el archivo de log");
                return;
            }

            initialized = true;

            // Log inicial
            logger.info("=== Inicio de la aplicación ===");
            logger.info("Sistema Operativo: {} {}", 
                System.getProperty("os.name"), 
                System.getProperty("os.version"));
            logger.info("Java Version: {}", System.getProperty("java.version"));
            logger.info("User Home: {}", System.getProperty("user.home"));
            logger.info("Working Directory: {}", System.getProperty("user.dir"));
            logger.info("Log Directory: {}", logDir.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error al inicializar logging: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void logError(String message, Throwable throwable) {
        if (throwable != null) {
            logger.error(message, throwable);
            // Log causa raíz
            Throwable rootCause = getRootCause(throwable);
            if (rootCause != throwable) {
                logger.error("Causa raíz: ", rootCause);
            }
        } else {
            logger.error(message);
        }
    }

    private static Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}