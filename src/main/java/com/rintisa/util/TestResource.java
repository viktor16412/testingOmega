package com.rintisa.util;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class TestResource {
/*    public static void main(String[] args) {
        URL url = TestResource.class.getResource("/database.properties");
        if (url == null) {
            System.out.println("Resource not found");
        } else {
            System.out.println("Resource found: " + url.toExternalForm());
        }
    }
}*/
  
 /*   ///prueba ruta de iconos 
     public static void main(String[] args) {
        // Cambia 'icon.png' por el nombre de tu archivo de icono
        String iconPath = "/icons/user.png"; // Ruta relativa a 'src/main/resources'
        
        // Carga el icono
        URL iconUrl = TestResource.class.getResource(iconPath);
        
        if (iconUrl != null) {
            // Si el recurso fue encontrado, muestra un mensaje
            System.out.println("Icono encontrado: " + iconUrl.toExternalForm());
            // Crea un ícono y lo muestra en un JOptionPane como ejemplo
            ImageIcon icon = new ImageIcon(iconUrl);
            JOptionPane.showMessageDialog(null, "Icono cargado con éxito", "Prueba de Icono", JOptionPane.INFORMATION_MESSAGE, icon);
        } else {
            // Si el recurso no fue encontrado, muestra un mensaje de error
            System.out.println("Icono no encontrado en la ruta: " + iconPath);
        }
    }
}
*/
    public static void main(String[] args) {
        String svgPath = "/icons/user.svg"; // Ruta del archivo SVG

        // Cargar el SVG y convertirlo a Image
        Image image = loadSvgAsImage(svgPath);
        if (image != null) {
            // Crear un JFrame para mostrar el icono
            JFrame frame = new JFrame("Prueba SVG");
            JLabel label = new JLabel(new ImageIcon(image));
            frame.getContentPane().add(label);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        } else {
            System.err.println("Error al cargar el SVG.");
        }
    }

    private static Image loadSvgAsImage(String svgPath) {
        try {
            // Obtener el InputStream del SVG
            InputStream svgStream = TestResource.class.getResourceAsStream(svgPath);
            if (svgStream == null) {
                System.err.println("SVG no encontrado en la ruta: " + svgPath);
                return null;
            }

            // Transcoder para convertir SVG a PNG
            PNGTranscoder transcoder = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(svgStream);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(pngOutputStream);

            // Transcodificar
            transcoder.transcode(input, output);

            // Convertir el byte array a Image
            byte[] pngData = pngOutputStream.toByteArray();
            return Toolkit.getDefaultToolkit().createImage(pngData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}