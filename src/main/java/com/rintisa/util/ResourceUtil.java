package com.rintisa.util;

import javax.swing.*;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUtil {
    private static final Logger logger = LoggerFactory.getLogger(ResourceUtil.class);

    /**
     * Obtiene un ImageIcon desde los recursos
     */
    public static ImageIcon getImageIcon(String path) {
        try {
            URL imageUrl = ResourceUtil.class.getResource("/images/" + path);
            if (imageUrl != null) {
                return new ImageIcon(imageUrl);
            } else {
                logger.warn("No se encontró el recurso de imagen: {}", path);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error al cargar imagen: {}", path, e);
            return null;
        }
    }

    /**
     * Obtiene una URL para un recurso
     */
    public static URL getResource(String path) {
        URL url = ResourceUtil.class.getResource(path);
        if (url == null) {
            logger.warn("No se encontró el recurso: {}", path);
        }
        return url;
    }

    /**
     * Verifica si un recurso existe
     */
    public static boolean resourceExists(String path) {
        return ResourceUtil.class.getResource(path) != null;
    }
}
