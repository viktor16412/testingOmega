package com.rintisa.view;

import com.rintisa.controller.UsuarioController;
import com.rintisa.model.Usuario;
import com.rintisa.model.Rol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class UsuarioDialog extends JDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDialog.class);
    
    private final UsuarioController usuarioController;
    private final Usuario usuarioExistente;
    private boolean aceptado = false;
    
     // Componentes del formulario
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtEmail;
    private JComboBox<String> cmbRol;
    private JCheckBox chkActivo;
    private JButton btnAceptar;
    private JButton btnCancelar;
    private JPanel mainPanel;

      public UsuarioDialog(Frame parent, UsuarioController usuarioController, Usuario usuario) {
        super(parent, usuario == null ? "Nuevo Usuario" : "Editar Usuario", true);
        this.usuarioController = usuarioController;
        this.usuarioExistente = usuario;
        
        createComponents();
        setupLayout();
        setupListeners();
        loadInitialData();
        
        pack();
        setLocationRelativeTo(parent);
    }

    private void createComponents() {
        // Crear componentes
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtEmail = new JTextField(20);
        cmbRol = new JComboBox<>();
        chkActivo = new JCheckBox("Activo");
        btnAceptar = new JButton("Aceptar");
        btnCancelar = new JButton("Cancelar");
        mainPanel = new JPanel();

        // Configurar estado inicial
        chkActivo.setSelected(true);
        if (usuarioExistente != null) {
            txtUsername.setEnabled(false);
        }
    }
  
       private void setupLayout() {
        setLayout(new BorderLayout());
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Agregar componentes al panel
        int row = 0;
        
        addLabelAndComponent("Usuario:", txtUsername, mainPanel, gbc, row++);
        addLabelAndComponent("Contraseña:", txtPassword, mainPanel, gbc, row++);
        addLabelAndComponent("Nombre:", txtNombre, mainPanel, gbc, row++);
        addLabelAndComponent("Apellido:", txtApellido, mainPanel, gbc, row++);
        addLabelAndComponent("Email:", txtEmail, mainPanel, gbc, row++);
        addLabelAndComponent("Rol:", cmbRol, mainPanel, gbc, row++);
        addLabelAndComponent("Estado:", chkActivo, mainPanel, gbc, row++);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnAceptar);
        buttonPanel.add(btnCancelar);

        // Agregar paneles al diálogo
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Cargar roles en el combo
        cargarRoles();
    } 
       
     private void addLabelAndComponent(String labelText, JComponent component, 
                                    JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(component, gbc);
    } 
      
   private void setupListeners() {
        btnAceptar.addActionListener(e -> guardarUsuario());
        btnCancelar.addActionListener(e -> dispose());

        // Enter key en campos
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Component comp = (Component) e.getSource();
                    comp.transferFocus();
                }
            }
        };

        txtUsername.addKeyListener(enterAdapter);
        txtPassword.addKeyListener(enterAdapter);
        txtNombre.addKeyListener(enterAdapter);
        txtApellido.addKeyListener(enterAdapter);
        txtEmail.addKeyListener(enterAdapter);
    }

    private void loadInitialData() {
        if (usuarioExistente != null) {
            txtUsername.setText(usuarioExistente.getUsername());
            txtNombre.setText(usuarioExistente.getNombre());
            txtApellido.setText(usuarioExistente.getApellido());
            txtEmail.setText(usuarioExistente.getEmail());
            chkActivo.setSelected(usuarioExistente.isActivo());
            
            if (usuarioExistente.getRol() != null) {
                cmbRol.setSelectedItem(usuarioExistente.getRol().getNombre());
            }
        }
    }
  
     
     
     
   private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        // Username
        gbc.gridx = 0;
        gbc.gridy = y;
        panelPrincipal.add(new JLabel("Usuario: *"), gbc);
        
        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        panelPrincipal.add(txtUsername, gbc);
        y++;

        // Password
        gbc.gridx = 0;
        gbc.gridy = y;
        panelPrincipal.add(new JLabel("Contraseña: *"), gbc);
        
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        panelPrincipal.add(txtPassword, gbc);
        y++;

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = y;
        panelPrincipal.add(new JLabel("Nombre: *"), gbc);
        
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panelPrincipal.add(txtNombre, gbc);
        y++;

        // Apellido
        gbc.gridx = 0;
        gbc.gridy = y;
        panelPrincipal.add(new JLabel("Apellido: *"), gbc);
        
        gbc.gridx = 1;
        txtApellido = new JTextField(20);
        panelPrincipal.add(txtApellido, gbc);
        y++;

        // Email
        gbc.gridx = 0;
        gbc.gridy = y;
        panelPrincipal.add(new JLabel("Email: *"), gbc);
        
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        panelPrincipal.add(txtEmail, gbc);
        y++;

        // Rol
        gbc.gridx = 0;
        gbc.gridy = y;
        panelPrincipal.add(new JLabel("Rol: *"), gbc);
        
        gbc.gridx = 1;
        cmbRol = new JComboBox<>();
        cargarRoles();
        panelPrincipal.add(cmbRol, gbc);
        y++;

        // Activo
        gbc.gridx = 0;
        gbc.gridy = y;
        panelPrincipal.add(new JLabel("Estado:"), gbc);
        
        gbc.gridx = 1;
        chkActivo = new JCheckBox("Activo");
        chkActivo.setSelected(true);
        panelPrincipal.add(chkActivo, gbc);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAceptar = new JButton("Aceptar");
        btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);

        // Agregar paneles al diálogo
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    public boolean isAceptado() {
        return aceptado;
    }
    // Método para cargar los roles en el ComboBox
    private void cargarRoles() {
          try {
            cmbRol.removeAllItems();
            List<Rol> roles = usuarioController.getRolService().listarTodos();
            for (Rol rol : roles) {
                cmbRol.addItem(rol.getNombre());
            }
            if (cmbRol.getItemCount() > 0) {
                cmbRol.setSelectedIndex(0);
            }
        } catch (Exception e) {
            logger.error("Error al cargar roles", e);
            JOptionPane.showMessageDialog(this,
                "Error al cargar roles: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para cargar los datos de un usuario existente
    private void cargarDatosUsuario(Usuario usuario) {
        try {
            txtUsername.setText(usuario.getUsername());
            txtNombre.setText(usuario.getNombre());
            txtApellido.setText(usuario.getApellido());
            txtEmail.setText(usuario.getEmail());
            chkActivo.setSelected(usuario.isActivo());
            
            // Seleccionar el rol en el combo
            if (usuario.getRol() != null) {
                cmbRol.setSelectedItem(usuario.getRol().getNombre());
            }
            
            // Si es edición, la contraseña es opcional
            txtPassword.setText("");
            txtPassword.setToolTipText("Dejar en blanco para mantener la contraseña actual");
            
            logger.debug("Datos cargados para usuario: {}", usuario.getUsername());
            
        } catch (Exception e) {
            logger.error("Error al cargar datos del usuario", e);
            JOptionPane.showMessageDialog(this,
                "Error al cargar los datos del usuario: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para obtener el rol seleccionado
    private Rol obtenerRolSeleccionado() throws Exception {
        String nombreRol = (String) cmbRol.getSelectedItem();
        if (nombreRol == null || nombreRol.trim().isEmpty()) {
            throw new Exception("Debe seleccionar un rol");
        }
        
        // Buscar el rol por nombre
        return usuarioController.getRolService()
            .buscarPorNombre(nombreRol)
            .orElseThrow(() -> new Exception("El rol seleccionado no existe"));
    }

    // Método para obtener los datos del formulario
    private Usuario obtenerDatosFormulario() throws Exception {
        Usuario usuario = usuarioExistente != null ? usuarioExistente : new Usuario();
        
        usuario.setUsername(txtUsername.getText().trim());
        usuario.setNombre(txtNombre.getText().trim());
        usuario.setApellido(txtApellido.getText().trim());
        usuario.setEmail(txtEmail.getText().trim());
        usuario.setActivo(chkActivo.isSelected());
        
        // Establecer contraseña solo si es nuevo usuario o si se ha ingresado una nueva
        String password = new String(txtPassword.getPassword());
        if (usuarioExistente == null || !password.isEmpty()) {
            usuario.setPassword(password);
        }
        
        // Establecer rol
        usuario.setRol(obtenerRolSeleccionado());
        
        return usuario;
    }

    // Método para validar campos del formulario
    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();
        
        if (txtUsername.getText().trim().isEmpty()) {
            errores.append("- El nombre de usuario es requerido\n");
        }
        
        // Validar contraseña solo si es nuevo usuario
        if (usuarioExistente == null && txtPassword.getPassword().length == 0) {
            errores.append("- La contraseña es requerida para nuevos usuarios\n");
        }
        
        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es requerido\n");
        }
        
        if (txtApellido.getText().trim().isEmpty()) {
            errores.append("- El apellido es requerido\n");
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            errores.append("- El email es requerido\n");
        } else if (!validarFormatoEmail(txtEmail.getText().trim())) {
            errores.append("- El formato del email no es válido\n");
        }
        
        if (cmbRol.getSelectedItem() == null) {
            errores.append("- Debe seleccionar un rol\n");
        }

        if (errores.length() > 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor corrija los siguientes errores:\n" + errores.toString(),
                "Errores de Validación",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    // Método para validar formato de email
    private boolean validarFormatoEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(regex);
    }
    
    // Configurar eventos de los botones y campos
     private void configurarEventos() {
        // Configurar eventos de botones
        btnAceptar.addActionListener(e -> aceptar());
        btnCancelar.addActionListener(e -> dispose());

        // Tecla Enter en los campos
        configurarEventoEnter(txtUsername, txtPassword);
        configurarEventoEnter(txtPassword, txtNombre);
        configurarEventoEnter(txtNombre, txtApellido);
        configurarEventoEnter(txtApellido, txtEmail);
        configurarEventoEnter(txtEmail, cmbRol);

        // ESC para cerrar
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

     private void aceptar() {
        try {
            if (!validarCampos()) {
                return;
            }
            
            Usuario usuario = usuarioExistente != null ? usuarioExistente : new Usuario();
            
            // Establecer datos básicos
            usuario.setUsername(txtUsername.getText().trim());
            usuario.setNombre(txtNombre.getText().trim());
            usuario.setApellido(txtApellido.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setActivo(chkActivo.isSelected());
            
            // Establecer contraseña solo si es nuevo o si se ha ingresado una nueva
            String password = new String(txtPassword.getPassword());
            if (usuarioExistente == null || !password.isEmpty()) {
                usuario.setPassword(password);
            }
            
            // Establecer rol
            String rolNombre = (String) cmbRol.getSelectedItem();
            if (rolNombre != null) {
                usuarioController.getRolService()
                    .buscarPorNombre(rolNombre)
                    .ifPresent(usuario::setRol);
            }
            
            // Guardar usuario
            if (usuarioExistente == null) {
                usuarioController.getUsuarioService().crear(usuario);
            } else {
                usuarioController.getUsuarioService().actualizar(usuario);
            }
            
            aceptado = true;
            dispose();
            
        } catch (Exception e) {
            logger.error("Error al guardar usuario", e);
            JOptionPane.showMessageDialog(this,
                "Error al guardar usuario: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    } 
     
     
     
     
    private void configurarEventoEnter(JComponent origen, JComponent destino) {
        origen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    destino.requestFocus();
                }
            }
        });
    }
    private void inicializarNuevoUsuario() {
        chkActivo.setSelected(true);
        txtUsername.setEnabled(true);
        txtPassword.setEnabled(true);
    }
     
     
    private void configurarEventosTeclas() {
        // Enter en username va a password
        txtUsername.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
                }
            }
        });

        // Enter en password va a nombre
        txtPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txtNombre.requestFocus();
                }
            }
        });

        // Enter en nombre va a apellido
        txtNombre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txtApellido.requestFocus();
                }
            }
        });

        // Enter en apellido va a email
        txtApellido.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txtEmail.requestFocus();
                }
            }
        });

        // Enter en email va al combo de roles
        txtEmail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    cmbRol.requestFocus();
                }
            }
        });

        // ESC cierra el diálogo
        KeyStroke stroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(),
            stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void guardarUsuario() {
        try {
            if (!validarCampos()) {
                return;
            }

            Usuario usuario = usuarioExistente != null ? usuarioExistente : new Usuario();
            
            usuario.setUsername(txtUsername.getText().trim());
            usuario.setNombre(txtNombre.getText().trim());
            usuario.setApellido(txtApellido.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setActivo(chkActivo.isSelected());

            // Contraseña solo si es nuevo o si se ha ingresado una nueva
            String password = new String(txtPassword.getPassword());
            if (usuarioExistente == null || !password.isEmpty()) {
                usuario.setPassword(password);
            }

            // Establecer rol
            String rolNombre = (String) cmbRol.getSelectedItem();
            if (rolNombre != null) {
                usuarioController.getRolService()
                    .buscarPorNombre(rolNombre)
                    .ifPresent(usuario::setRol);
            }

            // Guardar
            if (usuarioExistente == null) {
                usuarioController.getUsuarioService().crear(usuario);
            } else {
                usuarioController.getUsuarioService().actualizar(usuario);
            }

            aceptado = true;
            dispose();

        } catch (Exception e) {
            logger.error("Error al guardar usuario", e);
            JOptionPane.showMessageDialog(this,
                "Error al guardar usuario: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE);
        });
    }

    private void mostrarMensajeExito(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                mensaje,
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }

   

    // Método para limpiar los campos
    private void limpiarCampos() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtEmail.setText("");
        if (cmbRol.getItemCount() > 0) {
            cmbRol.setSelectedIndex(0);
        }
        chkActivo.setSelected(true);
    }

    // Método para establecer el foco inicial
    public void establecerFocoInicial() {
        SwingUtilities.invokeLater(() -> {
            if (usuarioExistente == null) {
                txtUsername.requestFocus();
            } else {
                txtNombre.requestFocus();
            }
 });
    }
}



