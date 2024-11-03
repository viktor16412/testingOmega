package com.rintisa.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Excepción personalizada para manejar errores relacionados con la base de datos.
 * Esta clase extiende Exception y proporciona funcionalidad adicional específica
 * para el manejo de errores de base de datos.
 */
public class DatabaseException extends Exception {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseException.class);
    
    // Códigos de error específicos
    public static final String ERROR_CONNECTION = "DB001";
    public static final String ERROR_QUERY = "DB002";
    public static final String ERROR_INSERT = "DB003";
    public static final String ERROR_UPDATE = "DB004";
    public static final String ERROR_DELETE = "DB005";
    public static final String ERROR_DUPLICATE = "DB006";
    public static final String ERROR_NOT_FOUND = "DB007";
    public static final String ERROR_CONSTRAINT = "DB008";
    
    private String errorCode;
    private String sqlState;
    private int vendorCode;

   
    private static final Map<String, String> MYSQL_ERROR_CODES = new HashMap<>();
    static {
        MYSQL_ERROR_CODES.put("23000", ERROR_DUPLICATE); // Violación de clave única
        MYSQL_ERROR_CODES.put("42S02", ERROR_NOT_FOUND); // Tabla no encontrada
        MYSQL_ERROR_CODES.put("28000", ERROR_CONNECTION); // Error de autenticación
        MYSQL_ERROR_CODES.put("08S01", ERROR_CONNECTION); // Error de comunicación
    }
    
    /**
     * Método  para convertir SQLException en DatabaseException
     * con el código de error apropiado.
     */
    public static DatabaseException fromSQLException(SQLException ex) {
        String sqlState = ex.getSQLState();
        String errorCode = MYSQL_ERROR_CODES.getOrDefault(sqlState, ERROR_QUERY);
        
        // Logging detallado del error
        logger.error("SQL Error occurred: {} - State: {} - Vendor Code: {}", 
                    ex.getMessage(), sqlState, ex.getErrorCode());
        
        return new DatabaseException(
            ex.getMessage(),
            errorCode,
            sqlState,
            ex.getErrorCode()
        );
    }
    
    
    /**
     * Genera un mensaje detallado del error incluyendo toda la información disponible.
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Database Error:\n");
        sb.append("Message: ").append(getMessage()).append("\n");
        sb.append("Error Code: ").append(errorCode).append("\n");
        
        if (sqlState != null) {
            sb.append("SQL State: ").append(sqlState).append("\n");
        }
        
        if (vendorCode != 0) {
            sb.append("Vendor Code: ").append(vendorCode).append("\n");
        }
        
        if (getCause() != null) {
            sb.append("Cause: ").append(getCause().getMessage());
        }
        
        return sb.toString();
    }

    /**
     * Determina si el error es recuperable o no.
     */
    public boolean isRecoverable() {
        if (errorCode == null) return false;
        
        switch (errorCode) {
            case ERROR_CONNECTION:
            case ERROR_QUERY:
                return true;
            case ERROR_CONSTRAINT:
            case ERROR_DUPLICATE:
                return false;
            default:
                return false;
        }
    }

    /**
     * Proporciona una sugerencia de acción basada en el tipo de error.
     */
     public String getSuggestedAction() {
        if (errorCode == null) return "Contacte al administrador del sistema.";
        
        switch (errorCode) {
            case ERROR_CONNECTION:
                return "Verifique su conexión a la base de datos y credenciales.";
            case ERROR_INSERT:
                return "Verifique que los datos no estén duplicados o violen restricciones.";
            case ERROR_QUERY:
                return "Verifique la sintaxis de la consulta y los nombres de las tablas.";
            case ERROR_UPDATE:
                return "Verifique que el registro existe y los datos son válidos.";
            case ERROR_DELETE:
                return "Verifique que el registro existe y puede ser eliminado.";
            default:
                return "Contacte al administrador del sistema.";
        }
    }

    /**
     * Método para logging estructurado del error.
     */
    public void logError() {
        logger.error("Database Exception:");
        logger.error("Message: {}", getMessage());
        logger.error("Error Code: {}", errorCode);
        if (sqlState != null) {
            logger.error("SQL State: {}", sqlState);
        }
        if (vendorCode != 0) {
            logger.error("Vendor Code: {}", vendorCode);
        }
        if (getCause() != null) {
            logger.error("Cause: ", getCause());
        }
    }

    // Constructor adicional con información SQL específica
    public DatabaseException(String message, String errorCode, String sqlState, int vendorCode) {
        super(message);
        this.errorCode = errorCode;
        this.sqlState = sqlState;
        this.vendorCode = vendorCode;
    }

    // Clase interna para almacenar información de error
    private static class ErrorInfo {
        final String errorCode;
        final String description;
        
        ErrorInfo(String errorCode, String description) {
            this.errorCode = errorCode;
            this.description = description;
        }
    }
    
    // Constructores
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DatabaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    

    // Métodos de fábrica para crear excepciones específicas
    public static DatabaseException connectionError(String message) {
        return new DatabaseException(message, ERROR_CONNECTION);
    }
    
    public static DatabaseException queryError(String message) {
        return new DatabaseException(message, ERROR_QUERY);
    }
    
    public static DatabaseException insertError(String message) {
        return new DatabaseException(message, ERROR_INSERT);
    }
    
    public static DatabaseException updateError(String message) {
        return new DatabaseException(message, ERROR_UPDATE);
    }
    
    public static DatabaseException deleteError(String message) {
        return new DatabaseException(message, ERROR_DELETE);
    }
    
     public static DatabaseException duplicateError(String message) {
        return new DatabaseException(message, ERROR_DUPLICATE);
    }
    
    public static DatabaseException notFoundError(String message) {
        return new DatabaseException(message, ERROR_NOT_FOUND);
    }
}