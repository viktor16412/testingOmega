
package com.rintisa.view;

import com.rintisa.controller.PermisoPantallaController;
import com.rintisa.model.Pantalla;
import com.rintisa.model.PermisosPantalla;
import com.rintisa.model.Rol;
import com.rintisa.exception.DatabaseException;
import com.rintisa.security.PermisosPantallaManager;
import com.rintisa.service.interfaces.IPermisosPantallaService;
import com.rintisa.util.ModernUIUtils;
import com.rintisa.view.base.VistaBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.util.List;
import javax.swing.table.TableColumnModel;


public class PermisosPantallaView extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(PermisosPantallaView.class);
    
    private final PermisoPantallaController controller;
    private final IPermisosPantallaService permisosService;
    private final String rolActual;
    
    private JTable tablaPermisos;
    private DefaultTableModel modeloTabla;
    private JComboBox<RolComboItem> cmbRoles;
    private JButton btnGuardar;
    private JButton btnAgregarPantalla;
    
    public PermisosPantallaView(PermisoPantallaController controller,
                               IPermisosPantallaService permisosService,
                               String rolActual) {
        this.controller = controller;
        this.permisosService = permisosService;
        this.rolActual = rolActual;
        
        initComponents();
        setupLayout();
        setupEvents();
        loadData();
    }

    private void initComponents() {
        // Inicializar los componentes
        cmbRoles = new JComboBox<>();
        btnGuardar = ModernUIUtils.createPrimaryButton("Guardar Cambios", "save");
          btnAgregarPantalla = ModernUIUtils.createPrimaryButton("Agregar Pantalla", "new");
  
        
        // Crear modelo y tabla
        String[] columnas = {"Rol", "Pantalla", "Acceso", "Edición", "Eliminación"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column >= 2 ? Boolean.class : String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2;
            }
        };
        
        tablaPermisos = new JTable(modeloTabla);
        ModernUIUtils.setupModernTable(tablaPermisos);
        
        // Configurar anchos de columnas
        tablaPermisos.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaPermisos.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaPermisos.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaPermisos.getColumnModel().getColumn(3).setPreferredWidth(80);
        tablaPermisos.getColumnModel().getColumn(4).setPreferredWidth(80);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        
        // Panel de selección de rol
        JPanel rolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolPanel.add(new JLabel("Rol:"));
        rolPanel.add(cmbRoles);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnAgregarPantalla);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(btnGuardar);
        
        topPanel.add(rolPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Agregar componentes al panel principal
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(tablaPermisos), BorderLayout.CENTER);
    }
    
    private void setupEvents() {
        // Evento del combo de roles
        
        cmbRoles.addActionListener(e -> {
            if (cmbRoles.getSelectedItem() != null) {
                RolComboItem selectedRol = (RolComboItem) cmbRoles.getSelectedItem();
                try {
                    cargarPermisosPorRol(selectedRol.getRol().getNombre());
                } catch (DatabaseException ex) {
                    logger.error("Error al cargar permisos", ex);
                    mostrarError("Error al cargar permisos: " + ex.getMessage());
                }
            }
        });

        // Evento del botón guardar
        btnGuardar.addActionListener(e -> guardarCambios());
        btnAgregarPantalla.addActionListener(e -> mostrarDialogoAgregarPantalla());
        // Eventos de cambios en la tabla
        tablaPermisos.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                try {
                    actualizarPermiso(e.getFirstRow(), e.getColumn());
                } catch (Exception ex) {
                    logger.error("Error al actualizar permiso", ex);
                    mostrarError("Error al actualizar permiso: " + ex.getMessage());
                }
            }
        });
    }
    
    private void mostrarDialogoAgregarPantalla() {
        if (cmbRoles.getSelectedItem() == null) {
            mostrarError("Debe seleccionar un rol primero");
            return;
        }

        // Obtener la ventana padre correctamente
        Window window = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(window, "Agregar Pantalla", Dialog.ModalityType.APPLICATION_MODAL);
    

        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // ComboBox de pantallas disponibles
        JComboBox<PantallaComboItem> cmbPantallas = new JComboBox<>();
        for (Pantalla pantalla : Pantalla.values()) {
            // Verificar si la pantalla ya está asignada al rol
            if (!pantallaYaAsignada(pantalla)) {
                cmbPantallas.addItem(new PantallaComboItem(pantalla));
            }
        }

        // Checkboxes para permisos
        JCheckBox chkAcceso = new JCheckBox("Acceso", true);
        JCheckBox chkEdicion = new JCheckBox("Edición");
        JCheckBox chkEliminacion = new JCheckBox("Eliminación");

        // Agregar componentes al panel
        mainPanel.add(new JLabel("Pantalla:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(cmbPantallas, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        mainPanel.add(new JSeparator(), gbc);

        gbc.gridy++;
        mainPanel.add(chkAcceso, gbc);

        gbc.gridy++;
        mainPanel.add(chkEdicion, gbc);

        gbc.gridy++;
        mainPanel.add(chkEliminacion, gbc);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");

        btnAceptar.addActionListener(e -> {
            try {
                if (cmbPantallas.getSelectedItem() == null) {
                    mostrarError("Debe seleccionar una pantalla");
                    return;
                }

                // Obtener datos
                RolComboItem rolItem = (RolComboItem) cmbRoles.getSelectedItem();
                PantallaComboItem pantallaItem = (PantallaComboItem) cmbPantallas.getSelectedItem();
                
                // Guardar el nuevo permiso
                controller.actualizarPermiso(
                    rolItem.getRol().getNombre(),
                    pantallaItem.getPantalla().name(),
                    0, chkAcceso.isSelected()
                );
                controller.actualizarPermiso(
                    rolItem.getRol().getNombre(),
                    pantallaItem.getPantalla().name(),
                    1, chkEdicion.isSelected()
                );
                controller.actualizarPermiso(
                    rolItem.getRol().getNombre(),
                    pantallaItem.getPantalla().name(),
                    2, chkEliminacion.isSelected()
                );

                // Recargar datos
                cargarPermisosPorRol(rolItem.getRol().getNombre());
                
                dialog.dispose();
                JOptionPane.showMessageDialog(this, 
                    "Pantalla agregada exitosamente", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                logger.error("Error al agregar pantalla", ex);
                mostrarError("Error al agregar pantalla: " + ex.getMessage());
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnAceptar);
        buttonPanel.add(btnCancelar);

        // Agregar paneles al diálogo
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Configurar el diálogo
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private boolean pantallaYaAsignada(Pantalla pantalla) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String pantallaNombre = (String) modeloTabla.getValueAt(i, 1);
            if (pantalla.getNombre().equals(pantallaNombre)) {
                return true;
            }
        }
        return false;
    }

    // Clase auxiliar para el ComboBox de pantallas
    private static class PantallaComboItem {
        private final Pantalla pantalla;

        public PantallaComboItem(Pantalla pantalla) {
            this.pantalla = pantalla;
        }

        public Pantalla getPantalla() {
            return pantalla;
        }

        @Override
        public String toString() {
            return pantalla.getNombre();
        }
    }
    

    private void loadData() {
        try {
            // Cargar roles en el combo
            cargarRoles();
            
            // Seleccionar primer rol si existe
            if (cmbRoles.getItemCount() > 0) {
                cmbRoles.setSelectedIndex(0);
            }
        } catch (Exception e) {
            logger.error("Error al cargar datos", e);
            mostrarError("Error al cargar datos: " + e.getMessage());
        }
    }

    private void cargarRoles() throws DatabaseException {
        cmbRoles.removeAllItems();
        List<Rol> roles = controller.getRoles();
        for (Rol rol : roles) {
            cmbRoles.addItem(new RolComboItem(rol));
        }
    }

    private void cargarPermisosPorRol(String rolNombre) throws DatabaseException {
        List<PermisosPantalla> permisos = controller.obtenerPermisosPorRol(rolNombre);
        actualizarTablaPermisos(permisos);
    }

    private void actualizarTablaPermisos(List<PermisosPantalla> permisos) {
        modeloTabla.setRowCount(0);
        for (PermisosPantalla permiso : permisos) {
            modeloTabla.addRow(new Object[]{
                permiso.getRolNombre(),
                permiso.getPantalla().getNombre(),
                permiso.isAcceso(),
                permiso.isEdicion(),
                permiso.isEliminacion()
            });
        }
    }

    private void actualizarPermiso(int row, int column) throws DatabaseException {
        String rolNombre = (String) modeloTabla.getValueAt(row, 0);
        String pantallaNombre = (String) modeloTabla.getValueAt(row, 1);
        boolean valor = (Boolean) modeloTabla.getValueAt(row, column);
        
        controller.actualizarPermiso(rolNombre, pantallaNombre, column - 2, valor);
    }

    private void guardarCambios() {
        try {
            controller.guardarCambios();
            JOptionPane.showMessageDialog(this, 
                "Cambios guardados exitosamente", 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            logger.error("Error al guardar cambios", e);
            mostrarError("Error al guardar cambios: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE)
        );
    }

    // Clase interna para el ComboBox de roles
    private static class RolComboItem {
        private final Rol rol;

        public RolComboItem(Rol rol) {
            this.rol = rol;
        }

        public Rol getRol() {
            return rol;
        }

        @Override
        public String toString() {
            return rol.getNombre();
        }
    }
}