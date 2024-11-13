
package com.rintisa.view;

import com.rintisa.model.RecepcionMercancia;
import com.rintisa.controller.RecepcionMercanciaController;
import com.rintisa.exception.ValidationException;
import com.rintisa.model.DetalleRecepcion;
import com.rintisa.model.Pantalla;
import com.rintisa.model.Producto;
import com.rintisa.model.Proveedor;
import com.rintisa.model.enums.EstadoRecepcion;
import com.rintisa.model.Usuario;
import com.rintisa.service.interfaces.IPermisosPantallaService;
import com.rintisa.service.interfaces.IProductoService;
import com.rintisa.service.interfaces.IProveedorService;
import com.rintisa.util.ModernUIUtils;
import com.rintisa.util.SwingUtils;
import com.rintisa.view.base.VistaBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import javax.swing.event.TableModelEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import com.toedter.calendar.JDateChooser; 
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class RecepcionMercanciaView extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RecepcionMercanciaView.class);

    private final RecepcionMercanciaController controller;

    // Componentes de la tabla
    private RecepcionTableModel modeloTabla;
    
    // Componentes principales
    private JSplitPane splitPane;
    private JPanel leftPanel;
    private JPanel rightPanel;
    
    private JScrollPane scrollPane;

    // Componentes de la tabla
    private JTable tablaRecepciones;
    //private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    
    // Componentes de filtro
    private JPanel filterPanel;
    private JDateChooser fechaInicio;
    private JDateChooser fechaFin;
    private JComboBox<EstadoRecepcion> cmbEstado;
    private JTextField txtNumeroRecepcion;
    private JTextField txtOrdenCompra;
    private JButton btnFiltrar;
    private JButton btnLimpiarFiltros;
    
     // Agregar botones de acción
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnVerificar;
    private JButton btnAceptar;
    private JButton btnRechazar;
    private JButton btnEliminar;

    private JButton btnReporte;    
    
    public RecepcionMercanciaView(RecepcionMercanciaController controller) {
        
        this.controller = controller;
        
        // Verificar permisos antes de inicializar
        if (!controller.tienePermisoAlmacen()) {
            logger.warn("Intento de acceso no autorizado a RecepcionMercanciaView");
            mostrarErrorPermiso();
            return;
        }

       createComponents();     // Crear componentes
       setupComponents();      // Configurar componentes
       layoutComponents();     // Organizar componentes
       setupEventListeners(); // Configurar eventos
       loadInitialData();     // Cargar datos iniciales
    }
       
   // Clase del modelo de tabla
    private class RecepcionTableModel extends DefaultTableModel {
         private final String[] columnNames = {
        "ID", "Número", "Fecha", "Proveedor", "Orden Compra", "Estado"
    };
    
    private final Class<?>[] columnTypes = {
        Long.class, String.class, LocalDateTime.class, 
        String.class, String.class, EstadoRecepcion.class
    };

    private List<RecepcionMercancia> recepciones;

    public RecepcionTableModel() {
        super();
        this.recepciones = new ArrayList<>();
        setColumnIdentifiers(columnNames);
    }

    @Override
    public int getRowCount() {
        return recepciones != null ? recepciones.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

     

    
    @Override
    public Object getValueAt(int row, int column) {
        if (row >= 0 && row < recepciones.size()) {
            RecepcionMercancia recepcion = recepciones.get(row);
            switch (column) {
                case 0: return recepcion.getId();
                case 1: return recepcion.getNumeroRecepcion();
                case 2: return recepcion.getFecha();
                //case 3: return controller.obtenerNombreProveedor(recepcion.getId());
                case 3: return String.valueOf(recepcion.getProveedor());

                case 4: return recepcion.getNumeroOrdenCompra();
                case 5: return recepcion.getEstado();
                default: return null;
            }
        }
        return null;
    }

    public RecepcionMercancia getRecepcionAt(int row) {
        if (row >= 0 && row < recepciones.size()) {
            return recepciones.get(row);
        }
        return null;
    }

    public void setRecepciones(List<RecepcionMercancia> recepciones) {
        this.recepciones = new ArrayList<>(recepciones != null ? recepciones : new ArrayList<>());
        fireTableDataChanged();
    }

    public void addRecepcion(RecepcionMercancia recepcion) {
        if (recepcion != null) {
            recepciones.add(recepcion);
            fireTableRowsInserted(recepciones.size() - 1, recepciones.size() - 1);
        }
    }

    public void updateRecepcion(int row, RecepcionMercancia recepcion) {
        if (row >= 0 && row < recepciones.size() && recepcion != null) {
            recepciones.set(row, recepcion);
            fireTableRowsUpdated(row, row);
        }
    }

    public void removeRecepcion(int row) {
        if (row >= 0 && row < recepciones.size()) {
            recepciones.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    public void clear() {
        int oldSize = recepciones.size();
        recepciones.clear();
        if (oldSize > 0) {
            fireTableRowsDeleted(0, oldSize - 1);
        }
            }
    }

    private void createComponents() {
        // Crear el modelo de tabla
        modeloTabla = new RecepcionTableModel();
        tablaRecepciones = new JTable(modeloTabla);
        scrollPane = new JScrollPane(tablaRecepciones);
        
        // Crear componentes de filtro
        fechaInicio = new JDateChooser();
        fechaFin = new JDateChooser();
        cmbEstado = new JComboBox<>(EstadoRecepcion.values());
        txtNumeroRecepcion = new JTextField(10);
        txtOrdenCompra = new JTextField(10);
        //btnFiltrar = new JButton("Buscar");
        btnFiltrar = ModernUIUtils.createPrimaryButton("Buscar", "search");
        //btnLimpiarFiltros = new JButton("Limpiar");
        btnLimpiarFiltros = ModernUIUtils.createPrimaryButton("Limpiar", "limpiar");
        
          // Crear botones de acción
       // btnNuevo = new JButton("Nuevo");
         btnNuevo = ModernUIUtils.createPrimaryButton("Nuevo", "new");
        //btnEditar = new JButton("Editar");
        btnEditar = ModernUIUtils.createPrimaryButton("Editar", "edit");
        //btnVerificar = new JButton("Verificar");
        btnVerificar = ModernUIUtils.createPrimaryButton("Verficar", "verificar");
        //btnAceptar = new JButton("Aceptar");
        btnAceptar = ModernUIUtils.createPrimaryButton("Aceptar", "check");
        //btnRechazar = new JButton("Rechazar");
        btnRechazar = ModernUIUtils.createPrimaryButton("Rechazar", "rechazar");
        //btnEliminar = new JButton("Eliminar");
        btnEliminar = ModernUIUtils.createPrimaryButton("Eliminar", "delete");
        
        //btnReporte = new JButton("Reporte General");
        btnReporte = ModernUIUtils.createPrimaryButton("Reporte", "report");
        btnReporte.setToolTipText("Generar reporte general de recepciones");

        // Crear la tabla
        tablaRecepciones = new JTable(modeloTabla);
        ModernUIUtils.setupModernTable(tablaRecepciones);
        
        // Crear el scroll pane
        scrollPane = new JScrollPane(tablaRecepciones);
         
    }

    private void setupComponents() {
        // Configurar el panel principal
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Configurar la tabla
        tablaRecepciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRecepciones.setAutoCreateRowSorter(true);
        tablaRecepciones.setRowHeight(25);
        
        // Configurar el scroll pane
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createLineBorder(Color.GRAY)
        ));
        // Configurar fechas
        fechaInicio.setDateFormatString("dd/MM/yyyy");
        fechaFin.setDateFormatString("dd/MM/yyyy");

        // Configurar combo de estado
        cmbEstado.insertItemAt(null, 0);
        cmbEstado.setSelectedIndex(0);

        // Configurar botones
        setupButtons();

        // Configurar formatos de columnas
        setupTableColumns();
    }
    
    private void setupButtons() {
        // Configurar iconos y tooltips
        btnNuevo.setToolTipText("Crear nueva recepción");
        btnEditar.setToolTipText("Editar recepción seleccionada");
        btnVerificar.setToolTipText("Verificar recepción seleccionada");
        btnAceptar.setToolTipText("Aceptar recepción verificada");
        btnRechazar.setToolTipText("Rechazar recepción");
        btnEliminar.setToolTipText("Eliminar recepción");

        // Deshabilitar botones que requieren selección
        btnEditar.setEnabled(false);
        btnVerificar.setEnabled(false);
        btnAceptar.setEnabled(false);
        btnRechazar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    private void setupTableColumns() {
        TableColumnModel columnModel = tablaRecepciones.getColumnModel();

        // Configurar renderizadores
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            switch (i) {
                case 2: // Fecha
                    column.setCellRenderer(new DefaultTableCellRenderer() {
                        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        {
                            setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                boolean isSelected, boolean hasFocus, int row, int column) {
                            if (value instanceof LocalDateTime) {
                                value = ((LocalDateTime) value).format(formatter);
                            }
                            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        }
                    });
                    break;
                case 5: // Estado
                    column.setCellRenderer(new DefaultTableCellRenderer() {
                        {
                            setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                boolean isSelected, boolean hasFocus, int row, int column) {
                            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            if (value instanceof EstadoRecepcion) {
                                EstadoRecepcion estado = (EstadoRecepcion) value;
                                setText(estado.getDescripcion());
                                if (!isSelected) {
                                    setBackground(getEstadoColor(estado));
                                }
                            }
                            return c;
                        }
                    });
                    break;
                default:
                    column.setCellRenderer(new DefaultTableCellRenderer() {
                        {
                            setHorizontalAlignment(SwingConstants.CENTER);
                        }
                    });
                    break;
            }
        }

        // Configurar anchos de columnas
        int[] columnWidths = {60, 120, 140, 200, 120, 100};
        for (int i = 0; i < columnWidths.length; i++) {
            columnModel.getColumn(i).setPreferredWidth(columnWidths[i]);
        }
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de filtros
        add(createFilterPanel(), BorderLayout.NORTH);
        
        // Panel central con la tabla
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtros de búsqueda"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Primera fila
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Fecha desde:"), gbc);

        gbc.gridx = 1;
        filterPanel.add(fechaInicio, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("hasta:"), gbc);

        gbc.gridx = 3;
        filterPanel.add(fechaFin, gbc);

        // Segunda fila
        gbc.gridx = 0; gbc.gridy = 1;
        filterPanel.add(new JLabel("Estado:"), gbc);

        gbc.gridx = 1;
        filterPanel.add(cmbEstado, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Nº Recepción:"), gbc);

        gbc.gridx = 3;
        filterPanel.add(txtNumeroRecepcion, gbc);

        // Tercera fila
        gbc.gridx = 0; gbc.gridy = 2;
        filterPanel.add(new JLabel("Orden Compra:"), gbc);

        gbc.gridx = 1;
        filterPanel.add(txtOrdenCompra, gbc);

        // Botones de filtro
        JPanel filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterButtonPanel.add(btnLimpiarFiltros);
        filterButtonPanel.add(btnFiltrar);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 4;
        filterPanel.add(filterButtonPanel, gbc);

        return filterPanel;
    }
     
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Panel izquierdo para botones de mantenimiento
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtons.add(btnNuevo);
        leftButtons.add(btnEditar);
        leftButtons.add(btnEliminar);
        leftButtons.add(btnReporte);

        // Panel derecho para botones de proceso
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtons.add(btnVerificar);
        rightButtons.add(btnAceptar);
        rightButtons.add(btnRechazar);

        // Panel que contiene ambos grupos
        JPanel mainButtonPanel = new JPanel(new GridLayout(1, 2));
        mainButtonPanel.add(leftButtons);
        mainButtonPanel.add(rightButtons);

        buttonPanel.add(mainButtonPanel);
        return buttonPanel;
    }
    
    private void setupEventListeners() {
        // Configurar el evento de selección de la tabla
    tablaRecepciones.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            int selectedRow = tablaRecepciones.getSelectedRow();
            if (selectedRow >= 0) {
                try {
                    // Convertir el índice de la fila seleccionada al modelo
                    int modelRow = tablaRecepciones.convertRowIndexToModel(selectedRow);
                    RecepcionMercancia recepcion = ((RecepcionTableModel)tablaRecepciones.getModel()).getRecepcionAt(modelRow);
                    updateButtonStates(recepcion);
                } catch (Exception ex) {
                    logger.error("Error al manejar selección de fila", ex);
                }
            } else {
                // No hay fila seleccionada, deshabilitar botones
                updateButtonStates(null);
            }
        }
    });
        
         // Eventos de filtro
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        btnLimpiarFiltros.addActionListener(e -> limpiarFiltros());

        // Eventos de botones de acción
        btnNuevo.addActionListener(e -> crearNuevaRecepcion());
        btnEditar.addActionListener(e -> editarRecepcion());
        btnVerificar.addActionListener(e -> verificarRecepcion());
        btnAceptar.addActionListener(e -> aceptarRecepcion());
        btnRechazar.addActionListener(e -> rechazarRecepcion());
        btnEliminar.addActionListener(e -> eliminarRecepcion());
        btnReporte.addActionListener(e -> mostrarDialogoReporte());
        
    }
        
    private void updateButtonStates(RecepcionMercancia recepcion) {
    if (recepcion != null) {
        EstadoRecepcion estado = recepcion.getEstado();
        btnEditar.setEnabled(estado == EstadoRecepcion.PENDIENTE);
        btnVerificar.setEnabled(estado == EstadoRecepcion.PENDIENTE);
        btnAceptar.setEnabled(estado == EstadoRecepcion.APROBADA);
        btnRechazar.setEnabled(estado != EstadoRecepcion.APROBADA && 
                             estado != EstadoRecepcion.RECHAZADA);
        btnEliminar.setEnabled(estado == EstadoRecepcion.PENDIENTE);
    } else {
        // No hay recepción seleccionada, deshabilitar todos los botones
        btnEditar.setEnabled(false);
        btnVerificar.setEnabled(false);
        btnAceptar.setEnabled(false);
        btnRechazar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }
}

    private void handleRowSelection(int selectedRow) {
        try {
            int modelRow = tablaRecepciones.convertRowIndexToModel(selectedRow);
            // Aquí puedes manejar la selección de la fila
            logger.debug("Fila seleccionada: {}", modelRow);
        } catch (Exception e) {
            logger.error("Error al manejar selección de fila", e);
        }
    }

    private void loadInitialData() {
        try {
            java.util.List<RecepcionMercancia> recepciones = controller.listarRecepciones(null, null, null);
            modeloTabla.setRecepciones(recepciones);
        } catch (Exception e) {
            logger.error("Error al cargar datos iniciales", e);
            JOptionPane.showMessageDialog(this,
                "Error al cargar los datos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private Color getEstadoColor(EstadoRecepcion estado) {
        switch (estado) {
            case PENDIENTE: return new Color(255, 255, 224);
            case EN_PROCESO: return new Color(240, 248, 255);
            case APROBADA: return new Color(240, 255, 240);
            case RECHAZADA: return new Color(255, 240, 240);
            default: return Color.WHITE;
        }
    }
    
    private void updateButtonStates() {
        int selectedRow = tablaRecepciones.getSelectedRow();
        boolean haySeleccion = selectedRow >= 0;
        
        if (haySeleccion) {
            int modelRow = tablaRecepciones.convertRowIndexToModel(selectedRow);
            RecepcionMercancia recepcion = modeloTabla.getRecepcionAt(modelRow);
            EstadoRecepcion estado = recepcion.getEstado();

            btnEditar.setEnabled(estado == EstadoRecepcion.PENDIENTE);
            btnVerificar.setEnabled(estado == EstadoRecepcion.PENDIENTE);
            btnAceptar.setEnabled(estado == EstadoRecepcion.EN_PROCESO);
            btnRechazar.setEnabled(estado != EstadoRecepcion.APROBADA && 
                                 estado != EstadoRecepcion.RECHAZADA);
            btnEliminar.setEnabled(estado == EstadoRecepcion.PENDIENTE);
        } else {
            btnEditar.setEnabled(false);
            btnVerificar.setEnabled(false);
            btnAceptar.setEnabled(false);
            btnRechazar.setEnabled(false);
            btnEliminar.setEnabled(false);
        }
    }
    
    private void mostrarErrorPermiso() {
        removeAll();
        setLayout(new BorderLayout());
        
        JPanel errorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel mensajeLabel = new JLabel("No tiene permisos para acceder a esta función");
        mensajeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mensajeLabel.setForeground(Color.RED);
        errorPanel.add(mensajeLabel, gbc);

        add(errorPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    
    // Implementar los métodos de acción
   private void crearNuevaRecepcion() {
    try {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Nueva Recepción", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de información básica
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Orden de Compra
        infoPanel.add(new JLabel("Orden de Compra:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtOrdenCompra = new JTextField(20);
        infoPanel.add(txtOrdenCompra, gbc);

        // Proveedor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        infoPanel.add(new JLabel("Proveedor:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        DefaultComboBoxModel<ProveedorComboItem> proveedorModel = new DefaultComboBoxModel<>();
        JComboBox<ProveedorComboItem> cmbProveedor = new JComboBox<>(proveedorModel);

        // Cargar proveedores
        List<Proveedor> proveedores = controller.obtenerProveedores();
        for (Proveedor proveedor : proveedores) {
            proveedorModel.addElement(new ProveedorComboItem(
                proveedor.getId(),
                proveedor.getRazonSocial()
            ));
        }
        infoPanel.add(cmbProveedor, gbc);

        // Panel para la tabla de detalles
        JPanel detallesPanel = new JPanel(new BorderLayout(5, 5));
        detallesPanel.setBorder(BorderFactory.createTitledBorder("Detalles de Recepción"));

        // Modelo para la tabla de detalles
        String[] columnNames = {"Código", "Producto", "Cantidad", "Precio Unit."};
        DefaultTableModel modeloDetalles = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Solo la cantidad es editable
            }
        };

        JTable tablaDetalles = new JTable(modeloDetalles);
        JScrollPane scrollDetalles = new JScrollPane(tablaDetalles);
        detallesPanel.add(scrollDetalles, BorderLayout.CENTER);

        // Panel de botones para detalles
        JPanel botonesDetalles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregarProducto = new JButton("Agregar Producto");
        JButton btnQuitarProducto = new JButton("Quitar Producto");
        botonesDetalles.add(btnAgregarProducto);
        botonesDetalles.add(btnQuitarProducto);
        detallesPanel.add(botonesDetalles, BorderLayout.NORTH);

        // Acción para agregar producto
        btnAgregarProducto.addActionListener(e -> {
            try {
                mostrarDialogoAgregarProducto(modeloDetalles);
            } catch (Exception ex) {
                logger.error("Error al mostrar diálogo de productos", ex);
                JOptionPane.showMessageDialog(dialog,
                    "Error al agregar producto: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Acción para quitar producto
        btnQuitarProducto.addActionListener(e -> {
            int selectedRow = tablaDetalles.getSelectedRow();
            if (selectedRow >= 0) {
                modeloDetalles.removeRow(selectedRow);
            }
        });

        // Observaciones
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        infoPanel.add(new JLabel("Observaciones:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea txtObservaciones = new JTextArea(4, 20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        infoPanel.add(scrollObs, gbc);

        // Agregar paneles al panel principal
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(detallesPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.addActionListener(e -> {
            try {
                // Validar campos requeridos
                if (txtOrdenCompra.getText().trim().isEmpty()) {
                    throw new ValidationException("ordenCompra", "La orden de compra es requerida");
                }

                ProveedorComboItem proveedorSeleccionado = (ProveedorComboItem) cmbProveedor.getSelectedItem();
                if (proveedorSeleccionado == null) {
                    throw new ValidationException("proveedor", "Debe seleccionar un proveedor");
                }

                // Validar que haya al menos un detalle
                if (modeloDetalles.getRowCount() == 0) {
                    throw new ValidationException("detalles", "Debe agregar al menos un producto");
                }

                // Crear la recepción
                RecepcionMercancia recepcion = controller.crearRecepcion(
                    txtOrdenCompra.getText().trim(),
                    proveedorSeleccionado.getId().toString(),
                    txtObservaciones.getText().trim()
                );

                // Agregar los detalles
                for (int i = 0; i < modeloDetalles.getRowCount(); i++) {
                    ProductoComboItem producto = (ProductoComboItem) modeloDetalles.getValueAt(i, 0);
                    int cantidad = ((Number) modeloDetalles.getValueAt(i, 2)).intValue();
                    double precio = ((Number) modeloDetalles.getValueAt(i, 3)).doubleValue();

                    controller.agregarProducto(recepcion.getId(), producto.getId(), cantidad, precio);
                }

                // Actualizar la tabla
                loadInitialData();

                // Cerrar el diálogo
                dialog.dispose();

                JOptionPane.showMessageDialog(this,
                    "Recepción creada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(dialog,
                    ve.getMessage(),
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                logger.error("Error al crear recepción", ex);
                JOptionPane.showMessageDialog(dialog,
                    "Error al crear la recepción: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    } catch (Exception e) {
        logger.error("Error al mostrar diálogo de nueva recepción", e);
        JOptionPane.showMessageDialog(this,
            "Error al crear nueva recepción: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

    // Método auxiliar para mostrar el diálogo de agregar producto
    private void mostrarDialogoAgregarProducto(DefaultTableModel modeloDetalles) {
    JDialog dialogProducto = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
        "Agregar Producto", true);
    dialogProducto.setLayout(new BorderLayout(10, 10));

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // ComboBox de productos
    panel.add(new JLabel("Producto:"), gbc);
    DefaultComboBoxModel<ProductoComboItem> productoModel = new DefaultComboBoxModel<>();
    JComboBox<ProductoComboItem> cmbProducto = new JComboBox<>(productoModel);
    
    // Cargar productos
    List<Producto> productos = controller.obtenerProductos();
    for (Producto producto : productos) {
        productoModel.addElement(new ProductoComboItem(
            producto.getId(),
            producto.getCodigo() + " - " + producto.getNombre(),
            producto.getPrecioUnitario()
        ));
    }
    
    gbc.gridx = 1;
    panel.add(cmbProducto, gbc);

    // Campo de cantidad
    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(new JLabel("Cantidad:"), gbc);
    
    JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
    gbc.gridx = 1;
    panel.add(spnCantidad, gbc);

    // Botones
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnAceptar = new JButton("Aceptar");
    JButton btnCancelar = new JButton("Cancelar");

    btnAceptar.addActionListener(e -> {
        try {
            ProductoComboItem productoSeleccionado = (ProductoComboItem) cmbProducto.getSelectedItem();
            if (productoSeleccionado == null) {
                throw new ValidationException("producto", "Debe seleccionar un producto");
            }

            // Agregar a la tabla
            modeloDetalles.addRow(new Object[]{
                productoSeleccionado,
                productoSeleccionado.getDescripcion(),
                spnCantidad.getValue(),
                productoSeleccionado.getPrecioUnitario()
            });

            dialogProducto.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialogProducto,
                "Error al agregar producto: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    });

    btnCancelar.addActionListener(e -> dialogProducto.dispose());

    buttonPanel.add(btnAceptar);
    buttonPanel.add(btnCancelar);

    dialogProducto.add(panel, BorderLayout.CENTER);
    dialogProducto.add(buttonPanel, BorderLayout.SOUTH);
    dialogProducto.pack();
    dialogProducto.setLocationRelativeTo(this);
    dialogProducto.setVisible(true);
}

    // Clases auxiliares para los ComboBox
    private static class ProductoComboItem {
    private final Long id;
    private final String descripcion;
    private final double precioUnitario;

    public ProductoComboItem(Long id, String descripcion, double precioUnitario) {
        this.id = id;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
    }

    public Long getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public double getPrecioUnitario() { return precioUnitario; }

    @Override
    public String toString() {
        return descripcion;
    }
        }
    

    private void editarRecepcion() {
                int selectedRow = tablaRecepciones.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione una recepción para editar",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Obtener la recepción seleccionada
            int modelRow = tablaRecepciones.convertRowIndexToModel(selectedRow);
            final RecepcionMercancia recepcion = modeloTabla.getRecepcionAt(modelRow);

            // Verificar que la recepción está en estado PENDIENTE
            if (recepcion.getEstado() != EstadoRecepcion.PENDIENTE) {
                JOptionPane.showMessageDialog(this,
                    "Solo se pueden editar recepciones en estado PENDIENTE",
                    "Edición no permitida",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Crear el diálogo de edición
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                      "Editar Recepción", true);
            dialog.setLayout(new BorderLayout(10, 10));

            // Panel principal
            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);

            // Campos no editables - Información
            // Número de Recepción
            addLabelAndField(mainPanel, gbc, "Número:", 
                           recepcion.getNumeroRecepcion(), false);

            // Fecha
            gbc.gridy++;
            addLabelAndField(mainPanel, gbc, "Fecha:", 
                           recepcion.getFecha().format(
                               DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false);

            // Usuario responsable
             gbc.gridy++;
            if (controller.hayUsuarioActivo()) {
            addLabelAndField(mainPanel, gbc, "Responsable:", 
            controller.getNombreUsuarioActual(), false);
            }

            // Campos editables
            // Orden de Compra
            gbc.gridy++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            mainPanel.add(new JLabel("Orden de Compra:"), gbc);
            gbc.gridx = 1;
            JTextField txtOrdenCompra = new JTextField(recepcion.getNumeroOrdenCompra(), 20);
            mainPanel.add(txtOrdenCompra, gbc);

            // Proveedor
            gbc.gridx = 0;
            gbc.gridy++;
            mainPanel.add(new JLabel("Proveedor:"), gbc);
            gbc.gridx = 1;

            // ComboBox de Proveedores
            DefaultComboBoxModel<ProveedorComboItem> proveedorModel = new DefaultComboBoxModel<>();
            JComboBox<ProveedorComboItem> cmbProveedor = new JComboBox<>(proveedorModel);

            // Cargar proveedores
            List<Proveedor> proveedores = controller.obtenerProveedores();
            for (Proveedor prov : proveedores) {
                ProveedorComboItem item = new ProveedorComboItem(
                    prov.getId(),
                    prov.getRazonSocial()
                );
                proveedorModel.addElement(item);
                if (prov.getId().toString().equals(recepcion.getProveedor())) {
                    cmbProveedor.setSelectedItem(item);
                }
            }
            mainPanel.add(cmbProveedor, gbc);

            // Observaciones
            gbc.gridx = 0;
            gbc.gridy++;
            mainPanel.add(new JLabel("Observaciones:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.BOTH;
            JTextArea txtObservaciones = new JTextArea(recepcion.getObservaciones(), 4, 20);
            txtObservaciones.setLineWrap(true);
            txtObservaciones.setWrapStyleWord(true);
            mainPanel.add(new JScrollPane(txtObservaciones), gbc);

            // Panel de botones
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnGuardar = new JButton("Guardar");
            JButton btnCancelar = new JButton("Cancelar");

            btnGuardar.addActionListener(e -> {
        try {
            // Validar campos requeridos
            if (txtOrdenCompra.getText().trim().isEmpty()) {
                throw new ValidationException("Orden de Compra", 
                    "La orden de compra es requerida");
            }

            ProveedorComboItem proveedorSeleccionado = 
                (ProveedorComboItem) cmbProveedor.getSelectedItem();
            if (proveedorSeleccionado == null) {
                throw new ValidationException("Proveedor", 
                    "Debe seleccionar un proveedor");
            }

            // Crear copia de la recepción con los nuevos datos
            RecepcionMercancia recepcionActualizada = new RecepcionMercancia();
            // Copiar datos que no cambian
            recepcionActualizada.setId(recepcion.getId());
            recepcionActualizada.setNumeroRecepcion(recepcion.getNumeroRecepcion());
            recepcionActualizada.setFecha(recepcion.getFecha());
            recepcionActualizada.setEstado(recepcion.getEstado());
            recepcionActualizada.setResponsable(recepcion.getResponsable());
            
            // Actualizar los campos editados
            recepcionActualizada.setNumeroOrdenCompra(txtOrdenCompra.getText().trim());
            Proveedor proveedor = new Proveedor();
            proveedor.setId(proveedorSeleccionado.getId());
            recepcionActualizada.setProveedor(proveedor);
            //recepcionActualizada.setProveedor(proveedorSeleccionado.getId().toString());
            recepcionActualizada.setObservaciones(txtObservaciones.getText().trim());

            // Guardar cambios
            controller.actualizarRecepcion(recepcionActualizada);

            // Actualizar la tabla
            modeloTabla.updateRecepcion(modelRow, recepcionActualizada);
            
            // Cerrar el diálogo
            dialog.dispose();
            
            // Mostrar mensaje de éxito
            JOptionPane.showMessageDialog(this,
                "Recepción actualizada exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(dialog,
                ve.getMessage(),
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            logger.error("Error al actualizar recepción", ex);
            JOptionPane.showMessageDialog(dialog,
                "Error al actualizar la recepción: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
                }
            });

            btnCancelar.addActionListener(e -> dialog.dispose());

            buttonPanel.add(btnGuardar);
            buttonPanel.add(btnCancelar);

            dialog.add(mainPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (Exception e) {
            logger.error("Error al editar recepción", e);
            JOptionPane.showMessageDialog(this,
                "Error al editar la recepción: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, 
                                String labelText, String value, boolean editable) {
        gbc.gridx = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        JTextField field = new JTextField(value, 20);
        field.setEditable(editable);
        field.setBackground(editable ? Color.WHITE : new Color(240, 240, 240));
        panel.add(field, gbc);
    }

    private void verificarRecepcion() {
         int selectedRow = tablaRecepciones.getSelectedRow();
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this,
            "Por favor, seleccione una recepción para verificar",
            "Selección requerida",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        // Obtener la recepción seleccionada
        int modelRow = tablaRecepciones.convertRowIndexToModel(selectedRow);
        final RecepcionMercancia recepcion = modeloTabla.getRecepcionAt(modelRow);

        // Verificar estado
        if (recepcion.getEstado() != EstadoRecepcion.PENDIENTE) {
            JOptionPane.showMessageDialog(this,
                "Solo se pueden verificar recepciones en estado PENDIENTE",
                "Estado inválido",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Crear diálogo
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Verificar Recepción", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de información
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Información de Recepción"));
        
        infoPanel.add(new JLabel("Número:"));
        infoPanel.add(new JLabel(recepcion.getNumeroRecepcion()));
        
        infoPanel.add(new JLabel("Fecha:"));
        infoPanel.add(new JLabel(recepcion.getFechaCreacion()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        
        infoPanel.add(new JLabel("Proveedor:"));
        infoPanel.add(new JLabel(String.valueOf(recepcion.getProveedorId())));
        
        infoPanel.add(new JLabel("Orden de Compra:"));
        infoPanel.add(new JLabel(recepcion.getNumeroOrdenCompra()));

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Configuración de la tabla
        String[] columnNames = {
            "Código", "Producto", "Cantidad Esperada", "Cantidad Recibida", 
            "Diferencia", "Precio Unit.", "Total", "Observaciones"
        };

        // 1. AQUÍ VA EL MODELO DE TABLA PERSONALIZADO
        DefaultTableModel modeloDetalles = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 7; // Solo cantidad recibida y observaciones
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: // Cantidad Esperada
                    case 3: // Cantidad Recibida
                    case 4: // Diferencia
                        return Integer.class;
                    case 5: // Precio Unitario
                    case 6: // Total
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };

        JTable tablaDetalles = new JTable(modeloDetalles);
        tablaDetalles.setRowHeight(25);

        // 2. AQUÍ VA LA CONFIGURACIÓN DEL EDITOR DE CANTIDAD RECIBIDA
        TableColumn cantidadRecibidaColumn = tablaDetalles.getColumnModel().getColumn(3);
        JTextField editorCantidad = new JTextField();
        editorCantidad.setHorizontalAlignment(SwingConstants.RIGHT);
        DefaultCellEditor cantidadEditor = new DefaultCellEditor(editorCantidad) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    if (!value.isEmpty()) {
                        int cantidad = Integer.parseInt(value);
                        if (cantidad < 0) {
                            JOptionPane.showMessageDialog(dialog,
                                "La cantidad recibida no puede ser negativa",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        
                        // Obtener la cantidad esperada de la fila actual
                        int row = tablaDetalles.getSelectedRow();
                        int cantidadEsperada = (Integer) modeloDetalles.getValueAt(row, 2);
                        
                        // Calcular y actualizar la diferencia
                        int diferencia = cantidad - cantidadEsperada;
                        modeloDetalles.setValueAt(diferencia, row, 4);
                        
                        // Calcular y actualizar el total
                        double precioUnitario = Double.parseDouble(modeloDetalles.getValueAt(row, 5).toString()
                            .replace(",", ""));
                        double total = cantidad * precioUnitario;
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        modeloDetalles.setValueAt(df.format(total), row, 6);
                    }
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(dialog,
                        "Por favor ingrese un número válido",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        };
        cantidadRecibidaColumn.setCellEditor(cantidadEditor);

        // 3. AQUÍ VAN LOS RENDERIZADORES
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                if (value instanceof Double) {
                    value = df.format(value);
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };

        // Aplicar renderizadores
        tablaDetalles.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tablaDetalles.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tablaDetalles.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tablaDetalles.getColumnModel().getColumn(5).setCellRenderer(numberRenderer);
        tablaDetalles.getColumnModel().getColumn(6).setCellRenderer(numberRenderer);

        // 4. AQUÍ VA LA CONFIGURACIÓN DE ANCHOS DE COLUMNA
        int[] anchos = {80, 200, 100, 100, 100, 100, 100, 200};
        for (int i = 0; i < anchos.length; i++) {
            tablaDetalles.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        // 5. AQUÍ VA LA CARGA DE DATOS
        // Obtener los detalles
        List<DetalleRecepcion> detalles = controller.obtenerDetalles(recepcion.getId());
        
        if (detalles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "La recepción no tiene detalles para verificar",
                "Sin detalles",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Cargar los detalles en la tabla usando el método cargarDetallesEnTabla
        cargarDetallesEnTabla(modeloDetalles, detalles);

        // Agregar la tabla al panel
        JScrollPane scrollPane = new JScrollPane(tablaDetalles);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel de observaciones
        JPanel observacionesPanel = new JPanel(new BorderLayout(5, 5));
        observacionesPanel.setBorder(BorderFactory.createTitledBorder("Observaciones"));
        JTextArea txtObservaciones = new JTextArea(4, 40);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        observacionesPanel.add(new JScrollPane(txtObservaciones), BorderLayout.CENTER);
        mainPanel.add(observacionesPanel, BorderLayout.SOUTH);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnVerificar = new JButton("Verificar");
        JButton btnCancelar = new JButton("Cancelar");

        btnVerificar.addActionListener(e -> {
            try {
                // Validar que todas las cantidades estén ingresadas
                Map<Long, Integer> cantidadesRecibidas = new HashMap<>();
                for (int i = 0; i < detalles.size(); i++) {
                    Object cantidadRecibida = modeloDetalles.getValueAt(i, 3);
                    if (cantidadRecibida == null) {
                        throw new ValidationException("cantidadRecibida",
                            "Debe ingresar todas las cantidades recibidas");
                    }
                    DetalleRecepcion detalle = detalles.get(i);
                    cantidadesRecibidas.put(detalle.getId(), 
                        Integer.parseInt(cantidadRecibida.toString()));
                }

                // Verificar la recepción
                controller.verificarRecepcion(
                    recepcion.getId(),
                    cantidadesRecibidas,
                    txtObservaciones.getText().trim()
                );

                dialog.dispose();
                loadInitialData();

                JOptionPane.showMessageDialog(this,
                    "Recepción verificada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                logger.error("Error al verificar recepción", ex);
                JOptionPane.showMessageDialog(dialog,
                    "Error al verificar la recepción: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnVerificar);
        buttonPanel.add(btnCancelar);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

    } catch (Exception e) {
        logger.error("Error al mostrar diálogo de verificación", e);
        JOptionPane.showMessageDialog(this,
            "Error al mostrar diálogo de verificación: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

    // Método auxiliar para cargar los detalles en la tabla
    private void cargarDetallesEnTabla(DefaultTableModel modeloDetalles, List<DetalleRecepcion> detalles) {
    try {
        // Limpiar tabla
        modeloDetalles.setRowCount(0);
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        // Agregar cada detalle
        for (DetalleRecepcion detalle : detalles) {
            // Verificar que el producto no sea null
            if (detalle.getProducto() == null) {
                logger.warn("Detalle {} tiene producto null", detalle.getId());
                continue;
            }
            
            // Obtener cantidades
            int cantidadEsperada = detalle.getCantidadEsperada();
            Integer cantidadRecibida = detalle.getCantidadRecibida();
            
            // Calcular diferencia si hay cantidad recibida
            Integer diferencia = null;
            if (cantidadRecibida != null) {
                diferencia = cantidadRecibida - cantidadEsperada;
            }
            
            // Calcular total
            double precioUnitario = detalle.getPrecioUnitario();
            double total;
            if (cantidadRecibida != null) {
                total = cantidadRecibida * precioUnitario;
            } else {
                total = cantidadEsperada * precioUnitario;
            }
            
            // Preparar datos de la fila
            Object[] rowData = new Object[]{
                detalle.getProducto().getCodigo(),              // Código
                detalle.getProducto().getNombre(),              // Nombre
                cantidadEsperada,                              // Cantidad Esperada
                cantidadRecibida,                              // Cantidad Recibida (puede ser null)
                diferencia,                                     // Diferencia (puede ser null)
                df.format(precioUnitario),                      // Precio Unitario
                df.format(total),                               // Total
                detalle.getObservaciones() != null ? 
                    detalle.getObservaciones() : ""            // Observaciones
            };
            
            // Agregar fila a la tabla
            modeloDetalles.addRow(rowData);
            
            logger.debug("Fila agregada - Producto: {}, Esp: {}, Rec: {}, Dif: {}", 
                detalle.getProducto().getCodigo(),
                cantidadEsperada,
                cantidadRecibida,
                diferencia);
        }
        
        // Verificar si se agregaron filas
        if (modeloDetalles.getRowCount() == 0) {
            logger.warn("No se agregaron filas a la tabla de detalles");
        } else {
            logger.debug("Se agregaron {} filas a la tabla de detalles", modeloDetalles.getRowCount());
        }
        
    } catch (Exception e) {
        logger.error("Error al cargar detalles en la tabla: {}", e.getMessage());
        throw new RuntimeException("Error al cargar detalles en la tabla: " + e.getMessage());
        }    
    }

    private void aceptarRecepcion() {
        try {
        // Obtener la recepción seleccionada
        int selectedRow = tablaRecepciones.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione una recepción para aceptar",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convertir índice de fila al modelo
        int modelRow = tablaRecepciones.convertRowIndexToModel(selectedRow);
        RecepcionMercancia recepcion = modeloTabla.getRecepcionAt(modelRow);

        // Verificar estado
        if (recepcion.getEstado() != EstadoRecepcion.EN_PROCESO) {
            JOptionPane.showMessageDialog(this,
                "Solo se pueden aceptar recepciones en estado VERIFICADO",
                "Estado inválido",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pedir confirmación y observaciones
        JTextArea txtObservaciones = new JTextArea(4, 30);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtObservaciones);

        Object[] message = {
            "¿Está seguro que desea aceptar esta recepción?",
            "Observaciones:",
            scrollPane
        };

        int option = JOptionPane.showConfirmDialog(this,
            message,
            "Confirmar Aceptación",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            // Llamar al controlador para aceptar la recepción
            controller.aceptarRecepcion(recepcion.getId(), txtObservaciones.getText().trim());
            
            // Recargar datos
            loadInitialData();
            
            JOptionPane.showMessageDialog(this,
                "Recepción aceptada exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
        }

    } catch (Exception e) {
        logger.error("Error al aceptar recepción", e);
        JOptionPane.showMessageDialog(this,
            "Error al aceptar la recepción: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechazarRecepcion() {
        try {
        // Obtener la recepción seleccionada
        int selectedRow = tablaRecepciones.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione una recepción para rechazar",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convertir índice de fila al modelo
        int modelRow = tablaRecepciones.convertRowIndexToModel(selectedRow);
        RecepcionMercancia recepcion = modeloTabla.getRecepcionAt(modelRow);

        // Verificar estado
        EstadoRecepcion estado = recepcion.getEstado();
        if (estado == EstadoRecepcion.APROBADA || estado == EstadoRecepcion.RECHAZADA) {
            JOptionPane.showMessageDialog(this,
                "No se puede rechazar una recepción que ya está finalizada",
                "Estado inválido",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pedir motivo del rechazo
        JTextArea txtMotivo = new JTextArea(4, 30);
        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtMotivo);

        Object[] message = {
            "Por favor, indique el motivo del rechazo:",
            scrollPane
        };

        int option = JOptionPane.showConfirmDialog(this,
            message,
            "Motivo de Rechazo",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            if (txtMotivo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Debe ingresar un motivo para el rechazo",
                    "Motivo requerido",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Llamar al controlador para rechazar la recepción
            controller.rechazarRecepcion(recepcion.getId(), txtMotivo.getText().trim());
            
            // Recargar datos
            loadInitialData();
            
            JOptionPane.showMessageDialog(this,
                "Recepción rechazada exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
        }

    } catch (Exception e) {
        logger.error("Error al rechazar recepción", e);
        JOptionPane.showMessageDialog(this,
            "Error al rechazar la recepción: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarRecepcion() {
        int selectedRow = tablaRecepciones.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione una recepción para eliminar",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Obtener la recepción seleccionada
            int modelRow = tablaRecepciones.convertRowIndexToModel(selectedRow);
            RecepcionMercancia recepcion = modeloTabla.getRecepcionAt(modelRow);

            // Confirmar eliminación
            int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar la recepción " + recepcion.getNumeroRecepcion() + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                // Eliminar la recepción
                controller.eliminarRecepcion(recepcion.getId());

                // Eliminar de la tabla
                modeloTabla.removeRecepcion(modelRow);

                // Mostrar mensaje de éxito
                JOptionPane.showMessageDialog(this,
                    "Recepción eliminada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            logger.error("Error al eliminar recepción", e);
            JOptionPane.showMessageDialog(this,
                "Error al eliminar la recepción: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } 
    }

     // Clase auxiliar para los items del ComboBox de proveedores
    private static class ProveedorComboItem {
        private final Long id;
        private final String razonSocial;

        public ProveedorComboItem(Long id, String razonSocial) {
            this.id = id;
            this.razonSocial = razonSocial;
        }

        public Long getId() {
            return id;
        }

        @Override
        public String toString() {
            return razonSocial;
        }
    }
    
    private void aplicarFiltros() {
          try {
            // Obtener valores de los filtros y hacerlos final
            final LocalDateTime fechaDesde;
            final LocalDateTime fechaHasta;
            
            // Convertir fecha inicial
            if (fechaInicio.getDate() != null) {
                fechaDesde = LocalDateTime.of(
                    fechaInicio.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(), 
                    LocalTime.MIN
                );
            } else {
                fechaDesde = null;
            }
            
            // Convertir fecha final
            if (fechaFin.getDate() != null) {
                fechaHasta = LocalDateTime.of(
                    fechaFin.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(), 
                    LocalTime.MAX
                );
            } else {
                fechaHasta = null;
            }

            final EstadoRecepcion estado = (EstadoRecepcion) cmbEstado.getSelectedItem();
            final String numeroRecepcion = txtNumeroRecepcion.getText().trim();
            final String ordenCompra = txtOrdenCompra.getText().trim();

            // Crear el filtro para el TableRowSorter
            RowFilter<RecepcionTableModel, Integer> filtroFinal = new RowFilter<RecepcionTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends RecepcionTableModel, ? extends Integer> entry) {
                    RecepcionTableModel model = entry.getModel();
                    int row = entry.getIdentifier();

                    try {
                        // Obtener valores de la fila
                        LocalDateTime fechaRecepcion = (LocalDateTime) model.getValueAt(row, 2);
                        String numRecep = String.valueOf(model.getValueAt(row, 1));
                        String numOC = String.valueOf(model.getValueAt(row, 4));
                        EstadoRecepcion estadoRec = (EstadoRecepcion) model.getValueAt(row, 5);

                        // Verificar fechas
                        if (fechaDesde != null && fechaRecepcion.isBefore(fechaDesde)) {
                            return false;
                        }
                        if (fechaHasta != null && fechaRecepcion.isAfter(fechaHasta)) {
                            return false;
                        }

                        // Verificar estado
                        if (estado != null && estadoRec != estado) {
                            return false;
                        }

                        // Verificar número de recepción
                        if (!numeroRecepcion.isEmpty() && 
                            !numRecep.toLowerCase().contains(numeroRecepcion.toLowerCase())) {
                            return false;
                        }

                        // Verificar orden de compra
                        if (!ordenCompra.isEmpty() && 
                            !numOC.toLowerCase().contains(ordenCompra.toLowerCase())) {
                            return false;
                        }

                        return true;
                    } catch (Exception e) {
                        logger.error("Error al filtrar fila", e);
                        return false;
                    }
                }
            };

            // Aplicar el filtro
            TableRowSorter<RecepcionTableModel> sorter = 
                (TableRowSorter<RecepcionTableModel>) tablaRecepciones.getRowSorter();
            sorter.setRowFilter(filtroFinal);

            // Mostrar mensaje si no hay resultados
            if (tablaRecepciones.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                    "No se encontraron recepciones con los filtros aplicados",
                    "Sin resultados",
                    JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            logger.error("Error al aplicar filtros", e);
            JOptionPane.showMessageDialog(this,
                "Error al aplicar filtros: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFiltros() {
        try {
            // Limpiar campos de filtro
            fechaInicio.setDate(null);
            fechaFin.setDate(null);
            cmbEstado.setSelectedIndex(0);
            txtNumeroRecepcion.setText("");
            txtOrdenCompra.setText("");

            // Quitar filtros de la tabla
            TableRowSorter<RecepcionTableModel> sorter = 
                (TableRowSorter<RecepcionTableModel>) tablaRecepciones.getRowSorter();
            sorter.setRowFilter(null);

            // Recargar datos
            loadInitialData();

        } catch (Exception e) {
            logger.error("Error al limpiar filtros", e);
            JOptionPane.showMessageDialog(this,
                "Error al limpiar filtros: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setupSearchListeners() {
        // Listener para cambios en el campo de número de recepción
        txtNumeroRecepcion.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
        });

        // Listener para cambios en el campo de orden de compra
        txtOrdenCompra.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
        });

        // Listener para cambios en el combo de estado
        cmbEstado.addActionListener(e -> aplicarFiltros());

        // Listener para cambios en las fechas
        PropertyChangeListener dateListener = evt -> {
            if ("date".equals(evt.getPropertyName())) {
                aplicarFiltros();
            }
        };
        fechaInicio.addPropertyChangeListener(dateListener);
        fechaFin.addPropertyChangeListener(dateListener);
    }
    
    private void mostrarDialogoReporte() {
    // Crear un diálogo para seleccionar el rango de fechas
    JDialog dialogoReporte = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
        "Generar Reporte General", true);
    dialogoReporte.setLayout(new BorderLayout(10, 10));

    // Panel principal
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Selector de fecha inicial
    panel.add(new JLabel("Fecha Inicio:"), gbc);
    JDateChooser fechaInicio = new JDateChooser();
    fechaInicio.setDateFormatString("dd/MM/yyyy");
    gbc.gridx = 1;
    panel.add(fechaInicio, gbc);

    // Selector de fecha final
    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(new JLabel("Fecha Fin:"), gbc);
    JDateChooser fechaFin = new JDateChooser();
    fechaFin.setDateFormatString("dd/MM/yyyy");
    gbc.gridx = 1;
    panel.add(fechaFin, gbc);

    // Panel de botones
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnGenerar = new JButton("Generar");
    JButton btnCancelar = new JButton("Cancelar");

    btnGenerar.addActionListener(e -> {
        try {
            // Validar fechas
            if (fechaInicio.getDate() == null || fechaFin.getDate() == null) {
                JOptionPane.showMessageDialog(dialogoReporte,
                    "Por favor seleccione ambas fechas",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Convertir fechas a LocalDateTime
            LocalDateTime inicio = fechaInicio.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .withHour(0).withMinute(0).withSecond(0);

            LocalDateTime fin = fechaFin.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .withHour(23).withMinute(59).withSecond(59);

            // Generar el reporte
            byte[] reporteBytes = controller.generarReporteGeneral(inicio, fin);

            // Guardar el archivo
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Reporte");
            fileChooser.setSelectedFile(new File("Reporte_Recepciones.xlsx"));
            
            if (fileChooser.showSaveDialog(dialogoReporte) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                    file = new File(file.getParentFile(), file.getName() + ".xlsx");
                }
                
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(reporteBytes);
                    JOptionPane.showMessageDialog(dialogoReporte,
                        "Reporte generado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialogoReporte.dispose();
                }
            }
        } catch (Exception ex) {
            logger.error("Error al generar reporte", ex);
            JOptionPane.showMessageDialog(dialogoReporte,
                "Error al generar reporte: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    });

    btnCancelar.addActionListener(e -> dialogoReporte.dispose());

    buttonPanel.add(btnGenerar);
    buttonPanel.add(btnCancelar);

    dialogoReporte.add(panel, BorderLayout.CENTER);
    dialogoReporte.add(buttonPanel, BorderLayout.SOUTH);

    dialogoReporte.pack();
    dialogoReporte.setLocationRelativeTo(this);
    dialogoReporte.setVisible(true);
    }

    
}