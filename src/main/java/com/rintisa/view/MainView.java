
package com.rintisa.view;

import com.rintisa.controller.RecepcionMercanciaController;
import com.rintisa.controller.UsuarioController;
import com.rintisa.controller.RolController;
import com.rintisa.dao.impl.ProductoDao;
import com.rintisa.dao.impl.ProveedorDao;
import com.rintisa.dao.impl.RecepcionMercanciaDao;
import com.rintisa.exception.DatabaseException;
import com.rintisa.model.Usuario;
import com.rintisa.model.Rol;
import com.rintisa.service.impl.ProductoService;
import com.rintisa.service.impl.ProveedorService;
import com.rintisa.service.impl.RecepcionMercanciaService;
import com.rintisa.service.impl.RecepcionReporteService;


import com.rintisa.service.interfaces.IUsuarioService;
import com.rintisa.service.interfaces.IRolService;
import com.rintisa.service.interfaces.IProductoService;
import com.rintisa.service.interfaces.IProveedorService;
import com.rintisa.service.interfaces.IRecepcionMercanciaService;

import com.rintisa.util.SwingUtils;
import com.rintisa.util.ReportGenerator;
import com.rintisa.view.ReportDialog;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

////MAIN FUNCIONANDO///
public class MainView extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);
    
    // Controladores
    private final UsuarioController userController;
   
    private final RolController rolController;
    private final IUsuarioService usuarioService;
    private final IRolService rolService;
    private final Usuario usuarioActual;
    private final IRecepcionMercanciaService recepcionService;
    private final IProductoService productoService;
    
    // Componentes principales
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPanel contentPanel;
    private JLabel statusBar;
    
    // Menús
    private JMenu menuArchivo;
    private JMenu menuAdministracion;
    private JMenu menuReportes;
    private JMenu menuAyuda;
    
    // Constructor
    public MainView(UsuarioController userController, 
                   IProductoService productoService,
                   IRecepcionMercanciaService recepcionService) {
        
        logger.debug("Iniciando construcción de MainView");
        
        // Validar parámetros
        if (userController == null) {
            throw new IllegalArgumentException("usuarioController no puede ser null");
        }
        if (productoService == null) {
            throw new IllegalArgumentException("productoService no puede ser null");
        }
        if (recepcionService == null) {
            throw new IllegalArgumentException("recepcionService no puede ser null");
        }
        
        this.userController  = userController ;
        this.usuarioActual = userController.getUsuarioActual();
        this.rolController = userController.getRolController();
        this.usuarioService = userController.getUsuarioService();
        this.rolService = userController.getRolService();
        
         // Inicializar DAOs
        ProductoDao productoDao = new ProductoDao();
        ProveedorDao proveedorDao = new ProveedorDao();
        RecepcionMercanciaDao recepcionDao = new RecepcionMercanciaDao();
        
        // Inicializar Servicios
        this.productoService = new ProductoService(productoDao);
        this.recepcionService = new RecepcionMercanciaService(recepcionDao);
        
        
        if (this.usuarioActual == null) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        
        initComponents();
        configurarVentana();
        configurarMenus();
        configurarToolBar();
        configurarStatusBar();
        configurarEventos();
        
        // Mostrar panel de bienvenida
        mostrarPanelBienvenida();
        
        logger.info("Ventana principal inicializada para usuario: {}", 
                   usuarioActual.getUsername());
    }
    
    private void initComponents() {
        // Panel principal
        mainPanel = new JPanel(new BorderLayout());
        
        // Panel de contenido
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Barra de estado
        statusBar = new JLabel(" ");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        
        // Agregar componentes al panel principal
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void configurarVentana() {
        setTitle("Sistema RINTISA - " + usuarioActual.getNombre() + " " + 
                usuarioActual.getApellido());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        // Icono de la aplicación
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/logo.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            logger.warn("No se pudo cargar el icono de la aplicación");
        }
        
        // Confirmar antes de cerrar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida();
            }
        });
    }
    
    
    private void configurarMenus() {
        menuBar = new JMenuBar();
        
        // Menú Archivo
        menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);
        
        JMenuItem menuCambiarPassword = new JMenuItem("Cambiar Contraseña", 
            new ImageIcon(getClass().getResource("/icons/key.png")));
        menuCambiarPassword.addActionListener(e -> mostrarDialogoCambiarPassword());
        
        JMenuItem menuCerrarSesion = new JMenuItem("Cerrar Sesión", 
            new ImageIcon(getClass().getResource("/icons/key.png")));
         menuCerrarSesion.addActionListener(e -> {
            try {
                cerrarSesion();
            } catch (DatabaseException ex) {
                java.util.logging.Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        JMenuItem menuSalir = new JMenuItem("Salir", 
            new ImageIcon(getClass().getResource("/icons/cancel.png")));//falta iconos salir
        menuSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 
            InputEvent.CTRL_DOWN_MASK));
        menuSalir.addActionListener(e -> confirmarSalida());
        
        menuArchivo.add(menuCambiarPassword);
        menuArchivo.addSeparator();
        menuArchivo.add(menuCerrarSesion);
        menuArchivo.addSeparator();
        menuArchivo.add(menuSalir);
        
        // Menú Administración
        menuAdministracion = new JMenu("Administración");
        menuAdministracion.setMnemonic(KeyEvent.VK_D);
        
        JMenuItem menuUsuarios = new JMenuItem("Gestión de Usuarios", 
            new ImageIcon(getClass().getResource("/icons/user.png")));
        menuUsuarios.addActionListener(e -> mostrarGestionUsuarios());
        
        JMenuItem menuRoles = new JMenuItem("Gestión de Roles", 
            new ImageIcon(getClass().getResource("/icons/user.png")));//falta iconos roles
        menuRoles.addActionListener(e -> mostrarGestionRoles());
        
        menuAdministracion.add(menuUsuarios);
        menuAdministracion.add(menuRoles);
        
         // Solo mostrar menú de administración si es admin
        String rolUsuario = usuarioActual.getRol() != null ? usuarioActual.getRol().getNombre() : "";
        menuAdministracion.setVisible("ADMIN".equals(rolUsuario));

        
        // El menú de administración solo es visible para administradores
        menuAdministracion.setVisible(userController.esAdministrador(usuarioActual));
        
        // Menú Reportes
        menuReportes = new JMenu("Reportes");
        menuReportes.setMnemonic(KeyEvent.VK_R);
        
        JMenuItem menuReporteUsuarios = new JMenuItem("Reporte de Usuarios", 
            new ImageIcon(getClass().getResource("/icons/user.png")));//falta iconos reporte usuario
        JMenuItem menuReporteRoles = new JMenuItem("Reporte de Roles", 
            new ImageIcon(getClass().getResource("/icons/user.png")));//falta iconos reporte roles
        JMenuItem menuReporteAccesos = new JMenuItem("Reporte de Accesos", 
            new ImageIcon(getClass().getResource("/icons/user.png")));//falta iconos reporte accesos
        
        // Menú Almacén
        JMenu menuAlmacen = new JMenu("Almacén");
        menuAlmacen.setMnemonic(KeyEvent.VK_A);

        JMenuItem menuRecepcion = new JMenuItem("Recepción de Mercancía",
            new ImageIcon(getClass().getResource("/icons/user.png")));
        JMenuItem menuInventario = new JMenuItem("Inventario",
            new ImageIcon(getClass().getResource("/icons/user.png")));
        
        menuRecepcion.addActionListener(e -> mostrarRecepcionMercancia());
     //   menuInventario.addActionListener(e -> mostrarInventario()); //falta implementar

        menuAlmacen.add(menuRecepcion);
        menuAlmacen.add(menuInventario);

        // Solo mostrar menú de almacén si el usuario tiene el rol correspondiente
        menuAlmacen.setVisible(tieneRolAlmacen(usuarioActual));

        // Agregar el menú a la barra después de menuAdministracion
            menuBar.add(menuAlmacen);
           
        
        menuReporteUsuarios.addActionListener(e -> generarReporteUsuarios());
        menuReporteRoles.addActionListener(e -> generarReporteRoles());
        menuReporteAccesos.addActionListener(e -> generarReporteAccesos());
        
        menuReportes.add(menuReporteUsuarios);
        menuReportes.add(menuReporteRoles);
        menuReportes.add(menuReporteAccesos);
        
        // Menú Ayuda
        menuAyuda = new JMenu("Ayuda");
        menuAyuda.setMnemonic(KeyEvent.VK_Y);
        
        JMenuItem menuAcercaDe = new JMenuItem("Acerca de...", 
            new ImageIcon(getClass().getResource("/icons/logo.png")));
        menuAcercaDe.addActionListener(e -> mostrarAcercaDe());
        
        menuAyuda.add(menuAcercaDe);
        
        // Agregar menús a la barra
        menuBar.add(menuArchivo);
        menuBar.add(menuAdministracion);
        menuBar.add(menuReportes);
        menuBar.add(menuAyuda);
        
        setJMenuBar(menuBar);
    }
    
    private void configurarToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        // Botones de la barra de herramientas
        JButton btnUsuarios = new JButton(new ImageIcon(
            getClass().getResource("/icons/user.png")));
        btnUsuarios.setToolTipText("Gestión de Usuarios");
        btnUsuarios.addActionListener(e -> mostrarGestionUsuarios());
        
        JButton btnRoles = new JButton(new ImageIcon(
            getClass().getResource("/icons/user.png")));///////Buscar Cambio de Icono a Roles
        btnRoles.setToolTipText("Gestión de Roles");
        btnRoles.addActionListener(e -> mostrarGestionRoles());
        
        JButton btnReportes = new JButton(new ImageIcon(
            getClass().getResource("/icons/user.png")));///////Buscar Cambio de Icono a Reportes
        btnReportes.setToolTipText("Reportes");
        btnReportes.addActionListener(e -> mostrarMenuReportes(btnReportes));
        
        toolBar.add(btnUsuarios);
        toolBar.add(btnRoles);
        toolBar.addSeparator();
        toolBar.add(btnReportes);
        
        // Solo mostrar botones de administración si el usuario es administrador
        btnUsuarios.setVisible(userController.esAdministrador(usuarioActual));
        btnRoles.setVisible(userController.esAdministrador(usuarioActual));
        
        mainPanel.add(toolBar, BorderLayout.NORTH);
    }
    
    private void configurarEventos() {
        // Configurar evento de cierre de ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida();
            }
        });
        
        // Registrar acciones globales
        registerKeyStrokes();
        
        // Configurar timer para actualizar la barra de estado
        new Timer(60000, e -> actualizarStatusBar()).start();
    }
    
    private void registerKeyStrokes() {
        // Registrar atajo Ctrl+Q para salir
        KeyStroke salirKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, 
            InputEvent.CTRL_DOWN_MASK);
        getRootPane().registerKeyboardAction(
            e -> confirmarSalida(),
            "salir",
            salirKeyStroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Registrar F1 para ayuda
        KeyStroke ayudaKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        getRootPane().registerKeyboardAction(
            e -> mostrarAyuda(),
            "ayuda",
            ayudaKeyStroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Registrar Ctrl+U para gestión de usuarios (solo admin)
        if (userController.esAdministrador(usuarioActual)) {
            KeyStroke usuariosKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_U, 
                InputEvent.CTRL_DOWN_MASK);
            getRootPane().registerKeyboardAction(
                e -> mostrarGestionUsuarios(),
                "usuarios",
                usuariosKeyStroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }
    }
    
    private void mostrarAyuda() {
        JOptionPane.showMessageDialog(this,
            "Atajos de teclado:\n\n" +
            "F1 - Mostrar esta ayuda\n" +
            "Ctrl+Q - Salir del sistema\n" +
            (userController.esAdministrador(usuarioActual) ? 
             "Ctrl+U - Gestión de usuarios\n" : "") +
            "\nPara más información, consulte el manual del usuario.",
            "Ayuda",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
     private void cerrarSesion() throws DatabaseException {
        int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea cerrar sesión?",
            "Cerrar Sesión",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            dispose();
            userController.logout();
            // Mostrar ventana de login
            LoginView.mostrar(userController);
        }
    }
      
    private void configurarStatusBar() {
        // Mostrar información del usuario en la barra de estado
        actualizarStatusBar();
        
        // Timer para actualizar la hora cada minuto
        new Timer(60000, e -> actualizarStatusBar()).start();
    }
    

    // Métodos para cambiar el contenido principal
    private void cambiarPanel(JPanel nuevoPanel, String titulo) {
        contentPanel.removeAll();
        contentPanel.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
        contentPanel.add(nuevoPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        actualizarStatusBar();
    }

    private void mostrarPanelBienvenida() {
        JPanel panelBienvenida = new JPanel(new BorderLayout(10, 10));
        panelBienvenida.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Mensaje de bienvenida
        JLabel lblBienvenida = new JLabel("¡Bienvenido al Sistema RINTISA!");
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 24));
        lblBienvenida.setHorizontalAlignment(SwingConstants.CENTER);

        // Información del usuario
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        infoPanel.add(new JLabel("Usuario: " + usuarioActual.getNombre() + " " + 
                                usuarioActual.getApellido()), gbc);
        gbc.gridy++;
        infoPanel.add(new JLabel("Rol: " + usuarioActual.getRol().getNombre()), gbc);
        gbc.gridy++;
        infoPanel.add(new JLabel("Último acceso: " + 
                     (usuarioActual.getUltimoAcceso() != null ? 
                      usuarioActual.getUltimoAcceso().toString() : "Primer acceso")), gbc);

        panelBienvenida.add(lblBienvenida, BorderLayout.NORTH);
        panelBienvenida.add(infoPanel, BorderLayout.CENTER);

        cambiarPanel(panelBienvenida, "Inicio");
    }

    // Métodos para mostrar diferentes secciones
    private void mostrarGestionUsuarios() {
        try {
            // Verificar si el usuario es administrador
            if (!userController.esAdministrador(usuarioActual)) {
                JOptionPane.showMessageDialog(this,
                    "No tiene permisos para acceder a esta función",
                    "Acceso Denegado",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Limpiar panel de contenido
            contentPanel.removeAll();

            // Crear y agregar el panel de gestión de usuarios
            UsuariosView usuariosView = new UsuariosView(userController);
            contentPanel.add(usuariosView, BorderLayout.CENTER);

            // Actualizar la interfaz
            contentPanel.revalidate();
            contentPanel.repaint();
            
            logger.info("Panel de gestión de usuarios mostrado");
            
        } catch (Exception e) {
            logger.error("Error al mostrar gestión de usuarios", e);
            JOptionPane.showMessageDialog(this,
                "Error al abrir la gestión de usuarios: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarGestionRoles() {
        if (!userController.esAdministrador(usuarioActual)) {
            mostrarMensajeError("No tiene permisos para acceder a esta función");
            return;
        }
        try {
            RolesView rolesView = new RolesView(userController.getRolController());
            cambiarPanel(rolesView, "Gestión de Roles");
        } catch (Exception e) {
            logger.error("Error al mostrar gestión de roles", e);
            mostrarMensajeError("Error al cargar la gestión de roles: " + e.getMessage());
        }
    }

    // Métodos para reportes
    private void generarReporteUsuarios() {
         try {
            java.util.List<Usuario> usuarios = usuarioService.listarTodos();
            String rutaReporte = ReportGenerator.generarReporteUsuarios(usuarios);
            ReportDialog.mostrarDialogoReporteGenerado(this, rutaReporte);
        } catch (Exception e) {
            logger.error("Error al generar reporte de usuarios", e);
            JOptionPane.showMessageDialog(this,
                "Error al generar reporte: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarReporteRoles() {
          try {
            java.util.List<Rol> roles = rolService.listarTodos();
            String rutaReporte = ReportGenerator.generarReporteRoles(roles);
            ReportDialog.mostrarDialogoReporteGenerado(this, rutaReporte);
        } catch (Exception e) {
            logger.error("Error al generar reporte de roles", e);
            JOptionPane.showMessageDialog(this,
                "Error al generar reporte: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarReporteAccesos() {
        try {
            String rutaReporte = ReportGenerator.generarReporteAccesos(
                userController.getUsuarioService().obtenerRegistroAccesos()
            );
            ReportDialog.mostrarDialogoReporteGenerado(this, rutaReporte);
        } catch (Exception e) {
            logger.error("Error al generar reporte de accesos", e);
            JOptionPane.showMessageDialog(this,
                "Error al generar reporte: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Métodos de utilidad
    private void actualizarStatusBar() {
        String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                          .format(new java.util.Date());
        statusBar.setText(" Usuario: " + usuarioActual.getUsername() + 
                         " | Rol: " + usuarioActual.getRol().getNombre() + 
                         " | Fecha: " + fecha);
    }

    private void mostrarDialogoCambiarPassword() {
        JPasswordField oldPass = new JPasswordField();
        JPasswordField newPass = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();

        Object[] mensaje = {
            "Contraseña actual:", oldPass,
            "Nueva contraseña:", newPass,
            "Confirmar contraseña:", confirmPass
        };

        int opcion = JOptionPane.showConfirmDialog(
            this, mensaje, "Cambiar Contraseña", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE
        );

        if (opcion == JOptionPane.OK_OPTION) {
            try {
                String oldPassStr = new String(oldPass.getPassword());
                String newPassStr = new String(newPass.getPassword());
                String confirmPassStr = new String(confirmPass.getPassword());

                if (!newPassStr.equals(confirmPassStr)) {
                    mostrarMensajeError("Las nuevas contraseñas no coinciden");
                    return;
                }

                userController.cambiarPassword(
                    usuarioActual.getId(), oldPassStr, newPassStr
                );
                mostrarMensajeInfo("Contraseña cambiada exitosamente");

            } catch (Exception e) {
                logger.error("Error al cambiar contraseña", e);
                mostrarMensajeError("Error al cambiar contraseña: " + e.getMessage());
            }
        }
    }

    private void confirmarSalida() {
        int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea salir del sistema?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            logger.info("Usuario {} cerrando sesión", usuarioActual.getUsername());
            dispose();
            System.exit(0);
        }
    }
    
    private boolean tieneRolAlmacen(Usuario usuario) {
    return usuario != null && 
           usuario.getRol() != null && 
           "ALMACEN".equalsIgnoreCase(usuario.getRol().getNombre());
}

    private void mostrarRecepcionMercancia() {
    try {
        // Inicializar los DAOs necesarios
        ProveedorDao proveedorDao = new ProveedorDao();
        ProductoDao productoDao = new ProductoDao();
        RecepcionMercanciaDao recepcionDao = new RecepcionMercanciaDao();

        // Inicializar los Servicios
        IProveedorService proveedorService = new ProveedorService(proveedorDao);
        IProductoService productoService = new ProductoService(productoDao);
        IRecepcionMercanciaService recepcionService = new RecepcionMercanciaService(
            recepcionDao, 
            productoDao);

        // Crear el servicio de reportes
        RecepcionReporteService reporteService = new RecepcionReporteService(recepcionService);

        // Crear el controlador con todos los servicios necesarios
        RecepcionMercanciaController controller = new RecepcionMercanciaController(
            recepcionService,
            productoService,
            proveedorService,
            userController,
            reporteService
        );

        // Crear la vista
        RecepcionMercanciaView vista = new RecepcionMercanciaView(controller);

        // Limpiar y mostrar la nueva vista
        contentPanel.removeAll();
        contentPanel.add(vista);
        contentPanel.revalidate();
        contentPanel.repaint();

        // Actualizar el título de la ventana
        setTitle("RINTISA - Recepción de Mercancía");

    } catch (Exception e) {
        logger.error("Error al mostrar la vista de Recepción de Mercancía", e);
        JOptionPane.showMessageDialog(
            this,
            "Error al abrir la vista de Recepción de Mercancía: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
            );
        }
}
    
    
    

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(
            this,
            "Sistema RINTISA\nVersión 1.0\n\n" +
            "Desarrollado por Grupo08@UTP\n" +
            "© 2024 Todos los derechos reservados",
            "Acerca de",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void mostrarMensajeError(String mensaje) {
        JOptionPane.showMessageDialog(
            this, mensaje, "Error", JOptionPane.ERROR_MESSAGE
        );
    }

    private void mostrarMensajeInfo(String mensaje) {
        JOptionPane.showMessageDialog(
            this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void mostrarMenuReportes(Component componente) {
        JPopupMenu menuPopup = new JPopupMenu();
        
        JMenuItem itemReporteUsuarios = new JMenuItem("Reporte de Usuarios");
        itemReporteUsuarios.addActionListener(e -> generarReporteUsuarios());
        
        JMenuItem itemReporteRoles = new JMenuItem("Reporte de Roles");
        itemReporteRoles.addActionListener(e -> generarReporteRoles());
        
        JMenuItem itemReporteAccesos = new JMenuItem("Reporte de Accesos");
        itemReporteAccesos.addActionListener(e -> generarReporteAccesos());
        
        menuPopup.add(itemReporteUsuarios);
        menuPopup.add(itemReporteRoles);
        menuPopup.add(itemReporteAccesos);
        
        menuPopup.show(componente, 0, componente.getHeight());
    }
}
/////MAIN FUNCIONANDO 