/*
import com.rintisa.controller.UsuarioController;
import com.rintisa.model.Usuario;
import com.rintisa.model.Rol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UsuarioDialog extends JDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDialog.class);
    
    private final UsuarioController usuarioController;
    private final Usuario usuarioExistente;
    private boolean aceptado = false;
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtEmail;
    private JComboBox<String> cmbRol;
    private JCheckBox chkActivo;
    private JButton btnAceptar;
    private JButton btnCancelar;

    public UsuarioDialog(JFrame parent, UsuarioController usuarioController, Usuario usuario) {
        super(parent, usuario == null ? "Nuevo Usuario" : "Editar Usuario", true);
        this.usuarioController = usuarioController;
        this.usuarioExistente = usuario;
        
        initComponents();
        // Establecer un tamaño mínimo
        setMinimumSize(new Dimension(400, 500));
        setPreferredSize(new Dimension(400, 500));
        
        // Centrar en la pantalla
        setLocationRelativeTo(parent);
        
        if (usuario != null) {
            cargarDatosUsuario(usuario);
        }
    }

    private void initComponents() {
          // Panel principal con margen
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.WEST;
        
        // Crear componentes con tamaño adecuado
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtEmail = new JTextField(20);
        cmbRol = new JComboBox<>();
        chkActivo = new JCheckBox("Activo");
        btnAceptar = new JButton("Aceptar");
        btnCancelar = new JButton("Cancelar");

        // Configurar el tamaño preferido para los campos de texto
        Dimension fieldSize = new Dimension(200, 25);
        txtUsername.setPreferredSize(fieldSize);
        txtPassword.setPreferredSize(fieldSize);
        txtNombre.setPreferredSize(fieldSize);
        txtApellido.setPreferredSize(fieldSize);
        txtEmail.setPreferredSize(fieldSize);
        cmbRol.setPreferredSize(fieldSize);

        // Agregar componentes
        int y = 0;

        gc.gridx = 0; gc.gridy = y;
        formPanel.add(new JLabel("Usuario:"), gc);
        gc.gridx = 1;
        formPanel.add(txtUsername, gc);
        y++;

        gc.gridx = 0; gc.gridy = y;
        formPanel.add(new JLabel("Contraseña:"), gc);
        gc.gridx = 1;
        formPanel.add(txtPassword, gc);
        y++;

        gc.gridx = 0; gc.gridy = y;
        formPanel.add(new JLabel("Nombre:"), gc);
        gc.gridx = 1;
        formPanel.add(txtNombre, gc);
        y++;

        gc.gridx = 0; gc.gridy = y;
        formPanel.add(new JLabel("Apellido:"), gc);
        gc.gridx = 1;
        formPanel.add(txtApellido, gc);
        y++;

        gc.gridx = 0; gc.gridy = y;
        formPanel.add(new JLabel("Email:"), gc);
        gc.gridx = 1;
        formPanel.add(txtEmail, gc);
        y++;

        gc.gridx = 0; gc.gridy = y;
        formPanel.add(new JLabel("Rol:"), gc);
        gc.gridx = 1;
        formPanel.add(cmbRol, gc);
        y++;

        gc.gridx = 0; gc.gridy = y;
        formPanel.add(new JLabel("Estado:"), gc);
        gc.gridx = 1;
        formPanel.add(chkActivo, gc);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.add(btnAceptar);
        buttonPanel.add(btnCancelar);

        // Agregar paneles al panel principal
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Configurar eventos
        btnAceptar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        // Cargar roles
        cargarRoles();

        // Agregar el panel principal al diálogo
        setContentPane(mainPanel);

        // Ajustar tamaño y posición
        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void cargarRoles() {
        try {
            cmbRol.removeAllItems();
            List<Rol> roles = usuarioController.getRolService().listarTodos();
            for (Rol rol : roles) {
                cmbRol.addItem(rol.getNombre());
            }
        } catch (Exception e) {
            logger.error("Error al cargar roles", e);
            JOptionPane.showMessageDialog(this, 
                "Error al cargar roles: " + e.getMessage());
        }
    }

    private void cargarDatosUsuario(Usuario usuario) {
        txtUsername.setText(usuario.getUsername());
        txtUsername.setEnabled(false);  // No permitir editar username
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtEmail.setText(usuario.getEmail());
        chkActivo.setSelected(usuario.isActivo());
        
        if (usuario.getRol() != null) {
            cmbRol.setSelectedItem(usuario.getRol().getNombre());
        }
    }

    private void guardar() {
        try {
            if (!validarCampos()) {
                return;
            }

            Usuario usuario = usuarioExistente != null ? usuarioExistente : new Usuario();
            
            usuario.setUsername(txtUsername.getText().trim());
            usuario.setNombre(txtNombre.getText().trim());
            usuario.setApellido(txtApellido.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setActivo(chkActivo.isSelected());

            // Contraseña solo si es nuevo o si se ingresó una nueva
            String password = new String(txtPassword.getPassword());
            if (usuarioExistente == null || !password.isEmpty()) {
                usuario.setPassword(password);
            }

            // Rol
            String rolNombre = (String) cmbRol.getSelectedItem();
            if (rolNombre != null) {
                usuarioController.getRolService()
                    .buscarPorNombre(rolNombre)
                    .ifPresent(usuario::setRol);
            }

            // Guardar
            if (usuarioExistente == null) {
                usuarioController.getUsuarioService().crear(usuario);
            } else {
                usuarioController.getUsuarioService().actualizar(usuario);
            }

            aceptado = true;
            dispose();

        } catch (Exception e) {
            logger.error("Error al guardar usuario", e);
            JOptionPane.showMessageDialog(this, 
                "Error al guardar: " + e.getMessage());
        }
    }

    private boolean validarCampos() {
        if (txtUsername.getText().trim().isEmpty()) {
            mostrarError("El usuario es requerido");
            return false;
        }
        if (usuarioExistente == null && txtPassword.getPassword().length == 0) {
            mostrarError("La contraseña es requerida para nuevos usuarios");
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es requerido");
            return false;
        }
        if (txtApellido.getText().trim().isEmpty()) {
            mostrarError("El apellido es requerido");
            return false;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            mostrarError("El email es requerido");
            return false;
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", 
            JOptionPane.ERROR_MESSAGE);
    }

    public boolean isAceptado() {
        return aceptado;
    }
}
*/




