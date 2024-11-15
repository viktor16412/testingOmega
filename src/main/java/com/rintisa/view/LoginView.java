
package com.rintisa.view;

///////////VERISON CON ICONOS FUNCIONA//////////////////////////
import com.rintisa.config.DatabaseConfig;
import com.rintisa.controller.PermisoPantallaController;
import com.rintisa.controller.UsuarioController;
import com.rintisa.dao.impl.PermisosPantallaDao;
import com.rintisa.dao.impl.ProductoDao;
import com.rintisa.dao.impl.RecepcionMercanciaDao;
import com.rintisa.dao.impl.RolDao;
import com.rintisa.dao.impl.UsuarioDao;
import com.rintisa.exception.DatabaseException;
import com.rintisa.model.Usuario;
import com.rintisa.service.impl.PermisosPantallaService;
import com.rintisa.service.impl.ProductoService;
import com.rintisa.service.impl.RecepcionMercanciaService;
import com.rintisa.service.impl.RolService;
import com.rintisa.service.impl.UsuarioService;
import com.rintisa.service.interfaces.IPermisosPantallaService;
import com.rintisa.util.IconManager;
import com.rintisa.util.LogUtils;
import com.rintisa.util.PermissionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginView extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginView.class);
    private static final long serialVersionUID = 1L;
     private IPermisosPantallaService permisosPantallaService;
    private final UsuarioController userController;
    private PermisoPantallaController permisoPantallaController;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnCancelar;
    private JLabel lblMensaje;

    public LoginView(UsuarioController controlador) {
        //this.userController  = controlador;
         try {
            logger.debug("Iniciando LoginView");
            
            if (controlador == null) {
                throw new IllegalArgumentException("El controlador no puede ser null");
            }
            this.userController = controlador;
            
             // Inicializar DAOs
            PermisosPantallaDao permisosPantallaDao = new PermisosPantallaDao();
            RolDao rolDao = new RolDao();

            // Inicializar Servicios
            RolService rolService = new RolService(rolDao);
            this.permisosPantallaService = new PermisosPantallaService(permisosPantallaDao);
            
            // Inicializar Controladores
            this.permisoPantallaController = new PermisoPantallaController(
                permisosPantallaService,
                rolService,
                null  // Usuario actual será establecido después del login
            );
            
            logger.debug("Inicializando componentes");
            initComponents();
            logger.debug("Configurando ventana");
            configurarVentana();
            logger.debug("Configurando eventos");
            configurarEventos();
            
            logger.info("LoginView inicializado correctamente");
            
        } catch (Exception e) {
            LogUtils.logError("Error al inicializar LoginView", e);
            throw new RuntimeException("Error al inicializar LoginView", e);
        }
        initComponents();
        configurarEventos();
        configurarVentana();
    }

    private void initComponents() {
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel del formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        try {
            ImageIcon logoIcon = IconManager.getIcon("logo", IconManager.LARGE);
            JLabel lblLogo = new JLabel(logoIcon);
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            formPanel.add(lblLogo, gbc);
        } catch (Exception e) {
            logger.warn("No se pudo cargar el logo", e);
        }

        // Título
        JLabel lblTitulo = new JLabel("Sistema RINTISA", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridy = 1;
        formPanel.add(lblTitulo, gbc);

        // Usuario
        JLabel lblUsuario = new JLabel("Usuario:", IconManager.getIcon("user"), SwingConstants.LEFT);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(lblUsuario, gbc);

        txtUsuario = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(txtUsuario, gbc);

        // Contraseña
        JLabel lblPassword = new JLabel("Contraseña:", IconManager.getIcon("key"), SwingConstants.LEFT);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        btnIngresar = new JButton("Ingresar", IconManager.getIcon("login"));
        btnIngresar.setIconTextGap(10);
        
        btnCancelar = new JButton("Cancelar", IconManager.getIcon("cancel"));
        btnCancelar.setIconTextGap(10);
        
        buttonPanel.add(btnIngresar);
        buttonPanel.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Label para mensajes
        lblMensaje = new JLabel(" ", SwingConstants.CENTER);
        lblMensaje.setForeground(Color.RED);
        gbc.gridy = 5;
        formPanel.add(lblMensaje, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void configurarVentana() {
        setTitle("Sistema RINTISA - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(IconManager.getIcon("logo").getImage());
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void configurarEventos() {
        // Evento del botón Ingresar
        btnIngresar.addActionListener(e -> intentarLogin());
        
        // Evento del botón Cancelar
        btnCancelar.addActionListener(e -> System.exit(0));
        
        // Manejar tecla Enter en el campo de usuario
        txtUsuario.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String input = txtUsuario.getText();
                if (!input.matches("[a-zA-Z0-9]*")) {
                    mostrarError("El nombre de usuario solo debe contener letras y números");
                    txtUsuario.setText(input.replaceAll("[^a-zA-Z0-9]", ""));
                }
            }
        });
        
        // Manejar tecla Enter en el campo de contraseña
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    intentarLogin();
                }
            }
        });
    }
    
    private void intentarLogin() {
    String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        logger.debug("Intento de login para usuario: {}", usuario);

        if (usuario.isEmpty() || password.isEmpty()) {
            logger.warn("Intento de login con campos vacíos");
            mostrarError("Por favor complete todos los campos");
            return;
        }

        setControlsEnabled(false);
        lblMensaje.setText("Autenticando...");
        lblMensaje.setForeground(Color.BLUE);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String mensajeError = null;

            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    logger.debug("Iniciando autenticación para usuario: {}", usuario);
                    return userController.login(usuario, password);
                } catch (Exception e) {
                    LogUtils.logError("Error en autenticación", e);
                    mensajeError = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean loginExitoso = get();
                    if (loginExitoso) {
                        logger.info("Login exitoso para usuario: {}", usuario);
                        loginExitoso();
                    } else {
                        logger.warn("Login fallido para usuario: {}", usuario);
                        String mensaje = mensajeError != null ? 
                            mensajeError : "Usuario o contraseña incorrectos";
                        mostrarError(mensaje);
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } catch (Exception e) {
                    LogUtils.logError("Error en proceso de login", e);
                    mostrarError("Error en proceso de login: " + e.getMessage());
                } finally {
                    setControlsEnabled(true);
                }
            }
        };

        worker.execute();
    }
    
    
    /*LOGINEXITOSO FUNCIONANDO 
    private void loginExitoso() {
           logger.info("Login exitoso para usuario: {}", txtUsuario.getText());
    try {
        Usuario usuarioActual = userController.getUsuarioActual();
        if (usuarioActual == null) {
            throw new IllegalStateException("Usuario no encontrado después del login");
        }
        logger.debug("Usuario actual: {}, Rol: {}", 
            usuarioActual.getUsername(), 
            usuarioActual.getRol() != null ? usuarioActual.getRol().getNombre() : "Sin rol");

                
        // Ocultar ventana de login
        setVisible(false);

        SwingUtilities.invokeLater(() -> {
            try {
                // Inicializar DAOs
                ProductoDao productoDao = new ProductoDao();
                RecepcionMercanciaDao recepcionDao = new RecepcionMercanciaDao();
                
                // Inicializar Servicios
                ProductoService productoService = new ProductoService(productoDao);
                RecepcionMercanciaService recepcionService = new RecepcionMercanciaService(recepcionDao, productoDao);
                
                RolDao rolDao = new RolDao();
                PermisosPantallaDao permisosPantallaDao = new PermisosPantallaDao();
        
                RolService rolService = new RolService(rolDao);
                PermisosPantallaService permisosPantallaService = new PermisosPantallaService(permisosPantallaDao);
                
                // Crear y mostrar ventana principal
                MainView mainView = new MainView(
                    userController,
                    productoService,
                        permisoPantallaController,
                        permisosPantallaService,
                    recepcionService
                    
                );

                mainView.setVisible(true);
                dispose();
                
            } catch (Exception e) {
                logger.error("Error al crear MainView", e);
                JOptionPane.showMessageDialog(
                    LoginView.this,
                    "Error al iniciar la aplicación:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                setVisible(true);
                txtPassword.setText("");
                txtUsuario.requestFocus();
            }
        });
    } catch (Exception e) {
        logger.error("Error en loginExitoso", e);
        JOptionPane.showMessageDialog(
            this,
            "Error al iniciar sesión:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        setVisible(true);
        txtPassword.setText("");
        txtUsuario.requestFocus();
    }
}*/
    private void loginExitoso() {
     try {
            Usuario usuarioActual = userController.getUsuarioActual();
            if (usuarioActual == null) {
                throw new IllegalStateException("Usuario no encontrado después del login");
            }
            
            setVisible(false);
            
            // Actualizar el usuario actual en el controlador de permisos
            permisoPantallaController = new PermisoPantallaController(
                permisosPantallaService,
                userController.getRolService(),
                usuarioActual
            );
            
            // Inicializar DAOs
            ProductoDao productoDao = new ProductoDao();
            RecepcionMercanciaDao recepcionDao = new RecepcionMercanciaDao();
            
            // Inicializar Servicios
            ProductoService productoService = new ProductoService(productoDao);
            RecepcionMercanciaService recepcionService = new RecepcionMercanciaService(
                recepcionDao, 
                productoDao
            );
            
            // Validar y redirigir usando el PermissionHandler
            PermissionHandler.validarYRedirigir(
                usuarioActual,
                userController,
                productoService,
                permisoPantallaController,
                permisosPantallaService,
                recepcionService,
                this
            );
            
        } catch (Exception e) {
            logger.error("Error al iniciar sesión", e);
            JOptionPane.showMessageDialog(
                this,
                "Error al iniciar sesión:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            setVisible(true);
            txtPassword.setText("");
            txtUsuario.requestFocus();
        }
}
    

    // Método auxiliar para crear servicios
    private void inicializarServicios() throws DatabaseException {
    try {
        // Verificar conexión a base de datos
        if (!DatabaseConfig.testConnection()) {
            throw new DatabaseException("No se pudo establecer conexión con la base de datos");
        }

        // Aquí puedes inicializar otros servicios necesarios
        logger.info("Servicios inicializados correctamente");
        
    } catch (Exception e) {
        logger.error("Error al inicializar servicios", e);
        throw new DatabaseException("Error al inicializar servicios: " + e.getMessage());
    }
}



    // Método estático para mostrar la ventana de login
    public static void mostrar(UsuarioController controlador) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                LoginView loginView = new LoginView(controlador);
                loginView.setVisible(true);
            } catch (Exception e) {
                logger.error("Error al mostrar ventana de login", e);
                JOptionPane.showMessageDialog(
                    null,
                    "Error al iniciar el sistema: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
   
    private void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setForeground(Color.RED);
        realizarEfectoShake();
        logger.warn("Error de login: {}", mensaje);

        // Reproducir sonido de error si está disponible
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
        logger.debug("No se pudo reproducir sonido de error", e);
        }
    }
    
    private void realizarEfectoShake() {
        final Point point = getLocation();
        final int delay = 50;
        final int distance = 10;
        
        Timer timer = new Timer(delay, null);
        timer.addActionListener(new AbstractAction() {
            private int numShakes = 0;
            private boolean goingRight = true;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                Point p = getLocation();
                setLocation(p.x + (goingRight ? distance : -distance), p.y);
                goingRight = !goingRight;
                numShakes++;
                if (numShakes == 4) {
                    timer.stop();
                    setLocation(point);
                }
            }
        });
        timer.start();
    }
    
    private void setControlsEnabled(boolean enabled) {
        txtUsuario.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        btnIngresar.setEnabled(enabled);
        btnCancelar.setEnabled(enabled);
        
        if (enabled) {
            if (txtUsuario.getText().trim().isEmpty()) {
                txtUsuario.requestFocus();
            } else if (txtPassword.getPassword().length == 0) {
                txtPassword.requestFocus();
            }
        }
     setCursor(enabled ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }    
    
    
    // Clase interna para manejar autenticación en segundo plano
    private class AutenticacionWorker extends SwingWorker<Boolean, Void> {
        private final String usuario;
        private final String password;

        public AutenticacionWorker(String usuario, String password) {
            this.usuario = usuario;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            return userController.autenticar(usuario, password);
        }

        @Override
        protected void done() {
            try {
                boolean loginExitoso = get();
                if (loginExitoso) {
                    loginExitoso();
                } else {
                    mostrarError("Usuario o contraseña incorrectos");
                }
            } catch (Exception e) {
                logger.error("Error durante la autenticación", e);
                mostrarError("Error al intentar ingresar al sistema");
            } finally {
                setControlsEnabled(true);
            }
        }
    }
    
}
    