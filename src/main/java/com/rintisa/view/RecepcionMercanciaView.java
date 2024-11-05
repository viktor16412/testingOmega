
package com.rintisa.view;

import com.rintisa.model.RecepcionMercancia;
import com.rintisa.controller.RecepcionMercanciaController;
import com.rintisa.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class RecepcionMercanciaView extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RecepcionMercanciaView.class);
    private final RecepcionMercanciaController controller;
    
    // Componentes UI
    private JTable tablaRecepciones;
    private DefaultTableModel modeloTabla;
    private JTextField txtNumeroRecepcion;
    private JTextField txtProveedor;
    private JTextField txtOrdenCompra;
    private JTextArea txtObservaciones;
    private JComboBox<RecepcionMercancia.EstadoRecepcion> cmbEstado;
    private JButton btnGuardar;
    private JButton btnVerificar;
    private JButton btnAceptar;
    private JButton btnRechazar;
    
    public RecepcionMercanciaView(RecepcionMercanciaController controller) {
        this.controller = controller;
         // Verificar permisos antes de inicializar
        if (!controller.tienePermisoAlmacen()) {
            logger.warn("Intento de acceso no autorizado a RecepcionMercanciaView");
            mostrarErrorPermiso();
            return;
        }
        initComponents();
        configureLayout();
        configureEvents();
        loadData();
    }
    
    private void mostrarErrorPermiso() {
        setLayout(new BorderLayout());
        JPanel errorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Icono de error
        JLabel iconLabel = new JLabel(new ImageIcon(getClass().getResource("/icons/error.png")));
        errorPanel.add(iconLabel, gbc);
        
        // Mensaje de error
        JLabel mensajeLabel = new JLabel("No tiene permisos para acceder a esta función");
        mensajeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mensajeLabel.setForeground(Color.RED);
        errorPanel.add(mensajeLabel, gbc);
        
        add(errorPanel, BorderLayout.CENTER);
    }
    
     // Sobrescribir el método setEnabled para controlar el acceso
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled && controller.tienePermisoAlmacen());
        // Deshabilitar todos los componentes si no tiene permiso
        if (!controller.tienePermisoAlmacen()) {
            deshabilitarComponentes(this);
        }
    }
    
    private void deshabilitarComponentes(Container container) {
        for (Component component : container.getComponents()) {
            component.setEnabled(false);
            if (component instanceof Container) {
                deshabilitarComponentes((Container) component);
            }
        }
    }
    
    
    
    private void initComponents() {
        // Inicializar componentes...
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de datos
        JPanel panelDatos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Agregar campos...
        txtNumeroRecepcion = new JTextField(20);
        txtProveedor = new JTextField(20);
        txtOrdenCompra = new JTextField(20);
        txtObservaciones = new JTextArea(4, 20);
        cmbEstado = new JComboBox<>(RecepcionMercancia.EstadoRecepcion.values());
        
        // Botones
        btnGuardar = new JButton("Guardar");
        btnVerificar = new JButton("Verificar");
        btnAceptar = new JButton("Aceptar");
        btnRechazar = new JButton("Rechazar");
        
        // Configurar tabla
        String[] columnas = {"ID", "Número", "Fecha", "Proveedor", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaRecepciones = new JTable(modeloTabla);
    }
    
    private void configureLayout() {
        // Configurar layout... 
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Panel izquierdo con la tabla
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(tablaRecepciones), BorderLayout.CENTER);
        
        // Panel derecho con el formulario
        JPanel rightPanel = new JPanel(new BorderLayout());
        // Agregar campos del formulario...
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnVerificar);
        buttonPanel.add(btnAceptar);
        buttonPanel.add(btnRechazar);
        
        // Agregar todo al panel principal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                            leftPanel, rightPanel);
        splitPane.setResizeWeight(0.6);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void configureEvents() {
        btnGuardar.addActionListener(e -> guardarRecepcion());
        btnVerificar.addActionListener(e -> verificarRecepcion());
        btnAceptar.addActionListener(e -> aceptarRecepcion());
        btnRechazar.addActionListener(e -> rechazarRecepcion());
    }
    
    private void loadData() {
         if (!controller.tienePermisoAlmacen()) {
            logger.warn("Intento de carga de datos sin permisos");
            return;
        }
        
        try {
            // Lógica de carga de datos...
        } catch (Exception e) {
            logger.error("Error al cargar datos", e);
            JOptionPane.showMessageDialog(this,
                "Error al cargar datos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Verificar permisos antes de cada operación crítica
    private void verificarPermisosAntes(Runnable operacion) {
        if (!controller.tienePermisoAlmacen()) {
            logger.warn("Intento de operación no autorizada en RecepcionMercanciaView");
            JOptionPane.showMessageDialog(this,
                "No tiene permisos para realizar esta operación",
                "Acceso Denegado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        operacion.run();
    }
    
    private void guardarRecepcion() {
         verificarPermisosAntes(() -> {
            try {
                // Lógica de guardado...
            } catch (Exception e) {
                logger.error("Error al guardar recepción", e);
                JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void verificarRecepcion() {
        verificarPermisosAntes(() -> {
            try {
                // Lógica de verificación...
            } catch (Exception e) {
                logger.error("Error al verificar recepción", e);
                JOptionPane.showMessageDialog(this,
                    "Error al verificar: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void aceptarRecepcion() {
        // Implementar aceptación
    }
    
    private void rechazarRecepcion() {
        // Implementar rechazo
    }
    
}