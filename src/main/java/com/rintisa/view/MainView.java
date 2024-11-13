
package com.rintisa.view;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.rintisa.model.Usuario;
import com.rintisa.model.Rol;
import com.rintisa.model.RegistroAcceso;
import java.awt.Desktop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.rintisa.controller.PermisoPantallaController;
import com.rintisa.controller.ProductoController;
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
import com.rintisa.controller.ProductoController;
import com.rintisa.exception.ValidationException;
import com.rintisa.model.Pantalla;
import com.rintisa.model.Producto;
import com.rintisa.model.RecepcionMercancia;
import com.rintisa.service.interfaces.IPermisosPantallaService;
import com.rintisa.model.enums.EstadoRecepcion;
import com.rintisa.service.interfaces.IUsuarioService;
import com.rintisa.service.interfaces.IRolService;
import com.rintisa.service.interfaces.IProductoService;
import com.rintisa.service.interfaces.IProveedorService;
import com.rintisa.service.interfaces.IRecepcionMercanciaService;
import com.rintisa.util.IconManager;
import com.rintisa.util.ModernUIUtils;

import com.rintisa.util.SwingUtils;
import com.rintisa.util.ReportGenerator;
import com.rintisa.view.ReportDialog;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainView extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);
    
    // Controladores
    private final UsuarioController userController;
    private final RolController rolController;
    private final PermisoPantallaController permisoPantallaController;
    
    // Servicios
    private final IUsuarioService usuarioService;
    private final IRolService rolService;
    private final IPermisosPantallaService permisosPantallaService;
    private final IProductoService productoService;
    private final IRecepcionMercanciaService recepcionService;
    
    // Usuario actual y sus permisos
    private final Usuario usuarioActual;
    private Map<Pantalla, Set<String>> permisosUsuario;
    
    // Componentes principales de la UI
    private JPanel mainPanel;          // Panel principal
    private JMenuBar menuBar;          // Barra de menú
    private JToolBar toolBar;          // Barra de herramientas
    private JPanel contentPanel;       // Panel de contenido
    private JLabel statusBar;          // Barra de estado
    
    // Menús principales
    private JMenu menuArchivo;
    private JMenu menuAdministracion;
    private JMenu menuReportes;
    private JMenu menuAlmacen;
    private JMenu menuAyuda;
    
    // Botones de la barra de herramientas
    private Map<String, JButton> toolbarButtons;

    /**
     * Constructor principal de MainView
     */
    public MainView(
            UsuarioController userController,
            IProductoService productoService,
            PermisoPantallaController permisoPantallaController,
            IPermisosPantallaService permisosPantallaService,
            IRecepcionMercanciaService recepcionService) {
            
        logger.debug("Inicializando MainView");
        
        // Inicializar controladores y servicios
        this.userController = userController;
        this.usuarioService = userController.getUsuarioService();
        this.rolController = userController.getRolController();
        this.rolService = userController.getRolService();
        this.permisoPantallaController = permisoPantallaController;
        this.permisosPantallaService = permisosPantallaService;
        this.productoService = productoService;
        this.recepcionService = recepcionService;
        
        // Obtener usuario actual
        this.usuarioActual = userController.getUsuarioActual();
        if (usuarioActual == null) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        
        // Inicializar mapa de botones
        this.toolbarButtons = new HashMap<>();
        
        try {
            // Cargar permisos del usuario
            cargarPermisosUsuario();
            
            // Inicializar componentes UI
            initComponents();
            configurarVentana();
            configurarMenus();
            configurarToolBar();
            configurarStatusBar();
            configurarEventos();
            
            // Mostrar panel inicial
            mostrarPanelBienvenida();
            
            logger.info("MainView inicializado correctamente para usuario: {}", 
                usuarioActual.getUsername());
                
        } catch (Exception e) {
            logger.error("Error al inicializar MainView", e);
            throw new RuntimeException("Error al inicializar MainView: " + e.getMessage(), e);
        }
    }
    
    private void configurarStatusBar() {
        // Mostrar información del usuario en la barra de estado
        actualizarStatusBar();
        
        // Timer para actualizar la hora cada minuto
        new Timer(60000, e -> actualizarStatusBar()).start();
    }
        
    /**
     * Carga los permisos del usuario actual
     */
    private void cargarPermisosUsuario() throws DatabaseException {
        try {
            if (usuarioActual.getRol() == null) {
                throw new IllegalStateException("El usuario no tiene un rol asignado");
            }
            
            this.permisosUsuario = permisoPantallaController
                .obtenerPermisosUsuario(usuarioActual.getRol().getNombre());
                
            logger.debug("Permisos cargados para usuario {}: {}", 
                usuarioActual.getUsername(), permisosUsuario);
                
        } catch (Exception e) {
            logger.error("Error al cargar permisos del usuario", e);
            throw new DatabaseException("Error al cargar permisos: " + e.getMessage());
        }
    }

    /**
     * Verifica si el usuario tiene acceso a una pantalla específica
     */
    protected boolean tieneAcceso(Pantalla pantalla) {
        Set<String> permisos = permisosUsuario.get(pantalla);
        return permisos != null && permisos.contains("acceso");
    }

    /**
     * Verifica si el usuario tiene permiso de edición en una pantalla
     */
    protected boolean puedeEditar(Pantalla pantalla) {
        Set<String> permisos = permisosUsuario.get(pantalla);
        return permisos != null && permisos.contains("edicion");
    }

    /**
     * Verifica si el usuario tiene permiso de eliminación en una pantalla
     */
    protected boolean puedeEliminar(Pantalla pantalla) {
        Set<String> permisos = permisosUsuario.get(pantalla);
        return permisos != null && permisos.contains("eliminacion");
    }

     /**
     * Inicializa los componentes principales de la interfaz
     */
    private void initComponents() {
        logger.debug("Inicializando componentes principales");
        
        // Panel principal con BorderLayout
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Panel de contenido con bordes
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY)
            )
        );
        
        // Barra de estado
        statusBar = new JLabel(" ");
        statusBar.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, java.awt.Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            )
        );
        
        // Agregar componentes al panel principal
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        // Establecer el panel principal como contenido del frame
        setContentPane(mainPanel);
        
        logger.debug("Componentes principales inicializados");
    }

    /**
     * Configura la barra de herramientas
     */
    private void configurarToolBar() {
        logger.debug("Configurando barra de herramientas");
        
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
            )
        );

        // Botones para Administración
        if (userController.esAdministrador(usuarioActual)) {
            agregarBotonToolbar("usuarios", "Usuarios", "user", 
                e -> navegarAPantalla(Pantalla.USUARIOS));
            
            agregarBotonToolbar("roles", "Roles", "groups", 
                e -> navegarAPantalla(Pantalla.ROLES));
            
            agregarBotonToolbar("permisos", "Permisos", "key", 
                e -> navegarAPantalla(Pantalla.PERMISOS));
            
            toolBar.addSeparator();
        }

        // Botones para Almacén
        if (tieneAcceso(Pantalla.RECEPCION)) {
            agregarBotonToolbar("recepcion", "Recepción", "inbox", 
                e -> navegarAPantalla(Pantalla.RECEPCION));
        }
        
        if (tieneAcceso(Pantalla.PRODUCTOS)) {
            agregarBotonToolbar("productos", "Productos", "package", 
                e -> navegarAPantalla(Pantalla.PRODUCTOS));
        }

        // Botones siempre visibles
        toolBar.addSeparator();
        agregarBotonToolbar("perfil", "Mi Perfil", "user-check", 
            e -> mostrarPerfil());
        agregarBotonToolbar("password", "Cambiar Contraseña", "lock", 
            e -> mostrarDialogoCambiarPassword());
        
        mainPanel.add(toolBar, BorderLayout.NORTH);
        
        logger.debug("Barra de herramientas configurada");
    }

    /**
     * Agrega un botón a la barra de herramientas
     */
    private void agregarBotonToolbar(String id, String tooltip, String iconName, 
                                   ActionListener action) {
        JButton button = new JButton();
        button.setToolTipText(tooltip);
        
        // Configurar icono
        try {
            ImageIcon icon = IconManager.getIcon(iconName, IconManager.SMALL);
            if (icon != null) {
                button.setIcon(icon);
            }
        } catch (Exception e) {
            logger.warn("No se pudo cargar el icono: {}", iconName);
        }
        
        // Configurar apariencia
        button.setFocusable(false);
        button.addActionListener(action);
        
        // Agregar el botón
        toolBar.add(button);
        toolbarButtons.put(id, button);
    }

    /**
     * Configura la ventana principal
     */
    private void configurarVentana() {
        setTitle("Sistema RINTISA - " + usuarioActual.getNombre() + " " + 
                usuarioActual.getApellido());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        // Configurar icono de la aplicación
        try {
            ImageIcon icon = IconManager.getIcon("logo");
            if (icon != null) {
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            logger.warn("No se pudo cargar el icono de la aplicación");
        }
        
        // Manejar cierre de ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida();
            }
        });
    }

    /**
     * Actualiza la barra de estado
     */
    private void actualizarStatusBar() {
        String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
            .format(new java.util.Date());
            
        statusBar.setText(
            String.format(" Usuario: %s | Rol: %s | Fecha: %s",
                usuarioActual.getUsername(),
                usuarioActual.getRol().getNombre(),
                fecha
            )
        );
    }

    /**
     * Muestra diálogo de confirmación para salir
     */
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
    
    /**
     * Configura la barra de menús
     */
    private void configurarMenus() {
        logger.debug("Configurando menús");
        
        menuBar = new JMenuBar();
        
        // Menú Archivo
        configurarMenuArchivo();
        
        // Menú Administración (solo para administradores)
        if (userController.esAdministrador(usuarioActual)) {
            configurarMenuAdministracion();
        }
        
        // Menú Almacén (según permisos)
        if (tieneAccesoAlmacen()) {
            configurarMenuAlmacen();
        }
        
        // Menú Reportes (según permisos)
        configurarMenuReportes();
        
        // Menú Ayuda
        configurarMenuAyuda();
        
        setJMenuBar(menuBar);
        logger.debug("Menús configurados");
    }

    /**
     * Configura el menú Archivo
     */
    private void configurarMenuArchivo() {
        menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);
        
        // Perfil de Usuario
        JMenuItem menuPerfil = crearMenuItem("Mi Perfil", "user-check", 
            KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK),
            e -> mostrarPerfil());
            
        // Cambiar Contraseña
        JMenuItem menuPassword = crearMenuItem("Cambiar Contraseña", "key",
            KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
            e -> mostrarDialogoCambiarPassword());
            
        // Cerrar Sesión
        JMenuItem menuCerrarSesion = crearMenuItem("Cerrar Sesión", "log-out",
            null, e -> cerrarSesion());
            
        // Salir
        JMenuItem menuSalir = crearMenuItem("Salir", "x",
            KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK),
            e -> confirmarSalida());
        
        menuArchivo.add(menuPerfil);
        menuArchivo.addSeparator();
        menuArchivo.add(menuPassword);
        menuArchivo.addSeparator();
        menuArchivo.add(menuCerrarSesion);
        menuArchivo.addSeparator();
        menuArchivo.add(menuSalir);
        
        menuBar.add(menuArchivo);
    }

    /**
     * Configura el menú Administración
     */
    private void configurarMenuAdministracion() {
        menuAdministracion = new JMenu("Administración");
        menuAdministracion.setMnemonic(KeyEvent.VK_D);
        
        // Gestión de Usuarios
        JMenuItem menuUsuarios = crearMenuItem("Gestión de Usuarios", "users",
            KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK),
            e -> navegarAPantalla(Pantalla.USUARIOS));
            
        // Gestión de Roles
        JMenuItem menuRoles = crearMenuItem("Gestión de Roles", "shield",
            KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK),
            e -> navegarAPantalla(Pantalla.ROLES));
            
        // Gestión de Permisos
        JMenuItem menuPermisos = crearMenuItem("Gestión de Permisos", "key",
            KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK),
            e -> navegarAPantalla(Pantalla.PERMISOS));
        
        menuAdministracion.add(menuUsuarios);
        menuAdministracion.add(menuRoles);
        menuAdministracion.add(menuPermisos);
        
        menuBar.add(menuAdministracion);
    }

    /**
     * Configura el menú Almacén
     */
    private void configurarMenuAlmacen() {
        menuAlmacen = new JMenu("Almacén");
        menuAlmacen.setMnemonic(KeyEvent.VK_L);
        
        if (tieneAcceso(Pantalla.RECEPCION)) {
            JMenuItem menuRecepcion = crearMenuItem("Recepción de Mercancía", "inbox",
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK),
                e -> navegarAPantalla(Pantalla.RECEPCION));
            menuAlmacen.add(menuRecepcion);
        }
        
        if (tieneAcceso(Pantalla.PRODUCTOS)) {
            JMenuItem menuProductos = crearMenuItem("Mantenimiento de Productos", "package",
                KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK),
                e -> navegarAPantalla(Pantalla.PRODUCTOS));
            menuAlmacen.add(menuProductos);
        }
        
        menuBar.add(menuAlmacen);
    }

    /**
     * Configura el menú Reportes
     */
    private void configurarMenuReportes() {
        menuReportes = new JMenu("Reportes");
        menuReportes.setMnemonic(KeyEvent.VK_R);
        
        // Reportes de Administración (solo admin)
        if (userController.esAdministrador(usuarioActual)) {
            JMenuItem menuReporteUsuarios = crearMenuItem("Reporte de Usuarios", "file-text",
                null, e -> generarReporteUsuarios());
            JMenuItem menuReporteRoles = crearMenuItem("Reporte de Roles", "file-text",
                null, e -> generarReporteRoles());
            JMenuItem menuReporteAccesos = crearMenuItem("Reporte de Accesos", "file-text",
                null, e -> generarReporteAccesos());
            
            menuReportes.add(menuReporteUsuarios);
            menuReportes.add(menuReporteRoles);
            menuReportes.add(menuReporteAccesos);
            menuReportes.addSeparator();
        }
        
      /*  // Reportes de Almacén
        if (tieneAccesoAlmacen()) {
            JMenuItem menuReporteRecepcion = crearMenuItem("Reporte de Recepciones", "file-text",
                null, e -> generarReporteRecepciones());
            JMenuItem menuReporteProductos = crearMenuItem("Reporte de Productos", "file-text",
                null, e -> generarReporteProductos());
            
            menuReportes.add(menuReporteRecepcion);
            menuReportes.add(menuReporteProductos);
        }
        
        if (menuReportes.getMenuComponentCount() > 0) {
            menuBar.add(menuReportes);
        }*/
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
    


    
    
    
    
    
    /**
     * Configura el menú Ayuda
     */
    private void configurarMenuAyuda() {
        menuAyuda = new JMenu("Ayuda");
        menuAyuda.setMnemonic(KeyEvent.VK_Y);
        
        JMenuItem menuAcercaDe = crearMenuItem("Acerca de...", "info",
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
            e -> mostrarAcercaDe());
            
        menuAyuda.add(menuAcercaDe);
        
        menuBar.add(menuAyuda);
    }

    /**
     * Crea un item de menú con icono y atajo de teclado
     */
    private JMenuItem crearMenuItem(String texto, String iconName, 
                                  KeyStroke accelerator, ActionListener action) {
        JMenuItem menuItem = new JMenuItem(texto);
        
        try {
            ImageIcon icon = IconManager.getIcon(iconName, IconManager.SMALL);
            if (icon != null) {
                menuItem.setIcon(icon);
            }
        } catch (Exception e) {
            logger.warn("No se pudo cargar el icono: {}", iconName);
        }
        
        if (accelerator != null) {
            menuItem.setAccelerator(accelerator);
        }
        
        menuItem.addActionListener(action);
        return menuItem;
    }

    /**
     * Verifica si el usuario tiene acceso al módulo de almacén
     */
    private boolean tieneAccesoAlmacen() {
        return tieneAcceso(Pantalla.RECEPCION) || 
               tieneAcceso(Pantalla.PRODUCTOS);
    }

    /**
     * Navega a una pantalla específica
     */
    public void navegarAPantalla(Pantalla pantalla) {
        try {
            if (!tieneAcceso(pantalla)) {
                throw new SecurityException("No tiene acceso a esta pantalla");
            }

            contentPanel.removeAll();
            actualizarTitulo(pantalla);
            
            JPanel nuevaVista = crearVista(pantalla);
            if (nuevaVista != null) {
                contentPanel.add(nuevaVista, BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
                actualizarStatusBar();
            }
            
        } catch (Exception e) {
            logger.error("Error al navegar a pantalla: {}", pantalla, e);
            mostrarError("Error al cargar la pantalla: " + e.getMessage());
        }
    }

    /**
     * Crea la vista correspondiente a una pantalla
     */
    private JPanel crearVista(Pantalla pantalla) throws Exception {
    switch (pantalla) {
        case USUARIOS:
            return new UsuariosView(userController);
            
        case ROLES:
            return new RolesView(rolController);
            
        case PERMISOS:
            return new PermisosPantallaView(
                permisoPantallaController,
                permisosPantallaService,
                usuarioActual.getRol().getNombre()
            );
            
        case RECEPCION:
            // Inicializar DAOs necesarios
            ProveedorDao proveedorDao = new ProveedorDao();
            
            // Inicializar Servicios adicionales
            IProveedorService proveedorService = new ProveedorService(proveedorDao);
            RecepcionReporteService reporteService = new RecepcionReporteService(recepcionService);
            
            // Crear controlador con todos los servicios necesarios
            RecepcionMercanciaController recepcionController = new RecepcionMercanciaController(
                recepcionService,
                productoService,
                proveedorService,
                userController,
                reporteService
            );
            
            return new RecepcionMercanciaView(recepcionController);
            
        case PRODUCTOS:
            ProductoController productoController = new ProductoController(productoService);
            return new ProductosView(productoController);
            
        default:
            logger.warn("Pantalla no implementada: {}", pantalla);
            return new JPanel();
    }
}
    
     /**
     * Configura los eventos globales
     */
    private void configurarEventos() {
        // Timer para actualizar la barra de estado cada minuto
        new Timer(60000, e -> actualizarStatusBar()).start();
        
        // Configurar acciones globales
        configurarAccionesTeclado();
    }

    /**
     * Configura acciones globales de teclado
     */
    private void configurarAccionesTeclado() {
        JRootPane rootPane = getRootPane();
        
        // ESC para mostrar diálogo de salida
        rootPane.registerKeyboardAction(
            e -> confirmarSalida(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // F5 para refrescar la vista actual
        rootPane.registerKeyboardAction(
            e -> refrescarVistaActual(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    /**
     * Muestra el panel de bienvenida
     */
    private void mostrarPanelBienvenida() {
        JPanel bienvenidaPanel = new JPanel(new BorderLayout(10, 10));
        bienvenidaPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Título de bienvenida
        JLabel lblBienvenida = new JLabel("¡Bienvenido al Sistema RINTISA!");
        lblBienvenida.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        lblBienvenida.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Panel de información
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Información del usuario
        infoPanel.add(new JLabel("Usuario: " + usuarioActual.getNombre() + 
            " " + usuarioActual.getApellido()), gbc);
        
        gbc.gridy++;
        infoPanel.add(new JLabel("Rol: " + usuarioActual.getRol().getNombre()), gbc);
        
        gbc.gridy++;
        infoPanel.add(new JLabel("Último acceso: " + 
            (usuarioActual.getUltimoAcceso() != null ? 
                usuarioActual.getUltimoAcceso().toString() : "Primer acceso")), gbc);
        
        // Logo de la empresa (si está disponible)
        try {
            ImageIcon logoIcon = IconManager.getIcon("logo", IconManager.LARGE);
            if (logoIcon != null) {
                JLabel lblLogo = new JLabel(logoIcon);
                lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
                bienvenidaPanel.add(lblLogo, BorderLayout.NORTH);
            }
        } catch (Exception e) {
            logger.warn("No se pudo cargar el logo", e);
        }
        
        bienvenidaPanel.add(lblBienvenida, BorderLayout.CENTER);
        bienvenidaPanel.add(infoPanel, BorderLayout.SOUTH);
        
        contentPanel.removeAll();
        contentPanel.add(bienvenidaPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
        
        actualizarTitulo(null);
        actualizarStatusBar();
    }

    /**
     * Actualiza el título de la ventana
     */
    private void actualizarTitulo(Pantalla pantalla) {
        StringBuilder titulo = new StringBuilder("Sistema RINTISA");
        
        if (pantalla != null) {
            titulo.append(" - ").append(pantalla.getNombre());
        }
        
        if (usuarioActual != null) {
            titulo.append(" - ").append(usuarioActual.getNombre())
                  .append(" ").append(usuarioActual.getApellido());
        }
        
        setTitle(titulo.toString());
    }

    /**
     * Refresca la vista actual
     */
    private void refrescarVistaActual() {
        Component component = contentPanel.getComponent(0);
        if (component instanceof Refreshable) {
            ((Refreshable) component).refresh();
        }
    }

    /**
     * Muestra el perfil del usuario
     */
    private void mostrarPerfil() {
        try {
            JDialog dialog = new JDialog(this, "Mi Perfil", true);
            dialog.setLayout(new BorderLayout());
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Datos del usuario
            panel.add(new JLabel("Usuario: " + usuarioActual.getUsername()), gbc);
            gbc.gridy++;
            panel.add(new JLabel("Nombre: " + usuarioActual.getNombre()), gbc);
            gbc.gridy++;
            panel.add(new JLabel("Apellido: " + usuarioActual.getApellido()), gbc);
            gbc.gridy++;
            panel.add(new JLabel("Rol: " + usuarioActual.getRol().getNombre()), gbc);
            gbc.gridy++;
            panel.add(new JLabel("Último acceso: " + usuarioActual.getUltimoAcceso()), gbc);
            
            // Botón cerrar
            JButton btnCerrar = new JButton("Cerrar");
            btnCerrar.addActionListener(e -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(btnCerrar);
            
            dialog.add(panel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            
        } catch (Exception e) {
            logger.error("Error al mostrar perfil", e);
            mostrarError("Error al mostrar perfil: " + e.getMessage());
        }
    }

    /**
     * Muestra el diálogo para cambiar contraseña
     */
    private void mostrarDialogoCambiarPassword() {
        try {
            // Campos de contraseña
            JPasswordField oldPass = new JPasswordField(20);
            JPasswordField newPass = new JPasswordField(20);
            JPasswordField confirmPass = new JPasswordField(20);
            
            Object[] message = {
                "Contraseña actual:", oldPass,
                "Nueva contraseña:", newPass,
                "Confirmar contraseña:", confirmPass
            };
            
            int option = JOptionPane.showConfirmDialog(
                this, 
                message, 
                "Cambiar Contraseña",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (option == JOptionPane.OK_OPTION) {
                String oldPassStr = new String(oldPass.getPassword());
                String newPassStr = new String(newPass.getPassword());
                String confirmPassStr = new String(confirmPass.getPassword());
                
                // Validar contraseñas
                if (!newPassStr.equals(confirmPassStr)) {
                    throw new IllegalArgumentException("Las nuevas contraseñas no coinciden");
                }
                
                // Cambiar contraseña
                userController.cambiarPassword(usuarioActual.getId(), oldPassStr, newPassStr);
                
                mostrarMensaje("Contraseña cambiada exitosamente");
            }
            
        } catch (Exception e) {
            logger.error("Error al cambiar contraseña", e);
            mostrarError("Error al cambiar contraseña: " + e.getMessage());
        }
    }

    /**
     * Cierra la sesión actual
     */
    private void cerrarSesion() {
        try {
            int option = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea cerrar la sesión?",
                "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (option == JOptionPane.YES_OPTION) {
                userController.logout();
                dispose();
                
                // Mostrar ventana de login
                LoginView.mostrar(userController);
            }
            
        } catch (Exception e) {
            logger.error("Error al cerrar sesión", e);
            mostrarError("Error al cerrar sesión: " + e.getMessage());
        }
    }

    /**
     * Muestra el diálogo Acerca de
     */
    private void mostrarAcercaDe() {
        JDialog dialog = new JDialog(this, "Acerca de", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Logo
        try {
            ImageIcon logo = IconManager.getIcon("logo", IconManager.LARGE);
            if (logo != null) {
                panel.add(new JLabel(logo), gbc);
                gbc.gridy++;
            }
        } catch (Exception e) {
            logger.warn("No se pudo cargar el logo", e);
        }
        
        // Información
        JLabel lblTitulo = new JLabel("Sistema RINTISA");
        lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        panel.add(lblTitulo, gbc);
        
        gbc.gridy++;
        panel.add(new JLabel("Versión 1.0"), gbc);
        
        gbc.gridy++;
        panel.add(new JLabel("© 2024 Grupo08@UTP"), gbc);
        
        gbc.gridy++;
        panel.add(new JLabel("Todos los derechos reservados"), gbc);
        
        // Botón cerrar
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnCerrar, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Muestra un mensaje informativo
     */
    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Información",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Interfaz para componentes actualizables
     */
    public interface Refreshable {
        void refresh();
    }
      
}
