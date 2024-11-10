
package com.rintisa.view;

import com.rintisa.controller.ProductoController;
import com.rintisa.exception.ValidationException;
import com.rintisa.model.Producto;
import com.rintisa.service.impl.ProductoTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductosView extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ProductosView.class);

    private final ProductoController controller;
    
    // Componentes de la tabla
    private JTable tablaProductos;
    private ProductoTableModel modeloTabla;
    
    // Componentes de búsqueda
    private JTextField txtBuscar;
    private JButton btnBuscar;
    
    // Botones de acción
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;

    public ProductosView(ProductoController controller) {
        this.controller = controller;
        createComponents();
        setupComponents();
        layoutComponents();
        setupEventListeners();
        loadInitialData();
    }

    private void createComponents() {
        // Crear componentes de búsqueda
        txtBuscar = new JTextField(20);
        btnBuscar = new JButton("Buscar");
        
        // Crear modelo y tabla
        modeloTabla = new ProductoTableModel();
        tablaProductos = new JTable(modeloTabla);
        
        // Crear botones
        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
    }

    private void setupComponents() {
        // Configurar tabla
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setAutoCreateRowSorter(true);
        
        // Configurar renderers para la tabla
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        // Aplicar renderers a columnas específicas
        tablaProductos.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Precio
        tablaProductos.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Stock
        
        // Configurar botones
        btnNuevo.setIcon(new ImageIcon(getClass().getResource("/icons/user.png")));
        btnEditar.setIcon(new ImageIcon(getClass().getResource("/icons/key.png")));
        btnEliminar.setIcon(new ImageIcon(getClass().getResource("/icons/cancel.png")));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar:"));
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(btnNuevo);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);

        // Panel superior que combina búsqueda y botones
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Agregar componentes al panel principal
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        // Listener para el botón buscar
        btnBuscar.addActionListener(e -> buscarProductos());
        
        // Listener para la tecla Enter en el campo de búsqueda
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarProductos();
                }
            }
        });

        // Listeners para los botones de acción
        btnNuevo.addActionListener(e -> mostrarDialogoNuevoProducto());
        btnEditar.addActionListener(e -> mostrarDialogoEditarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());

        // Listener para la selección en la tabla
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            boolean haySeleccion = tablaProductos.getSelectedRow() != -1;
            btnEditar.setEnabled(haySeleccion);
            btnEliminar.setEnabled(haySeleccion);
        });
    }

    private void loadInitialData() {
        try {
            List<Producto> productos = controller.listarProductos();
            modeloTabla.setProductos(productos);
        } catch (Exception e) {
            logger.error("Error al cargar datos iniciales", e);
            JOptionPane.showMessageDialog(this,
                "Error al cargar los productos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarProductos() {
        try {
            String criterio = txtBuscar.getText().trim();
            List<Producto> productos = controller.buscarProductos(criterio);
            modeloTabla.setProductos(productos);
        } catch (Exception e) {
            logger.error("Error al buscar productos", e);
            JOptionPane.showMessageDialog(this,
                "Error al buscar productos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDialogoNuevoProducto() {
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
        "Nuevo Producto", true);
    dialog.setLayout(new BorderLayout(10, 10));

    // Panel principal con GridBagLayout
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Campos del formulario
    JTextField txtCodigo = new JTextField(20);
    JTextField txtNombre = new JTextField(20);
    JTextArea txtDescripcion = new JTextArea(3, 20);
    JTextField txtUnidadMedida = new JTextField(20);
    JTextField txtPrecioUnitario = new JTextField(20);
    JSpinner spnStockMinimo = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
    JSpinner spnStockActual = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));

    // Agregar componentes con sus etiquetas
    agregarCampo(mainPanel, gbc, "Código (*)", txtCodigo);
    agregarCampo(mainPanel, gbc, "Nombre (*)", txtNombre);
    
    // Descripción con scroll
    gbc.gridy++;
    mainPanel.add(new JLabel("Descripción:"), gbc);
    gbc.gridx = 1;
    txtDescripcion.setLineWrap(true);
    txtDescripcion.setWrapStyleWord(true);
    JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
    mainPanel.add(scrollDescripcion, gbc);
    
    gbc.gridx = 0;
    agregarCampo(mainPanel, gbc, "Unidad Medida (*)", txtUnidadMedida);
    agregarCampo(mainPanel, gbc, "Precio Unitario (*)", txtPrecioUnitario);
    agregarCampo(mainPanel, gbc, "Stock Mínimo (*)", spnStockMinimo);
    agregarCampo(mainPanel, gbc, "Stock Actual (*)", spnStockActual);

    // Panel de botones
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnGuardar = new JButton("Guardar");
    JButton btnCancelar = new JButton("Cancelar");

    btnGuardar.addActionListener(e -> {
        try {
            // Validaciones básicas
            if (txtCodigo.getText().trim().isEmpty() ||
                txtNombre.getText().trim().isEmpty() ||
                txtUnidadMedida.getText().trim().isEmpty() ||
                txtPrecioUnitario.getText().trim().isEmpty()) {
                throw new ValidationException("campos", "Los campos marcados con (*) son obligatorios");
            }

            // Crear nuevo producto
            Producto producto = new Producto();
            producto.setCodigo(txtCodigo.getText().trim());
            producto.setNombre(txtNombre.getText().trim());
            producto.setDescripcion(txtDescripcion.getText().trim());
            producto.setUnidadMedida(txtUnidadMedida.getText().trim());
            try {
                producto.setPrecioUnitario(Double.parseDouble(txtPrecioUnitario.getText().trim()));
            } catch (NumberFormatException ex) {
                throw new ValidationException("precioUnitario", "El precio debe ser un número válido");
            }
            producto.setStockMinimo((Integer) spnStockMinimo.getValue());
            producto.setStockActual((Integer) spnStockActual.getValue());
            producto.setActivo(true);

            // Guardar producto
            controller.crearProducto(producto);

            // Actualizar tabla
            loadInitialData();

            // Cerrar diálogo
            dialog.dispose();

            JOptionPane.showMessageDialog(this,
                "Producto creado exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            logger.error("Error al crear producto", ex);
            JOptionPane.showMessageDialog(dialog,
                "Error al crear producto: " + ex.getMessage(),
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
}

    private void mostrarDialogoEditarProducto() {
    int selectedRow = tablaProductos.getSelectedRow();
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this,
            "Por favor, seleccione un producto para editar",
            "Selección requerida",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    int modelRow = tablaProductos.convertRowIndexToModel(selectedRow);
    Producto productoOriginal = modeloTabla.getProductoAt(modelRow);

    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
        "Editar Producto", true);
    dialog.setLayout(new BorderLayout(10, 10));

    // Panel principal
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Campos del formulario
    JTextField txtCodigo = new JTextField(productoOriginal.getCodigo(), 20);
    txtCodigo.setEditable(false); // El código no se puede editar
    JTextField txtNombre = new JTextField(productoOriginal.getNombre(), 20);
    JTextArea txtDescripcion = new JTextArea(productoOriginal.getDescripcion(), 3, 20);
    JTextField txtUnidadMedida = new JTextField(productoOriginal.getUnidadMedida(), 20);
    JTextField txtPrecioUnitario = new JTextField(String.valueOf(productoOriginal.getPrecioUnitario()), 20);
    JSpinner spnStockMinimo = new JSpinner(new SpinnerNumberModel(
        productoOriginal.getStockMinimo(), 0, 999999, 1));
    JSpinner spnStockActual = new JSpinner(new SpinnerNumberModel(
        productoOriginal.getStockActual(), 0, 999999, 1));

    // Agregar componentes con sus etiquetas
    agregarCampo(mainPanel, gbc, "Código", txtCodigo);
    agregarCampo(mainPanel, gbc, "Nombre (*)", txtNombre);
    
    // Descripción con scroll
    gbc.gridy++;
    mainPanel.add(new JLabel("Descripción:"), gbc);
    gbc.gridx = 1;
    txtDescripcion.setLineWrap(true);
    txtDescripcion.setWrapStyleWord(true);
    JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
    mainPanel.add(scrollDescripcion, gbc);
    
    gbc.gridx = 0;
    agregarCampo(mainPanel, gbc, "Unidad Medida (*)", txtUnidadMedida);
    agregarCampo(mainPanel, gbc, "Precio Unitario (*)", txtPrecioUnitario);
    agregarCampo(mainPanel, gbc, "Stock Mínimo (*)", spnStockMinimo);
    agregarCampo(mainPanel, gbc, "Stock Actual (*)", spnStockActual);

    // Panel de botones
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnGuardar = new JButton("Guardar");
    JButton btnCancelar = new JButton("Cancelar");

    btnGuardar.addActionListener(e -> {
        try {
            // Validaciones básicas
            if (txtNombre.getText().trim().isEmpty() ||
                txtUnidadMedida.getText().trim().isEmpty() ||
                txtPrecioUnitario.getText().trim().isEmpty()) {
                throw new ValidationException("campos", "Los campos marcados con (*) son obligatorios");
            }

            // Actualizar producto
            Producto producto = new Producto();
            producto.setId(productoOriginal.getId());
            producto.setCodigo(productoOriginal.getCodigo());
            producto.setNombre(txtNombre.getText().trim());
            producto.setDescripcion(txtDescripcion.getText().trim());
            producto.setUnidadMedida(txtUnidadMedida.getText().trim());
            try {
                producto.setPrecioUnitario(Double.parseDouble(txtPrecioUnitario.getText().trim()));
            } catch (NumberFormatException ex) {
                throw new ValidationException("precioUnitario", "El precio debe ser un número válido");
            }
            producto.setStockMinimo((Integer) spnStockMinimo.getValue());
            producto.setStockActual((Integer) spnStockActual.getValue());
            producto.setActivo(productoOriginal.isActivo());

            // Guardar cambios
            controller.actualizarProducto(producto);

            // Actualizar tabla
            loadInitialData();

            // Cerrar diálogo
            dialog.dispose();

            JOptionPane.showMessageDialog(this,
                "Producto actualizado exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            logger.error("Error al actualizar producto", ex);
            JOptionPane.showMessageDialog(dialog,
                "Error al actualizar producto: " + ex.getMessage(),
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
}

    private void eliminarProducto() {
    int selectedRow = tablaProductos.getSelectedRow();
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this,
            "Por favor, seleccione un producto para eliminar",
            "Selección requerida",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    int modelRow = tablaProductos.convertRowIndexToModel(selectedRow);
    Producto producto = modeloTabla.getProductoAt(modelRow);

    int confirmacion = JOptionPane.showConfirmDialog(this,
        "¿Está seguro de eliminar el producto " + producto.getNombre() + "?",
        "Confirmar eliminación",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);

    if (confirmacion == JOptionPane.YES_OPTION) {
        try {
            controller.eliminarProducto(producto.getId());
            loadInitialData();
            JOptionPane.showMessageDialog(this,
                "Producto eliminado exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            logger.error("Error al eliminar producto", e);
            JOptionPane.showMessageDialog(this,
                "Error al eliminar producto: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, String label, JComponent campo) {
    gbc.gridy++;
    gbc.gridx = 0;
    panel.add(new JLabel(label), gbc);
    gbc.gridx = 1;
    panel.add(campo, gbc);
}
      
    }
