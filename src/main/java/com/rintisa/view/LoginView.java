/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.view;

import com.rintisa.controller.UsuarioController;
import com.rintisa.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginView extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginView.class);
    
    private final UsuarioController usuarioController;
    
    // Componentes de la interfaz
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnCancelar;
    private JLabel lblMensaje;
    
    // Constructor
    public LoginView(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
        initComponents();
        configurarVentana();
        configurarEventos();
    }
    
    private void initComponents() {
        // Panel principal con padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        JLabel lblTitulo = new JLabel("Iniciar Sesión");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(lblTitulo, gbc);
        
        // Campo Usuario
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Usuario:"), gbc);
        
        gbc.gridx = 1;
        txtUsuario = new JTextField(20);
        formPanel.add(txtUsuario, gbc);
        
        // Campo Contraseña
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Contraseña:"), gbc);
        
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        formPanel.add(txtPassword, gbc);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnIngresar = new JButton("Ingresar");
        btnCancelar = new JButton("Cancelar");
        buttonPanel.add(btnIngresar);
        buttonPanel.add(btnCancelar);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Label para mensajes
        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(Color.RED);
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        formPanel.add(lblMensaje, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }
    
private void configurarVentana() {
        setTitle("Sistema RINTISA - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Centrar en pantalla
        
        // Icono de la aplicación (si existe)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            logger.warn("No se pudo cargar el icono de la aplicación");
        }
    }
    
    private void configurarEventos() {
        // Evento del botón Ingresar
        btnIngresar.addActionListener((ActionEvent e) -> {
            intentarLogin();
        });
        
        // Evento del botón Cancelar
        btnCancelar.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });
        
        // Manejar tecla Enter en el campo de usuario
        txtUsuario.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
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
        
        // Validar campos vacíos
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor complete todos los campos");
            return;
        }
        
        // Deshabilitar controles durante la autenticación
        setControlsEnabled(false);
        lblMensaje.setText("Autenticando...");
        lblMensaje.setForeground(Color.BLUE);
        
        // Usar SwingWorker para no bloquear la UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return usuarioController.autenticar(usuario, password);
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
        };
        
        worker.execute();
    }
    
