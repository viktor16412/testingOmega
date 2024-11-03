package com.rintisa.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordUtil {
    
  private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    
    /**
     * Genera un hash BCrypt para una contraseña
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
    
    /**
     * Verifica si una contraseña coincide con su hash
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (IllegalArgumentException e) {
            logger.error("Error al verificar contraseña", e);
            return false;
        }
    }
    
    /**
     * Método para generar un hash para una contraseña específica
     * (útil para generar hashes para insertar en la base de datos)
     */
    public static void main(String[] args) {
        String password = "admin123";
        String hashedPassword = hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Hashed: " + hashedPassword);
        System.out.println("Verificación: " + verifyPassword(password, hashedPassword));
    }
}