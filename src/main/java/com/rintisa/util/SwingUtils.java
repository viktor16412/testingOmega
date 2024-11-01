/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import javax.swing.text.JTextComponent;
import java.util.List;
import java.util.function.Consumer;

public class SwingUtils {
    
    /**
     * Centra una ventana en la pantalla
     */
    public static void centrarVentana(Window ventana) {
        ventana.setLocationRelativeTo(null);
    }
    
    /**
     * Configura un JTextField para aceptar solo números
     */
    public static void soloNumeros(JTextField textField) {
        textField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
    }
    
    /**
     * Configura un JTextField con límite de caracteres
     */
    public static void limitarCaracteres(JTextField textField, int limite) {
        textField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (textField.getText().length() >= limite) {
                    e.consume();
                }
            }
        });
    }
    
    /**
     * Crea un TableRowSorter para una tabla
     */
    public static <T extends TableModel> TableRowSorter<T> crearTableSorter(T model) {
        return new TableRowSorter<>(model);
    }
    
    /**
     * Configura un filtro para una tabla
     */
    public static void configurarFiltroTabla(JTextField txtFiltro, JTable tabla) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tabla.getModel());
        tabla.setRowSorter(sorter);
        
        txtFiltro.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String texto = txtFiltro.getText();
                if (texto.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
                }
            }
        });
    }
    
    /**
     * Configura un campo de texto para ejecutar una acción al presionar Enter
     */
    public static void onEnter(JTextComponent textComponent, Runnable action) {
        textComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    action.run();
                }
            }
        });
    }
    
    /**
     * Crea un panel con layout GridBagLayout y padding
     */
    public static JPanel createPanelWithPadding(int padding) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        return panel;
    }
    
    /**
     * Crea constraints para GridBagLayout
     */
    public static GridBagConstraints createGBC(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(2, 2, 2, 2);
        return gbc;
    }
    
    /**
     * Configura un botón con icono y tooltip
     */
    public static void configurarBoton(JButton boton, String tooltip, String iconPath) {
        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(SwingUtils.class.getResource(iconPath));
                boton.setIcon(icon);
            } catch (Exception e) {
                // Si no se puede cargar el icono, solo mostrará el texto
            }
        }
        boton.setToolTipText(tooltip);
    }
    
    /**
     * Ejecuta una tarea en segundo plano mostrando un diálogo de progreso
     */
    public static void ejecutarTareaConProgreso(Component padre, String mensaje, 
                                              Runnable tarea) {
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(padre), 
                                    "Procesando", true);
        JProgressBar progreso = new JProgressBar();
        progreso.setIndeterminate(true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel(mensaje), BorderLayout.NORTH);
        panel.add(progreso, BorderLayout.CENTER);
        
        dialogo.getContentPane().add(panel);
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                tarea.run();
                return null;
            }
            
            @Override
            protected void done() {
                dialogo.dispose();
            }
        };
        
        worker.execute();
        dialogo.setVisible(true);
    }
    
    /**
     * Configura el modelo de una tabla con datos
     */
    public static void configurarModelo(JTable tabla, String[] columnas, 
                                      List<Object[]> datos) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        for (Object[] fila : datos) {
            modelo.addRow(fila);
        }
        
        tabla.setModel(modelo);
    }
    
    /**
     * Configura propiedades comunes de una tabla
     */
    public static void configurarTabla(JTable tabla) {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.setRowHeight(25);
    }
    
    /**
     * Muestra un diálogo de confirmación
     */
    public static boolean confirmar(Component padre, String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(padre, mensaje, titulo,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
    
    /**
     * Muestra un mensaje de error
     */
    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Muestra un mensaje de información
     */
    public static void mostrarInfo(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Formatea una cadena para ser usada en una consulta LIKE
     */
    public static String formatearParaLike(String texto) {
        if (texto == null) return "%";
        return "%" + texto.trim().replace("'", "''") + "%";
    }
}
