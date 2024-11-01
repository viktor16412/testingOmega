/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.view;

import com.rintisa.controller.UsuarioController;
import com.rintisa.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
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
}
