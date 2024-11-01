/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;
    private static final Properties properties = new Properties();
    
    static {
        try {
            loadProperties();
            initializeDataSource();
        } catch (IOException e) {
            logger.error("Error fatal al cargar configuración de base de datos", e);
            throw new RuntimeException("No se pudo inicializar la conexión a la base de datos", e);
        }
    }
    
    private static void loadProperties() throws IOException {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new IOException("No se pudo encontrar database.properties");
            }
            properties.load(input);
            logger.info("Propiedades de base de datos cargadas correctamente");
        }
    }
    
    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Configuración básica
        config.setJdbcUrl(properties.getProperty("db.url"));
        config.setUsername(properties.getProperty("db.username", "root"));
        // No configuramos contraseña si está vacía
        String password = properties.getProperty("db.password");
        if (password != null && !password.trim().isEmpty()) {
            config.setPassword(password);
        }
        
        // Configuración del pool
        config.setMinimumIdle(Integer.parseInt(
            properties.getProperty("db.pool.minIdle", "5")));
        config.setMaximumPoolSize(Integer.parseInt(
            properties.getProperty("db.pool.maxSize", "20")));
        config.setIdleTimeout(Long.parseLong(
            properties.getProperty("db.pool.timeout", "300000")));
        
        // Configuración adicional
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        dataSource = new HikariDataSource(config);
        logger.info("Pool de conexiones inicializado correctamente");
    }
    
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El pool de conexiones no está inicializado");
        }
        return dataSource.getConnection();
    }
    
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool de conexiones cerrado correctamente");
        }
    }
    
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(1000);
        } catch (SQLException e) {
            logger.error("Error al probar la conexión a la base de datos", e);
            return false;
        }
    }
    
    public static String getUrl() {
        return properties.getProperty("db.url");
    }
    
    public static String getUsername() {
        return properties.getProperty("db.username", "root");
    }
}