/*
import com.rintisa.controller.UsuarioController;
import com.rintisa.model.Usuario;
import javax.swing.*;
import java.awt.*;

public class UsuarioDialog extends JDialog {
    
    private JPanel mainPanel;
    private JButton btnAceptar;
    private JButton btnCancelar;
    private boolean aceptado = false;
    
    public UsuarioDialog(JFrame parent, UsuarioController controller, Usuario usuario) {
        super(parent, "Nuevo Usuario", true);
        createComponents();
        setupLayout();
        pack();
        setLocationRelativeTo(parent);
        setSize(400, 300);
    }
    
    private void createComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        
        // Panel de formulario
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        formPanel.add(new JLabel("Usuario:"));
        formPanel.add(new JTextField(20));
        formPanel.add(new JLabel("Contraseña:"));
        formPanel.add(new JPasswordField(20));
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(new JTextField(20));
        formPanel.add(new JLabel("Apellido:"));
        formPanel.add(new JTextField(20));
        formPanel.add(new JLabel("Email:"));
        formPanel.add(new JTextField(20));
        formPanel.add(new JLabel("Rol:"));
        formPanel.add(new JComboBox<>());
        formPanel.add(new JLabel("Activo:"));
        formPanel.add(new JCheckBox());
        
        // Panel de botones
        JPanel buttonPanel = new JPanel();
        btnAceptar = new JButton("Aceptar");
        btnCancelar = new JButton("Cancelar");
        buttonPanel.add(btnAceptar);
        buttonPanel.add(btnCancelar);
        
        // Configurar eventos básicos
        btnAceptar.addActionListener(e -> dispose());
        btnCancelar.addActionListener(e -> dispose());
        
        // Agregar paneles al panel principal
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Agregar márgenes
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void setupLayout() {
        // Configurar el contenido del diálogo
        setContentPane(mainPanel);
        setResizable(false);
    }
    
    public boolean isAceptado() {
        return aceptado;
    }
}
*/