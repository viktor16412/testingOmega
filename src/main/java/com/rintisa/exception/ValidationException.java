/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ValidationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final Map<String, List<String>> errors;
    
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }
    
    public ValidationException(String field, String message) {
        super(message);
        this.errors = new HashMap<>();
        addError(field, message);
    }
    
    public void addError(String field, String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
    }
    
    public Map<String, List<String>> getErrors() {
        return Collections.unmodifiableMap(errors);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    @Override
    public String getMessage() {
        if (errors.isEmpty()) {
            return super.getMessage();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(super.getMessage()).append("\n");
        errors.forEach((field, messages) -> {
            sb.append(field).append(":\n");
            messages.forEach(message -> sb.append("  - ").append(message).append("\n"));
        });
        
        return sb.toString();
    }
    
    public static class Builder {
        private final ValidationException exception;
        
        public Builder(String message) {
            this.exception = new ValidationException(message);
        }
        
        public Builder addError(String field, String message) {
            exception.addError(field, message);
            return this;
        }
        
        public ValidationException build() {
            return exception;
        }
        
        public void throwIfHasErrors() throws ValidationException {
            if (exception.hasErrors()) {
                throw exception;
            }
        }
    }
}