
package com.rintisa.view;

import com.rintisa.controller.RolController;
import com.rintisa.model.Rol;
import com.rintisa.util.SwingUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.table.TableRowSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RolesView extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(RolesView.class);
    
    private final RolController rolController;
    
    // Componentes de la interfaz
    private JTable tablaRoles;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    
    // Panel de detalles
    private JPanel panelDetalles;
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JCheckBox chkActivo;
    private JButton btnGuardar;
    private JButton btnCancelar;
    
    // Estado
    private boolean modoEdicion = false;
    private Long rolIdEnEdicion = null;
    
    // Constructor
    public RolesView(RolController rolController) {
        this.rolController = rolController;
        initComponents();
        configurarTabla();
        configurarEventos();
        cargarDatos();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel izquierdo (tabla y búsqueda)
        JPanel panelIzquierdo = new JPanel(new BorderLayout(5, 5));
        
        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBuscar = new JTextField(20);
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscar);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        
        // Configurar botones
        SwingUtils.configurarBoton(btnNuevo, "Crear nuevo rol", "/images/new.png");
        SwingUtils.configurarBoton(btnEditar, "Editar rol seleccionado", "/images/edit.png");
        SwingUtils.configurarBoton(btnEliminar, "Eliminar rol seleccionado", "/images/delete.png");
        
        panelBotones.add(btnNuevo);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        
        // Panel superior izquierdo
        JPanel panelSupIzq = new JPanel(new BorderLayout());
        panelSupIzq.add(panelBusqueda, BorderLayout.CENTER);
        panelSupIzq.add(panelBotones, BorderLayout.SOUTH);
        
        // Tabla de roles
        String[] columnas = {"ID", "Nombre", "Descripción", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaRoles = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaRoles);
        
        // Agregar componentes al panel izquierdo
        panelIzquierdo.add(panelSupIzq, BorderLayout.NORTH);
        panelIzquierdo.add(scrollPane, BorderLayout.CENTER);
        
        // Panel derecho (detalles)
        panelDetalles = crearPanelDetalles();
        
        // Dividir los paneles
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            panelIzquierdo,
            panelDetalles
        );
        splitPane.setResizeWeight(0.6);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Estado inicial de los componentes
        actualizarEstadoComponentes(false);
    }
    
    private JPanel crearPanelDetalles() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de campos
        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCampos.add(new JLabel("Nombre:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtNombre = new JTextField(20);
        panelCampos.add(txtNombre, gbc);
        
        // Descripción
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panelCampos.add(new JLabel("Descripción:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtDescripcion = new JTextArea(5, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        panelCampos.add(scrollDescripcion, gbc);
        
        // Estado
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCampos.add(new JLabel("Estado:"), gbc);
        
        gbc.gridx = 1;
        chkActivo = new JCheckBox("Activo");
        panelCampos.add(chkActivo, gbc);
        
        // Panel de botones
        JPanel panelBotonesDetalle = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        
        SwingUtils.configurarBoton(btnGuardar, "Guardar cambios", "/images/save.png");
        SwingUtils.configurarBoton(btnCancelar, "Cancelar cambios", "/images/cancel.png");
        
        panelBotonesDetalle.add(btnGuardar);
        panelBotonesDetalle.add(btnCancelar);
        
        // Agregar todo al panel principal
        panel.add(new JLabel("Detalles del Rol", SwingConstants.CENTER), 
                 BorderLayout.NORTH);
        panel.add(panelCampos, BorderLayout.CENTER);
        panel.add(panelBotonesDetalle, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void configurarTabla() {
        // Configurar propiedades de la tabla
        tablaRoles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRoles.getTableHeader().setReorderingAllowed(false);
        
        // Configurar ancho de columnas
        tablaRoles.getColumnModel().getColumn(0).setMaxWidth(0);  // ID (oculto)
        tablaRoles.getColumnModel().getColumn(0).setMinWidth(0);
        tablaRoles.getColumnModel().getColumn(0).setPreferredWidth(0);
        tablaRoles.getColumnModel().getColumn(1).setPreferredWidth(150); // Nombre
        tablaRoles.getColumnModel().getColumn(2).setPreferredWidth(300); // Descripción
        tablaRoles.getColumnModel().getColumn(3).setPreferredWidth(100); // Estado
        
        // Configurar ordenamiento
        tablaRoles.setAutoCreateRowSorter(true);
    }
    
    private void configurarEventos() {
        // Evento de búsqueda
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabla();
            }
        });
        
        // Eventos de botones principales
        btnNuevo.addActionListener(e -> nuevoRol());
        btnEditar.addActionListener(e -> editarRol());
        btnEliminar.addActionListener(e -> eliminarRol());
        
        // Eventos de botones de detalle
        btnGuardar.addActionListener(e -> guardarRol());
        btnCancelar.addActionListener(e -> cancelarEdicion());
        
        // Evento de selección en la tabla
        tablaRoles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                rolSeleccionado();
            }
        });
        
        // Evento de doble clic en la tabla
        tablaRoles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarRol();
                }
            }
        });
        
        // Validación de longitud máxima
        SwingUtils.limitarCaracteres(txtNombre, 50);
        
        // Convertir nombre a mayúsculas
        txtNombre.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                txtNombre.setText(txtNombre.getText().toUpperCase().trim());
            }
        });
    }
    
    private void cargarDatos() {
        try {
            // Limpiar tabla
            modeloTabla.setRowCount(0);
            
            // Obtener roles
            List<Rol> roles = rolController.listarRoles();
            
            // Agregar roles a la tabla
            for (Rol rol : roles) {
                Object[] fila = {
                    rol.getId(),
                    rol.getNombre(),
                    rol.getDescripcion(),
                    rol.isActivo() ? "Activo" : "Inactivo"
                };
                modeloTabla.addRow(fila);
            }
            
            logger.info("Datos de roles cargados exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al cargar datos de roles", e);
            SwingUtils.mostrarError(this, "Error al cargar los roles: " + e.getMessage());
        }
    }
    
    private void filtrarTabla() {
        String textoBusqueda = txtBuscar.getText().toLowerCase().trim();
        TableRowSorter<DefaultTableModel> sorter = 
            new TableRowSorter<>(modeloTabla);
        
        if (textoBusqueda.isEmpty()) {
            tablaRoles.setRowSorter(sorter);
            return;
        }
        
        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter(
            "(?i)" + textoBusqueda,
            1, 2  // Buscar en columnas Nombre y Descripción
        );
        sorter.setRowFilter(rf);
        tablaRoles.setRowSorter(sorter);
    }
    
    private void actualizarEstadoComponentes(boolean editando) {
        boolean haySeleccion = tablaRoles.getSelectedRow() != -1;
        
        // Botones principales
        btnNuevo.setEnabled(!editando);
        btnEditar.setEnabled(!editando && haySeleccion);
        btnEliminar.setEnabled(!editando && haySeleccion);
        
        // Campos de detalle
        txtNombre.setEnabled(editando);
        txtDescripcion.setEnabled(editando);
        chkActivo.setEnabled(editando);
        
        // Botones de detalle
        btnGuardar.setEnabled(editando);
        btnCancelar.setEnabled(editando);
        
        // Tabla
        tablaRoles.setEnabled(!editando);
        txtBuscar.setEnabled(!editando);
        
        modoEdicion = editando;
    }
    
    private void nuevoRol() {
        limpiarCampos();
        rolIdEnEdicion = null;
        actualizarEstadoComponentes(true);
        txtNombre.requestFocus();
        chkActivo.setSelected(true);  // Por defecto activo
    }
    
    private void editarRol() {
        int filaSeleccionada = tablaRoles.getSelectedRow();
        if (filaSeleccionada == -1) {
            SwingUtils.mostrarError(this, "Por favor, seleccione un rol para editar");
            return;
        }
        
        // Convertir índice de fila si la tabla está filtrada
        int modelRow = tablaRoles.convertRowIndexToModel(filaSeleccionada);
        
        // Obtener ID del rol seleccionado
        rolIdEnEdicion = (Long) modeloTabla.getValueAt(modelRow, 0);
        
        try {
            Rol rol = rolController.buscarRol(rolIdEnEdicion)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            
            // Si es el rol ADMIN, verificar si se puede editar
            if ("ADMIN".equals(rol.getNombre()) && !puedeEditarRolAdmin()) {
                SwingUtils.mostrarError(this, "No tiene permisos para editar el rol de Administrador");
                return;
            }
            
            cargarDatosEnFormulario(rol);
            actualizarEstadoComponentes(true);
            txtNombre.requestFocus();
            
        } catch (Exception e) {
            logger.error("Error al cargar rol para edición", e);
            SwingUtils.mostrarError(this, "Error al cargar el rol: " + e.getMessage());
        }
    }
    
    private void eliminarRol() {
        int filaSeleccionada = tablaRoles.getSelectedRow();
        if (filaSeleccionada == -1) {
            SwingUtils.mostrarError(this, "Por favor, seleccione un rol para eliminar");
            return;
        }
        
        // Convertir índice si la tabla está filtrada
        int modelRow = tablaRoles.convertRowIndexToModel(filaSeleccionada);
        
        // Obtener datos del rol
        Long rolId = (Long) modeloTabla.getValueAt(modelRow, 0);
        String nombreRol = (String) modeloTabla.getValueAt(modelRow, 1);
        
        // Verificar si es rol ADMIN
        if ("ADMIN".equals(nombreRol)) {
            SwingUtils.mostrarError(this, "No se puede eliminar el rol de Administrador");
            return;
        }
        
        // Confirmar eliminación
        if (!SwingUtils.confirmar(this,
                "¿Está seguro que desea eliminar el rol '" + nombreRol + "'?",
                "Confirmar Eliminación")) {
            return;
        }
        
        try {
            rolController.eliminarRol(rolId);
            cargarDatos();
            limpiarCampos();
            SwingUtils.mostrarInfo(this, "Rol eliminado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al eliminar rol", e);
            SwingUtils.mostrarError(this, 
                "No se puede eliminar el rol porque está en uso o ocurrió un error: " 
                + e.getMessage());
        }
    }
    
    private void guardarRol() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }
        
        try {
            Rol rol = new Rol();
            if (rolIdEnEdicion != null) {
                rol.setId(rolIdEnEdicion);
            }
            
            rol.setNombre(txtNombre.getText().trim().toUpperCase());
            rol.setDescripcion(txtDescripcion.getText().trim());
            rol.setActivo(chkActivo.isSelected());
            
            // Guardar rol
            if (rolIdEnEdicion == null) {
                rolController.crear(rol);
                SwingUtils.mostrarInfo(this, "Rol creado exitosamente");
            } else {
                rolController.actualizar(rol);
                SwingUtils.mostrarInfo(this, "Rol actualizado exitosamente");
            }
            
            // Recargar datos y limpiar formulario
            cargarDatos();
            limpiarCampos();
            actualizarEstadoComponentes(false);
            
        } catch (Exception e) {
            logger.error("Error al guardar rol", e);
            SwingUtils.mostrarError(this, "Error al guardar el rol: " + e.getMessage());
        }
    }
    
    private void cancelarEdicion() {
        limpiarCampos();
        actualizarEstadoComponentes(false);
        rolIdEnEdicion = null;
    }
    
    private void rolSeleccionado() {
        if (modoEdicion) {
            return;
        }
        
        int filaSeleccionada = tablaRoles.getSelectedRow();
        if (filaSeleccionada == -1) {
            limpiarCampos();
            actualizarEstadoComponentes(false);
            return;
        }
        
        // Convertir índice si la tabla está filtrada
        int modelRow = tablaRoles.convertRowIndexToModel(filaSeleccionada);
        
        // Cargar datos del rol seleccionado en el formulario
        Long rolId = (Long) modeloTabla.getValueAt(modelRow, 0);
        try {
            rolController.buscarRol(rolId).ifPresent(this::cargarDatosEnFormulario);
        } catch (Exception e) {
            logger.error("Error al cargar detalles del rol", e);
        }
    }
    
    // Métodos de utilidad
    private void limpiarCampos() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        chkActivo.setSelected(true);
        rolIdEnEdicion = null;
    }
    
    private void cargarDatosEnFormulario(Rol rol) {
        txtNombre.setText(rol.getNombre());
        txtDescripcion.setText(rol.getDescripcion());
        chkActivo.setSelected(rol.isActivo());
    }
    
    private boolean validarCampos() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        
        if (nombre.isEmpty()) {
            SwingUtils.mostrarError(this, "El nombre del rol es obligatorio");
            txtNombre.requestFocus();
            return false;
        }
        
        if (nombre.length() < 3) {
            SwingUtils.mostrarError(this, "El nombre del rol debe tener al menos 3 caracteres");
            txtNombre.requestFocus();
            return false;
        }
        
        if (nombre.length() > 50) {
            SwingUtils.mostrarError(this, "El nombre del rol no puede exceder los 50 caracteres");
            txtNombre.requestFocus();
            return false;
        }
        
        if (descripcion.isEmpty()) {
            SwingUtils.mostrarError(this, "La descripción del rol es obligatoria");
            txtDescripcion.requestFocus();
            return false;
        }
        
        if (descripcion.length() > 255) {
            SwingUtils.mostrarError(this, 
                "La descripción del rol no puede exceder los 255 caracteres");
            txtDescripcion.requestFocus();
            return false;
        }
        
        // Validar nombre único
        try {
            if (rolController.existeNombre(nombre, rolIdEnEdicion)) {
                SwingUtils.mostrarError(this, "Ya existe un rol con ese nombre");
                txtNombre.requestFocus();
                return false;
            }
        } catch (Exception e) {
            logger.error("Error al validar nombre único", e);
            SwingUtils.mostrarError(this, "Error al validar el nombre del rol");
            return false;
        }
        
        return true;
    }
    
    private boolean puedeEditarRolAdmin() {
        // Aquí podrías implementar una lógica más compleja de permisos
        return true; // Por ahora permitimos la edición
    }
}
