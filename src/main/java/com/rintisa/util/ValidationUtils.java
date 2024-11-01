/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.util;

import com.rintisa.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_-]{4,20}$");
    
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    
    public static void validarUsuario(String username, String password, String email) 
            throws ValidationException {
        ValidationException.Builder validationBuilder = 
            new ValidationException.Builder("Error de validación de usuario");
        
        // Validar username
        if (StringUtils.isBlank(username)) {
            validationBuilder.addError("username", "El nombre de usuario no puede estar vacío");
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            validationBuilder.addError("username", 
                "El nombre de usuario debe tener entre 4 y 20 caracteres y solo puede contener letras, números, guiones y guiones bajos");
        }
        
        // Validar password
        if (StringUtils.isBlank(password)) {
            validationBuilder.addError("password", "La contraseña no puede estar vacía");
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            validationBuilder.addError("password", 
                "La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas, números y caracteres especiales");
        }
        
        // Validar email
        if (StringUtils.isBlank(email)) {
            validationBuilder.addError("email", "El email no puede estar vacío");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            validationBuilder.addError("email", "El formato del email no es válido");
        }
        
        validationBuilder.throwIfHasErrors();
    }
    
    public static void validarRol(String nombre, String descripcion) 
            throws ValidationException {
        ValidationException.Builder validationBuilder = 
            new ValidationException.Builder("Error de validación de rol");
        
        // Validar nombre
        if (StringUtils.isBlank(nombre)) {
            validationBuilder.addError("nombre", "El nombre del rol no puede estar vacío");
        } else if (nombre.length() < 3 || nombre.length() > 50) {
            validationBuilder.addError("nombre", 
                "El nombre del rol debe tener entre 3 y 50 caracteres");
        }
        
        // Validar descripción
        if (StringUtils.isBlank(descripcion)) {
            validationBuilder.addError("descripcion", 
                "La descripción del rol no puede estar vacía");
        } else if (descripcion.length() > 255) {
            validationBuilder.addError("descripcion", 
                "La descripción del rol no puede exceder los 255 caracteres");
        }
        
        validationBuilder.throwIfHasErrors();
    }
    
    public static void validarPassword(String password) throws ValidationException {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("password", 
                "La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas, números y caracteres especiales");
        }
    }
    
    public static void validarEmail(String email) throws ValidationException {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("email", "El formato del email no es válido");
        }
    }
}
