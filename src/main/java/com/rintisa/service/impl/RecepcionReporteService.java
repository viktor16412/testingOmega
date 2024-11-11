package com.rintisa.service.impl;

import com.rintisa.model.DetalleRecepcion;
import com.rintisa.model.RecepcionMercancia;
import com.rintisa.service.interfaces.IRecepcionMercanciaService;
import com.rintisa.service.interfaces.IRecepcionReporteService;
import com.rintisa.exception.ReportException;
import com.rintisa.service.interfaces.IProductoService;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecepcionReporteService implements IRecepcionReporteService {
    
    private final IRecepcionMercanciaService recepcionService;   
    private static final Logger logger = LoggerFactory.getLogger(RecepcionReporteService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      
    public RecepcionReporteService(IRecepcionMercanciaService recepcionService) {
        this.recepcionService = recepcionService;
    }
    
    @Override
    public byte[] generarReporteGeneral(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
            throws ReportException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte de Recepciones");
            
            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            // Crear título del reporte
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE GENERAL DE RECEPCIONES");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            // Crear fila de período
            Row periodRow = sheet.createRow(1);
            Cell periodCell = periodRow.createCell(0);
            periodCell.setCellValue("Período: " + fechaInicio.format(DATE_FORMATTER) + 
                                  " - " + fechaFin.format(DATE_FORMATTER));
            periodCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

            // Crear encabezados
            Row headerRow = sheet.createRow(3);
            String[] headers = {
                "N° Recepción", "Fecha", "Proveedor", "Orden Compra", 
                "Estado", "Total Items", "Total Valor", "Observaciones"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Obtener datos
            List<RecepcionMercancia> recepciones = recepcionService.listarPorFechas(fechaInicio, fechaFin);
            
            int rowNum = 4;
            double totalValor = 0;
            int totalItems = 0;

            // Llenar datos
            for (RecepcionMercancia recepcion : recepciones) {
                Row row = sheet.createRow(rowNum++);
                
                // N° Recepción
                row.createCell(0).setCellValue(recepcion.getNumeroRecepcion());
                
                // Fecha
                Cell fechaCell = row.createCell(1);
                fechaCell.setCellValue(recepcion.getFechaRecepcion().format(DATE_FORMATTER));
                fechaCell.setCellStyle(dateStyle);
                
                // Proveedor
                row.createCell(2).setCellValue(recepcion.getProveedorNombre());
                
                // Orden Compra
                row.createCell(3).setCellValue(recepcion.getNumeroOrdenCompra());
                
                // Estado
                row.createCell(4).setCellValue(recepcion.getEstado().getDescripcion());
                
                // Calcular totales
                List<DetalleRecepcion> detalles = recepcionService.listarDetalles(recepcion.getId());
                int items = 0;
                double valor = 0;
                
                for (DetalleRecepcion detalle : detalles) {
                    items += detalle.getCantidadEsperada();
                    valor += detalle.getCantidadEsperada() * detalle.getPrecioUnitario();
                }
                
                // Total Items
                Cell itemsCell = row.createCell(5);
                itemsCell.setCellValue(items);
                itemsCell.setCellStyle(numberStyle);
                
                // Total Valor
                Cell valorCell = row.createCell(6);
                valorCell.setCellValue(valor);
                valorCell.setCellStyle(numberStyle);
                
                // Observaciones
                row.createCell(7).setCellValue(recepcion.getObservaciones());
                
                totalItems += items;
                totalValor += valor;
            }

            // Agregar totales
            Row totalRow = sheet.createRow(rowNum + 1);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTALES:");
            totalLabel.setCellStyle(headerStyle);
            
            Cell totalItemsCell = totalRow.createCell(5);
            totalItemsCell.setCellValue(totalItems);
            totalItemsCell.setCellStyle(numberStyle);
            
            Cell totalValorCell = totalRow.createCell(6);
            totalValorCell.setCellValue(totalValor);
            totalValorCell.setCellStyle(numberStyle);

            // Ajustar anchos de columna
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Error al generar reporte general", e);
            throw new ReportException("Error al generar reporte general: " + e.getMessage());
        }
    }

    // Métodos auxiliares para crear estilos
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
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
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
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
