/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.view;

import com.rintisa.controller.UsuarioController;
import com.rintisa.model.Usuario;
import com.rintisa.model.Rol;
import com.rintisa.util.SwingUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Optional;
import javax.swing.table.TableRowSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
public class UsuariosView extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuariosView.class);
    
    private final UsuarioController usuarioController;
    
    // Componentes de la interfaz
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnGuardar;
    private JButton btnCancelar;
    
    // Constructor
    public UsuariosView(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
        initComponents();
        configurarTabla();
        configurarEventos();
        cargarDatos();
        
        
    }
     
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior con búsqueda y botones
        JPanel panelSuperior = new JPanel(new BorderLayout(5, 0));
        
        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBuscar = new JTextField(20);
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscar);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        
        // Configurar botones
        SwingUtils.configurarBoton(btnNuevo, "Crear nuevo usuario", "/images/new.png");
        SwingUtils.configurarBoton(btnEditar, "Editar usuario seleccionado", "/images/edit.png");
        SwingUtils.configurarBoton(btnEliminar, "Eliminar usuario seleccionado", "/images/delete.png");
        SwingUtils.configurarBoton(btnGuardar, "Guardar cambios", "/images/save.png");
        SwingUtils.configurarBoton(btnCancelar, "Cancelar operación", "/images/cancel.png");
        
        panelBotones.add(btnNuevo);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelBotones, BorderLayout.EAST);
        
        // Tabla de usuarios
        String[] columnas = {
            "ID", "Usuario", "Nombre", "Apellido", "Email", "Rol", "Estado", "Último Acceso"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaUsuarios = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Estado inicial de los botones
        actualizarEstadoBotones(false);
    }
    
    private void configurarTabla() {
        // Configurar propiedades de la tabla
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.getTableHeader().setReorderingAllowed(false);
        
        // Configurar ancho de columnas
        tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(100); // Usuario
        tablaUsuarios.getColumnModel().getColumn(2).setPreferredWidth(150); // Nombre
        tablaUsuarios.getColumnModel().getColumn(3).setPreferredWidth(150); // Apellido
        tablaUsuarios.getColumnModel().getColumn(4).setPreferredWidth(200); // Email
        tablaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(100); // Rol
        tablaUsuarios.getColumnModel().getColumn(6).setPreferredWidth(80);  // Estado
        tablaUsuarios.getColumnModel().getColumn(7).setPreferredWidth(150); // Último Acceso
        
        // Ocultar la columna ID
        tablaUsuarios.getColumnModel().getColumn(0).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setWidth(0);
    }
    
    private void configurarEventos() {
         
         // Búsqueda en tiempo real/ 
         
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                buscarUsuarios();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                buscarUsuarios();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                buscarUsuarios();
            }
        });
         
         // Botón Nuevo
        btnNuevo.addActionListener(e -> {
            logger.debug("Clic en botón Nuevo");
            mostrarDialogoNuevoUsuario();
        });
           

        // Botón Editar
        btnEditar.addActionListener(e -> {
            logger.debug("Clic en botón Editar");
            editarUsuarioSeleccionado();
        });
        
        // Botón Eliminar
        btnEliminar.addActionListener(e -> {
            logger.debug("Clic en botón Eliminar");
            eliminarUsuarioSeleccionado();
        });

        // Doble clic en tabla
        tablaUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    logger.debug("Doble clic en tabla");
                    editarUsuarioSeleccionado();
                }
            }
        });

        // Habilitar/deshabilitar botones según selección
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            boolean haySeleccion = tablaUsuarios.getSelectedRow() != -1;
            btnEditar.setEnabled(haySeleccion);
            btnEliminar.setEnabled(haySeleccion);
        });
    }
    
    
     private void mostrarDialogoNuevoUsuario() {
           try {
            // Obtener la ventana principal
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                
                // Crear y mostrar el diálogo
                UsuarioDialog dialog = new UsuarioDialog(frame, usuarioController, null);
                dialog.setVisible(true);
                
                if (dialog.isAceptado()) {
                    // Aquí iría la lógica para actualizar la tabla
                    System.out.println("Diálogo aceptado");
                }
            }
        } catch (Exception ex) {
            logger.error("Error al mostrar diálogo de nuevo usuario", ex);
            JOptionPane.showMessageDialog(this,
                "Error al abrir el diálogo: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
         
           
    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        try {
            List<Usuario> usuarios = usuarioController.getUsuarioService().listarTodos();
            for (Usuario usuario : usuarios) {
                Object[] fila = {
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getEmail(),
                    usuario.getRol().getNombre(),
                    usuario.isActivo() ? "Activo" : "Inactivo",
                    usuario.getUltimoAcceso()
                };
                modeloTabla.addRow(fila);
            }
        } catch (Exception e) {
            logger.error("Error al cargar usuarios", e);
            JOptionPane.showMessageDialog(this,
                "Error al cargar la lista de usuarios: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarTabla() {
        String textoBusqueda = txtBuscar.getText().toLowerCase().trim();
        TableRowSorter<DefaultTableModel> sorter = 
            (TableRowSorter<DefaultTableModel>) tablaUsuarios.getRowSorter();
            
        if (textoBusqueda.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + textoBusqueda));
        }
    }
    
    private void actualizarEstadoBotones(boolean haySeleccion) {
        btnEditar.setEnabled(haySeleccion);
        btnEliminar.setEnabled(haySeleccion);
        btnGuardar.setEnabled(false);
        btnCancelar.setEnabled(false);
    }
    
   
    private void mostrarDialogoUsuario(Usuario usuario) {
         // Crear el diálogo
        UsuarioDialog dialog = new UsuarioDialog(
            // Obtener la ventana principal
            (JFrame) SwingUtilities.getWindowAncestor(this),
            usuarioController,
            usuario  // null para nuevo usuario, usuario existente para editar
        );
        
        // Mostrar el diálogo
        dialog.setVisible(true);
        
        // Si se aceptó el diálogo (guardó cambios)
        if (dialog.isAceptado()) {
            // Recargar la tabla
            cargarDatos();
        }
    }



    private void editarUsuarioSeleccionado() {
           int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un usuario para editar",
                "Aviso",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            int modelRow = tablaUsuarios.convertRowIndexToModel(filaSeleccionada);
            Long userId = (Long) modeloTabla.getValueAt(modelRow, 0);
            
            usuarioController.getUsuarioService().buscarPorId(userId).ifPresent(usuario -> {
                UsuarioDialog dialog = new UsuarioDialog(parentFrame, usuarioController, usuario);
                dialog.setVisible(true);
                
                if (dialog.isAceptado()) {
                    cargarDatos();
                }
            });
        } catch (Exception e) {
            logger.error("Error al editar usuario", e);
            JOptionPane.showMessageDialog(this,
                "Error al editar usuario: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarUsuarioSeleccionado() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            SwingUtils.mostrarError(this, "Por favor, seleccione un usuario para eliminar");
            return;
        }
        
        // Obtener datos del usuario seleccionado
        Long userId = (Long) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        String username = (String) tablaUsuarios.getValueAt(filaSeleccionada, 1);
        
        // Confirmar eliminación
        if (!SwingUtils.confirmar(this,
                "¿Está seguro que desea eliminar el usuario '" + username + "'?",
                "Confirmar Eliminación")) {
            return;
        }
        
        try {
            // Verificar que no sea el usuario actual
            if (usuarioController.getUsuarioActual().getId().equals(userId)) {
                SwingUtils.mostrarError(this, "No puede eliminar su propio usuario");
                return;
            }
            
            usuarioController.eliminarUsuario(userId);
            cargarDatos();
            SwingUtils.mostrarInfo(this, "Usuario eliminado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al eliminar usuario", e);
            SwingUtils.mostrarError(this, "Error al eliminar el usuario: " + e.getMessage());
        }
    }
    
    
    private void buscarUsuarios() {
        String criterio = txtBuscar.getText().trim().toLowerCase();
        modeloTabla.setRowCount(0);
        
        try {
            List<Usuario> usuarios = usuarioController.getUsuarioService().listarTodos();
            for (Usuario usuario : usuarios) {
                if (criterio.isEmpty() || coincideConCriterio(usuario, criterio)) {
                    Object[] fila = {
                        usuario.getId(),
                        usuario.getUsername(),
                        usuario.getNombre(),
                        usuario.getApellido(),
                        usuario.getEmail(),
                        usuario.getRol().getNombre(),
                        usuario.isActivo() ? "Activo" : "Inactivo"
                    };
                    modeloTabla.addRow(fila);
                }
            }
        } catch (Exception e) {
            logger.error("Error al buscar usuarios", e);
            JOptionPane.showMessageDialog(this,
                "Error al buscar usuarios: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    private boolean coincideConCriterio(Usuario usuario, String criterio) {
        return usuario.getUsername().toLowerCase().contains(criterio) ||
               usuario.getNombre().toLowerCase().contains(criterio) ||
               usuario.getApellido().toLowerCase().contains(criterio) ||
               usuario.getEmail().toLowerCase().contains(criterio) ||
               usuario.getRol().getNombre().toLowerCase().contains(criterio);
    }
    
     private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
     private void botonNuevoUsuario() {
        mostrarDialogoUsuario(null);  // null indica nuevo usuario
    }
    
    
    private void guardarCambios() {
        // Este método se usa cuando se implementa edición directa en la tabla
        // Por ahora usamos diálogos separados para edición
    }
    
    private void cancelarOperacion() {
        // Este método se usa cuando se implementa edición directa en la tabla
        // Por ahora usamos diálogos separados para edición
    }
    
    // Clase interna para el diálogo de usuario
    private class UsuarioDialog extends JDialog {
        private boolean aceptado = false;
        private JTextField txtUsername;
        private JPasswordField txtPassword;
        private JTextField txtNombre;
        private JTextField txtApellido;
        private JTextField txtEmail;
        private JComboBox<String> cmbRol;
        private JCheckBox chkActivo;
        
        public UsuarioDialog(JFrame parent, UsuarioController controller) {
            this(parent, controller, null);
        }
        
        public UsuarioDialog(JFrame parent, UsuarioController controller, Usuario usuario) {
            super(parent, usuario == null ? "Nuevo Usuario" : "Editar Usuario", true);
            // Implementar el diálogo...
            // Esta parte se implementará en detalle cuando hagamos la vista de diálogo
        }
        
        public boolean isAceptado() {
            return aceptado;
        }
    }
}

*/






   public class UsuariosView extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuariosView.class);
    private final UsuarioController usuarioController;
    private JButton btnNuevo;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnGuardar;
    private JButton btnCancelar;
        
    public UsuariosView(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
        setupComponents();
        configurarTabla();
        configurarEventos();
        cargarDatos();
    
    }
    
    private void setupComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior con búsqueda y botones
        JPanel panelSuperior = new JPanel(new BorderLayout(5, 0));
        
        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBuscar = new JTextField(20);
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscar);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        
        // Configurar botones
        SwingUtils.configurarBoton(btnNuevo, "Crear nuevo usuario", "/images/new.png");
        SwingUtils.configurarBoton(btnEditar, "Editar usuario seleccionado", "/images/edit.png");
        SwingUtils.configurarBoton(btnEliminar, "Eliminar usuario seleccionado", "/images/delete.png");
        SwingUtils.configurarBoton(btnGuardar, "Guardar cambios", "/images/save.png");
        SwingUtils.configurarBoton(btnCancelar, "Cancelar operación", "/images/cancel.png");
        
        panelBotones.add(btnNuevo);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelBotones, BorderLayout.EAST);
        
        // Tabla de usuarios
        String[] columnas = {
            "ID", "Usuario", "Nombre", "Apellido", "Email", "Rol", "Estado", "Último Acceso"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaUsuarios = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);  
          // Estado inicial de los botones
        actualizarEstadoBotones(false);
    }
    
    
    private void configurarEventos() {
         
         // Búsqueda en tiempo real/ 
         
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                buscarUsuarios();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                buscarUsuarios();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                buscarUsuarios();
            }
        });
         
         // Botón Nuevo
        btnNuevo.addActionListener(e -> {
            logger.debug("Clic en botón Nuevo");
            mostrarDialogoNuevoUsuario();
        });
        // Botón Editar
        btnEditar.addActionListener(e -> {
            logger.debug("Clic en botón Editar");
            editarUsuarioSeleccionado();
        });
         // Botón Eliminar
        btnEliminar.addActionListener(e -> {
            logger.debug("Clic en botón Eliminar");
            eliminarUsuarioSeleccionado();
        });
         // Doble clic en tabla
        tablaUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    logger.debug("Doble clic en tabla");
                    editarUsuarioSeleccionado();
                }
            }
        });

        // Habilitar/deshabilitar botones según selección
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            boolean haySeleccion = tablaUsuarios.getSelectedRow() != -1;
            btnEditar.setEnabled(haySeleccion);
            btnEliminar.setEnabled(haySeleccion);
        });
    }
    
    private void mostrarDialogoNuevoUsuario() {
        try {
            // Obtener la ventana principal
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                
                // Crear y mostrar el diálogo
                UsuarioDialog dialog = new UsuarioDialog(frame, usuarioController, null);
                dialog.setVisible(true);
                
                if (dialog.isAceptado()) {
                    cargarDatos();// Aquí iría la lógica para actualizar la tabla
                    System.out.println("Diálogo aceptado");
                }
            }
        } catch (Exception ex) {
            logger.error("Error al mostrar diálogo de nuevo usuario", ex);
            JOptionPane.showMessageDialog(this,
                "Error al abrir el diálogo: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void editarUsuarioSeleccionado() {
           int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un usuario para editar",
                "Aviso",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            int modelRow = tablaUsuarios.convertRowIndexToModel(filaSeleccionada);
            Long userId = (Long) modeloTabla.getValueAt(modelRow, 0);
            
            usuarioController.getUsuarioService().buscarPorId(userId).ifPresent(usuario -> {
                UsuarioDialog dialog = new UsuarioDialog(parentFrame, usuarioController, usuario);
                dialog.setVisible(true);
                
                if (dialog.isAceptado()) {
                    cargarDatos();
                }
            });
        } catch (Exception e) {
            logger.error("Error al editar usuario", e);
            JOptionPane.showMessageDialog(this,
                "Error al editar usuario: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void eliminarUsuarioSeleccionado() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            SwingUtils.mostrarError(this, "Por favor, seleccione un usuario para eliminar");
            return;
        }
        
        // Obtener datos del usuario seleccionado
        Long userId = (Long) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        String username = (String) tablaUsuarios.getValueAt(filaSeleccionada, 1);
        
        // Confirmar eliminación
        if (!SwingUtils.confirmar(this,
                "¿Está seguro que desea eliminar el usuario '" + username + "'?",
                "Confirmar Eliminación")) {
            return;
        }
        
        try {
            // Verificar que no sea el usuario actual
            if (usuarioController.getUsuarioActual().getId().equals(userId)) {
                SwingUtils.mostrarError(this, "No puede eliminar su propio usuario");
                return;
            }
            
            usuarioController.eliminarUsuario(userId);
            cargarDatos();
            SwingUtils.mostrarInfo(this, "Usuario eliminado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al eliminar usuario", e);
            SwingUtils.mostrarError(this, "Error al eliminar el usuario: " + e.getMessage());
        }
    }
    
    
    
    
    
    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        try {
            List<Usuario> usuarios = usuarioController.getUsuarioService().listarTodos();
            for (Usuario usuario : usuarios) {
                Object[] fila = {
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getEmail(),
                    usuario.getRol().getNombre(),
                    usuario.isActivo() ? "Activo" : "Inactivo",
                    usuario.getUltimoAcceso()
                };
                modeloTabla.addRow(fila);
            }
        } catch (Exception e) {
            logger.error("Error al cargar usuarios", e);
            JOptionPane.showMessageDialog(this,
                "Error al cargar la lista de usuarios: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configurarTabla() {
        // Configurar propiedades de la tabla
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.getTableHeader().setReorderingAllowed(false);
        
        // Configurar ancho de columnas
        tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(100); // Usuario
        tablaUsuarios.getColumnModel().getColumn(2).setPreferredWidth(150); // Nombre
        tablaUsuarios.getColumnModel().getColumn(3).setPreferredWidth(150); // Apellido
        tablaUsuarios.getColumnModel().getColumn(4).setPreferredWidth(200); // Email
        tablaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(100); // Rol
        tablaUsuarios.getColumnModel().getColumn(6).setPreferredWidth(80);  // Estado
        tablaUsuarios.getColumnModel().getColumn(7).setPreferredWidth(150); // Último Acceso
        
        // Ocultar la columna ID
        tablaUsuarios.getColumnModel().getColumn(0).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setWidth(0);
    }
    
     private void buscarUsuarios() {
        String criterio = txtBuscar.getText().trim().toLowerCase();
        modeloTabla.setRowCount(0);
        
        try {
            List<Usuario> usuarios = usuarioController.getUsuarioService().listarTodos();
            for (Usuario usuario : usuarios) {
                if (criterio.isEmpty() || coincideConCriterio(usuario, criterio)) {
                    Object[] fila = {
                        usuario.getId(),
                        usuario.getUsername(),
                        usuario.getNombre(),
                        usuario.getApellido(),
                        usuario.getEmail(),
                        usuario.getRol().getNombre(),
                        usuario.isActivo() ? "Activo" : "Inactivo"
                    };
                    modeloTabla.addRow(fila);
                }
            }
        } catch (Exception e) {
            logger.error("Error al buscar usuarios", e);
            JOptionPane.showMessageDialog(this,
                "Error al buscar usuarios: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    private boolean coincideConCriterio(Usuario usuario, String criterio) {
        return usuario.getUsername().toLowerCase().contains(criterio) ||
               usuario.getNombre().toLowerCase().contains(criterio) ||
               usuario.getApellido().toLowerCase().contains(criterio) ||
               usuario.getEmail().toLowerCase().contains(criterio) ||
               usuario.getRol().getNombre().toLowerCase().contains(criterio);
    }
    
    private void actualizarEstadoBotones(boolean haySeleccion) {
        btnEditar.setEnabled(haySeleccion);
        btnEliminar.setEnabled(haySeleccion);
        btnGuardar.setEnabled(false);
        btnCancelar.setEnabled(false);
    }
    
     private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
} 