private void loginExitoso() {
        logger.info("Login exitoso para usuario: {}", txtUsuario.getText());
        
        // Ocultar ventana de login
        setVisible(false);
        
        // Mostrar ventana principal
        SwingUtilities.invokeLater(() -> {
            try {
                MainView mainView = new MainView(usuarioController);
                mainView.setVisible(true);
                dispose(); // Cerrar ventana de login
            } catch (Exception e) {
                logger.error("Error al abrir ventana principal", e);
                JOptionPane.showMessageDialog(
                    this,
                    "Error al abrir la ventana principal: " + e.getMessage(),
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
        // Efecto de shake en caso de error
        realizarEfectoShake();
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
                if (goingRight) {
                    setLocation(p.x + distance, p.y);
                } else {
                    setLocation(p.x - distance, p.y);
                }
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
            // Si se habilitan los controles, dar foco al campo que esté vacío
            if (txtUsuario.getText().trim().isEmpty()) {
                txtUsuario.requestFocus();
            } else if (txtPassword.getPassword().length == 0) {
                txtPassword.requestFocus();
            }
        }
    }
    
    // Método estático para mostrar la ventana de login
    public static void mostrar(UsuarioController usuarioController) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                LoginView loginView = new LoginView(usuarioController);
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
    
    // Método main para pruebas
    public static void main(String[] args) {
        // Aquí deberías inicializar tus servicios y controladores
        UsuarioController usuarioController = null; // Inicializar apropiadamente
        mostrar(usuarioController);
    }
}*/

/*
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.rintisa.util.ResourceUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;

public class LoginView extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginView.class);
    private static final long serialVersionUID = 1L;
    
    private final UsuarioController usuarioController;
    
    // Componentes de la interfaz
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnCancelar;
    private JLabel lblMensaje;
    
    // Constructor
    public LoginView(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
        initComponents();
        configurarVentana();
        configurarEventos();
    }
    
    private void initComponents() {
         setLayout(new BorderLayout());
        
        // Panel principal con padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
         // Logo de la aplicación
        JLabel lblLogo = new JLabel();
        ImageIcon logo = ResourceUtil.getImageIcon("logo.png");
        if (logo != null) {
            lblLogo.setIcon(logo);
        }
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(lblLogo, gbc);
        
        
        // Título
        JLabel lblTitulo = new JLabel("Iniciar Sesión");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(lblTitulo, gbc);
        
        // Campo Usuario
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Usuario:"), gbc);
        
        /////Icono usuario
        ImageIcon userIcon = ResourceUtil.getImageIcon("user.png");
        if (userIcon != null) {
            lblMensaje.setIcon(userIcon);
        }
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(lblMensaje, gbc);
        
        gbc.gridx = 1;
        txtUsuario = new JTextField(20);
        formPanel.add(txtUsuario, gbc);
        
        // Campo Contraseña
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Contraseña:"), gbc);
        
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        formPanel.add(txtPassword, gbc);
        
        // Checkbox de Mostrar Contraseña
        JCheckBox mostrarContraseña = new JCheckBox("Mostrar contraseña");
        mostrarContraseña.addActionListener(e -> {
            if (mostrarContraseña.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('\u2022');
            }
        });
        gbc.gridy = 3;
        formPanel.add(mostrarContraseña, gbc);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnIngresar = new JButton("Ingresar");
        btnCancelar = new JButton("Cancelar");
        buttonPanel.add(btnIngresar);
        buttonPanel.add(btnCancelar);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Label para mensajes
        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(Color.RED);
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 5;
        formPanel.add(lblMensaje, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }
    
    private void configurarVentana() {
        setTitle("Sistema RINTISA - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Centrar en pantalla
        
        // Icono de la aplicación (si existe)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            logger.warn("No se pudo cargar el icono de la aplicación");
        }
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
        
        // Validar campos vacíos
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor complete todos los campos");
            return;
        }
        
        // Deshabilitar controles durante la autenticación
        setControlsEnabled(false);
        lblMensaje.setText("Autenticando...");
        lblMensaje.setForeground(Color.BLUE);
        
        // Usar SwingWorker para no bloquear la UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                logger.debug("Intentando autenticar usuario: {}", usuario);
                return usuarioController.login(usuario, password);
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
                        mostrarError("Usuario o contraseña incorrectos");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } catch (Exception e) {
                    logger.error("Error durante el login", e);
                    mostrarError("Error al intentar ingresar: " + e.getMessage());
                } finally {
                    setControlsEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    
    private void loginExitoso() {
        logger.info("Login exitoso para usuario: {}", txtUsuario.getText());
        
        // Ocultar ventana de login
        setVisible(false);
        
        // Mostrar ventana principal
        SwingUtilities.invokeLater(() -> {
            try {
                MainView mainView = new MainView(usuarioController);
                mainView.setVisible(true);
                dispose(); // Cerrar ventana de login
            } catch (Exception e) {
                logger.error("Error al abrir ventana principal", e);
                JOptionPane.showMessageDialog(
                    this,
                    "Error al abrir la ventana principal: " + e.getMessage(),
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
        logger.warn("Error de login: {}", mensaje);
        
        // Efecto de shake para feedback visual
        realizarEfectoShake();
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
            return usuarioController.autenticar(usuario, password);
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
    
    // Método estático para mostrar la ventana de login
    public static void mostrar(UsuarioController usuarioController) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                LoginView loginView = new LoginView(usuarioController);
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
}
*/


///////////VERISON CON ICONOS FUNCIONA//////////////////////////
import com.rintisa.controller.UsuarioController;
import com.rintisa.util.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginView extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginView.class);
    private static final long serialVersionUID = 1L;
    
    private final UsuarioController usuarioController;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnCancelar;
    private JLabel lblMensaje;

    public LoginView(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
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
        
        // Validar campos vacíos
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor complete todos los campos");
            return;
        }
        
        // Deshabilitar controles durante la autenticación
        setControlsEnabled(false);
        lblMensaje.setText("Autenticando...");
        lblMensaje.setForeground(Color.BLUE);
        
        // Usar SwingWorker para no bloquear la UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                logger.debug("Intentando autenticar usuario: {}", usuario);
                return usuarioController.login(usuario, password);
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
                        mostrarError("Usuario o contraseña incorrectos");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } catch (Exception e) {
                    logger.error("Error durante el login", e);
                    mostrarError("Error al intentar ingresar: " + e.getMessage());
                } finally {
                    setControlsEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    
    private void loginExitoso() {
        logger.info("Login exitoso para usuario: {}", txtUsuario.getText());
        
        // Ocultar ventana de login
        setVisible(false);
        
        // Mostrar ventana principal
        SwingUtilities.invokeLater(() -> {
            try {
                MainView mainView = new MainView(usuarioController);
                mainView.setVisible(true);
                dispose(); // Cerrar ventana de login
            } catch (Exception e) {
                logger.error("Error al abrir ventana principal", e);
                JOptionPane.showMessageDialog(
                    this,
                    "Error al abrir la ventana principal: " + e.getMessage(),
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
        logger.warn("Error de login: {}", mensaje);
        
        // Efecto de shake para feedback visual
        realizarEfectoShake();
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
            return usuarioController.autenticar(usuario, password);
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
    
    // Método estático para mostrar la ventana de login
    public static void mostrar(UsuarioController usuarioController) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                LoginView loginView = new LoginView(usuarioController);
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
}
///////////VERISON CON ICONOS FUNCIONA//////////////////////////


/*///VERSION SIMPLE
/*
import com.rintisa.controller.UsuarioController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginView extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginView.class);
    
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnCancelar;
    private JLabel lblMensaje;
    private final UsuarioController usuarioController;

    public LoginView(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
        setupUI();
    }

    private void setupUI() {
        setTitle("Login - Sistema RINTISA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Panel principal
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);

        // Título
        JLabel lblTitle = new JLabel("SISTEMA RINTISA");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        panel.add(lblTitle, gc);

        // Usuario
        gc.gridy = 1;
        gc.gridwidth = 1;
        panel.add(new JLabel("Usuario:"), gc);

        gc.gridx = 1;
        txtUsuario = new JTextField(15);
        panel.add(txtUsuario, gc);

        // Contraseña
        gc.gridx = 0;
        gc.gridy = 2;
        panel.add(new JLabel("Contraseña:"), gc);

        gc.gridx = 1;
        txtPassword = new JPasswordField(15);
        panel.add(txtPassword, gc);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnIngresar = new JButton("Ingresar");
        btnCancelar = new JButton("Cancelar");
        buttonPanel.add(btnIngresar);
        buttonPanel.add(btnCancelar);

        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 2;
        panel.add(buttonPanel, gc);

        // Mensaje de error
        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(Color.RED);
        gc.gridy = 4;
        panel.add(lblMensaje, gc);

        // Configurar eventos
        btnIngresar.addActionListener(e -> login());
        btnCancelar.addActionListener(e -> System.exit(0));

        txtUsuario.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
                }
            }
        });

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });

        // Configurar ventana
        setContentPane(panel);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void login() {
        String username = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblMensaje.setText("Ingrese usuario y contraseña");
            return;
        }

        try {
            if (usuarioController.login(username, password)) {
                dispose();
                openMainView();
            } else {
                lblMensaje.setText("Usuario o contraseña incorrectos");
                txtPassword.setText("");
                txtPassword.requestFocus();
            }
        } catch (Exception e) {
            logger.error("Error en login", e);
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    private void openMainView() {
        SwingUtilities.invokeLater(() -> {
            try {
                MainView mainView = new MainView(usuarioController);
                mainView.setVisible(true);
            } catch (Exception e) {
                logger.error("Error al abrir ventana principal", e);
                JOptionPane.showMessageDialog(null,
                    "Error al abrir ventana principal: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    public static void mostrar(UsuarioController usuarioController) {
        SwingUtilities.invokeLater(() -> {
            LoginView view = new LoginView(usuarioController);
            view.setVisible(true);
            view.txtUsuario.requestFocus();
        });
    }
    
}
*/