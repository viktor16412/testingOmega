/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.view;

import com.rintisa.util.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ReportDialog extends JDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportDialog.class);

    public static void mostrarDialogoReporteGenerado(JFrame parent, String rutaArchivo) {
        try {
            // Crear el mensaje
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Icono de éxito (puedes usar un ícono personalizado si lo deseas)
            JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
            panel.add(iconLabel, BorderLayout.WEST);

            // Panel de mensaje y ruta
            JPanel messagePanel = new JPanel(new GridLayout(3, 1, 5, 5));
            messagePanel.add(new JLabel("Reporte generado exitosamente"));
            messagePanel.add(new JLabel("Ubicación:"));
            
            // Campo de texto con la ruta (seleccionable)
            JTextField txtRuta = new JTextField(rutaArchivo);
            txtRuta.setEditable(false);
            messagePanel.add(txtRuta);

            panel.add(messagePanel, BorderLayout.CENTER);

            // Panel de botones
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            
            // Botón para abrir archivo
            JButton btnAbrir = new JButton("Abrir Archivo");
            btnAbrir.addActionListener(e -> {
                try {
                    Desktop.getDesktop().open(new File(rutaArchivo));
                } catch (Exception ex) {
                    logger.error("Error al abrir archivo", ex);
                    JOptionPane.showMessageDialog(parent,
                        "Error al abrir el archivo: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            });

            // Botón para abrir carpeta
            JButton btnCarpeta = new JButton("Abrir Carpeta");
            btnCarpeta.addActionListener(e -> ReportGenerator.abrirDirectorioReportes());

            // Botón cerrar
            JButton btnCerrar = new JButton("Cerrar");
            
            buttonPanel.add(btnAbrir);
            buttonPanel.add(btnCarpeta);
            buttonPanel.add(btnCerrar);

            panel.add(buttonPanel, BorderLayout.SOUTH);

            // Crear y mostrar el diálogo
            JDialog dialog = new JDialog(parent, "Reporte Generado", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setContentPane(panel);
            
            btnCerrar.addActionListener(e -> dialog.dispose());
            
            // Configurar diálogo
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setResizable(false);
            dialog.setVisible(true);

        } catch (Exception e) {
            logger.error("Error al mostrar diálogo de reporte", e);
            JOptionPane.showMessageDialog(parent,
                "Error al mostrar información del reporte: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}