/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

public class ResourceChecker {
    private static final Logger logger = LoggerFactory.getLogger(ResourceChecker.class);

    public static void verifyResources() {
        // Verificar estructura de directorios
        File resourceDir = new File("src/main/resources");
        File imagesDir = new File(resourceDir, "icons");

        logger.info("Verificando estructura de directorios:");
        logger.info("  resources dir exists: {}", resourceDir.exists());
        logger.info("  images dir exists: {}", imagesDir.exists());

        // Verificar archivos de imágenes
        String[] requiredImages = {
            "logo.png",
            "user.png",
            "key.png",
            "login.png",
            "cancel.png"
        };

        logger.info("Verificando archivos de imágenes:");
        for (String image : requiredImages) {
            File imageFile = new File(imagesDir, image);
            logger.info("  {} exists: {}", image, imageFile.exists());
        }

        // Verificar classpath
        logger.info("Verificando recursos en classpath:");
        for (String image : requiredImages) {
            String path = "/icons/" + image;
            boolean exists = ResourceChecker.class.getResource(path) != null;
            logger.info("  {} in classpath: {}", path, exists);
        }
    }

    public static void main(String[] args) {
        verifyResources();
    }
}