
package com.rintisa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoggingInitializer {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInitializer.class);

    public static void initializeLogging() {
        try {
            // Crear directorio de logs si no existe
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            // Redirigir Java Util Logging a SLF4J
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            // Verificar permisos de escritura
            File logFile = new File("logs/rintisa.log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            if (!logFile.canWrite()) {
                System.err.println("ADVERTENCIA: No hay permisos de escritura para el archivo de log");
            }

            logger.info("Sistema de logging inicializado correctamente");
            logger.info("Directorio de logs: {}", logDir.toAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("Error al inicializar el sistema de logging: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void logSystemInfo() {
        logger.info("=== Información del Sistema ===");
        logger.info("OS: {} version {}", 
            System.getProperty("os.name"), 
            System.getProperty("os.version"));
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("User home: {}", System.getProperty("user.home"));
        logger.info("Working directory: {}", System.getProperty("user.dir"));
        logger.info("File encoding: {}", System.getProperty("file.encoding"));
        logger.info("Available processors: {}", 
            Runtime.getRuntime().availableProcessors());
        logger.info("Max memory: {} MB", 
            Runtime.getRuntime().maxMemory() / (1024 * 1024));
        logger.info("===========================");
    }
}
