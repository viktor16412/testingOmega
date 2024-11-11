
package com.rintisa.service.impl;

import com.rintisa.model.Producto;
import com.rintisa.service.interfaces.IProductoService;
import com.rintisa.exception.ReportException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProductoReporteService {
    private static final Logger logger = LoggerFactory.getLogger(ProductoReporteService.class);
    private final IProductoService productoService;

    public ProductoReporteService(IProductoService productoService) {
        this.productoService = productoService;
    }

    public byte[] generarReporteProductos() throws ReportException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Crear hoja de trabajo
            Sheet sheet = workbook.createSheet("Productos");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // Crear título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE PRODUCTOS");
            CellStyle titleStyle = createTitleStyle(workbook);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            // Fecha del reporte
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Fecha de generación: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            dateCell.setCellStyle(dateStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

            // Crear encabezados
            String[] headers = {
                "Código", "Nombre", "Unidad Medida", "Descripción",
                "Precio Unit.", "Stock Mín.", "Stock Actual", "Estado"
            };
            
            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Obtener datos
            List<Producto> productos = productoService.listarTodos();
            int rowNum = 4;
            
            // Variables para totales
            double totalValorInventario = 0;
            int totalProductos = 0;
            int productosStockBajo = 0;

            // Llenar datos
            for (Producto producto : productos) {
                Row row = sheet.createRow(rowNum++);
                
                // Código
                Cell codCell = row.createCell(0);
                codCell.setCellValue(producto.getCodigo());
                codCell.setCellStyle(centerStyle);
                
                // Nombre
                row.createCell(1).setCellValue(producto.getNombre());
                
                // Unidad Medida
                Cell umCell = row.createCell(2);
                umCell.setCellValue(producto.getUnidadMedida());
                umCell.setCellStyle(centerStyle);
                
                // Descripción
                row.createCell(3).setCellValue(producto.getDescripcion());
                
                // Precio Unitario
                Cell precioCell = row.createCell(4);
                precioCell.setCellValue(producto.getPrecioUnitario());
                precioCell.setCellStyle(moneyStyle);
                
                // Stock Mínimo
                Cell minCell = row.createCell(5);
                minCell.setCellValue(producto.getStockMinimo());
                minCell.setCellStyle(numberStyle);
                
                // Stock Actual
                Cell stockCell = row.createCell(6);
                stockCell.setCellValue(producto.getStockActual());
                stockCell.setCellStyle(numberStyle);
                
                // Estado
                Cell estadoCell = row.createCell(7);
                estadoCell.setCellValue(producto.isActivo() ? "Activo" : "Inactivo");
                estadoCell.setCellStyle(centerStyle);

                // Calcular totales
                totalValorInventario += producto.getPrecioUnitario() * producto.getStockActual();
                totalProductos++;
                if (producto.getStockActual() <= producto.getStockMinimo()) {
                    productosStockBajo++;
                }
            }

            // Agregar resumen
            rowNum += 2;
            Row summaryRow = sheet.createRow(rowNum++);
            Cell summaryCell = summaryCell = summaryRow.createCell(0);
            summaryCell.setCellValue("RESUMEN:");
            summaryCell.setCellStyle(headerStyle);

            // Total de productos
            Row totalRow = sheet.createRow(rowNum++);
            totalRow.createCell(0).setCellValue("Total de Productos:");
            Cell totalCell = totalRow.createCell(1);
            totalCell.setCellValue(totalProductos);
            totalCell.setCellStyle(numberStyle);

            // Productos con stock bajo
            Row stockBajoRow = sheet.createRow(rowNum++);
            stockBajoRow.createCell(0).setCellValue("Productos con Stock Bajo:");
            Cell stockBajoCell = stockBajoRow.createCell(1);
            stockBajoCell.setCellValue(productosStockBajo);
            stockBajoCell.setCellStyle(numberStyle);

            // Valor total del inventario
            Row valorRow = sheet.createRow(rowNum++);
            valorRow.createCell(0).setCellValue("Valor Total del Inventario:");
            Cell valorCell = valorRow.createCell(1);
            valorCell.setCellValue(totalValorInventario);
            valorCell.setCellStyle(moneyStyle);

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Error al generar reporte de productos", e);
            throw new ReportException("Error al generar reporte: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}