
package com.rintisa.service.impl;

import com.rintisa.model.Producto;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class ProductoTableModel extends AbstractTableModel {
    private final String[] columnNames = {
        "Código", "Nombre", "Unidad Medida", "Descripción", 
        "Precio Unit.", "Stock Min.", "Stock Actual", "Estado"
    };
    
    private final Class<?>[] columnTypes = {
        String.class, String.class, String.class, String.class,
        Double.class, Integer.class, Integer.class, Boolean.class
    };
    
    private List<Producto> productos;
    
    public ProductoTableModel() {
        this.productos = new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return productos.size();
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
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Ninguna celda es editable en la tabla
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < productos.size()) {
            Producto producto = productos.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return producto.getCodigo();
                case 1:
                    return producto.getNombre();
                case 2:
                    return producto.getUnidadMedida();
                case 3:
                    return producto.getDescripcion();
                case 4:
                    return producto.getPrecioUnitario();
                case 5:
                    return producto.getStockMinimo();
                case 6:
                    return producto.getStockActual();
                case 7:
                    return producto.isActivo();
                default:
                    return null;
            }
        }
        return null;
    }
    
    public void setProductos(List<Producto> productos) {
        this.productos = new ArrayList<>(productos != null ? productos : new ArrayList<>());
        fireTableDataChanged();
    }
    
    public Producto getProductoAt(int row) {
        if (row >= 0 && row < productos.size()) {
            return productos.get(row);
        }
        return null;
    }
    
    public void addProducto(Producto producto) {
        if (producto != null) {
            productos.add(producto);
            fireTableRowsInserted(productos.size() - 1, productos.size() - 1);
        }
    }
    
    public void updateProducto(int row, Producto producto) {
        if (row >= 0 && row < productos.size() && producto != null) {
            productos.set(row, producto);
            fireTableRowsUpdated(row, row);
        }
    }
    
    public void removeProducto(int row) {
        if (row >= 0 && row < productos.size()) {
            productos.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    
    public void clear() {
        int size = productos.size();
        productos.clear();
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    }
}