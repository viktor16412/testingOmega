package com.rintisa.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconManager {
    private static final Logger logger = LoggerFactory.getLogger(IconManager.class);
    
    // Tamaños predefinidos
    public static final int SMALL = 16;
    public static final int MEDIUM = 24;
    public static final int LARGE = 32;
    public static final int EXTRA_LARGE = 64;

    public static ImageIcon getIcon(String name) {
        return getIcon(name, MEDIUM);
    }

    public static ImageIcon getIcon(String name, int size) {
        try {
            BufferedImage image = SvgConverter.convertSvgToPng(name, size, size);
            return new ImageIcon(image);
        } catch (Exception e) {
            logger.warn("No se pudo cargar el icono: {} ({}px)", name, size);
            return createDefaultIcon(size);
        }
    }

    private static ImageIcon createDefaultIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillOval(2, 2, size-4, size-4);
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawOval(2, 2, size-4, size-4);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    // Método para verificar si un icono existe
    public static boolean existsIcon(String name) {
        String resourcePath = "/icons/" + name + ".png";
        return IconManager.class.getResourceAsStream(resourcePath) != null;
    }
    
    
    public static void listAvailableResources() {
        try {
            String[] resources = {
                "/icons/logo.png",
                "/icons/user.png",
                "/icons/key.png",
                "/icons/login.png",
                "/icons/cancel.png"
            };
            
            logger.info("Verificando recursos disponibles:");
            for (String resource : resources) {
                boolean exists = IconManager.class.getResourceAsStream(resource) != null;
                logger.info("  {} : {}", resource, exists ? "ENCONTRADO" : "NO ENCONTRADO");
            }
        } catch (Exception e) {
            logger.error("Error al listar recursos", e);
        }
    }
       
    
}
