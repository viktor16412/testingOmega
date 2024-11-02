/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rintisa.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.rintisa.model.Usuario;
import com.rintisa.model.Rol;
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

public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    private static final String REPORTS_DIR = "reportes";
    
    // Inicializar directorio de reportes
    static {
        try {
            Files.createDirectories(Paths.get(REPORTS_DIR));
        } catch (Exception e) {
            logger.error("Error al crear directorio de reportes", e);
        }
    }

    public static String generarReporteUsuarios(List<Usuario> usuarios) {
        String fileName = generarNombreArchivo("Usuarios");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Usuarios");
            
            // Crear estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);
            
            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Usuario", "Nombre", "Apellido", "Email", "Rol", "Estado", "Último Acceso"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            int rowNum = 1;
            for (Usuario usuario : usuarios) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(usuario.getId());
                row.createCell(1).setCellValue(usuario.getUsername());
                row.createCell(2).setCellValue(usuario.getNombre());
                row.createCell(3).setCellValue(usuario.getApellido());
                row.createCell(4).setCellValue(usuario.getEmail());
                row.createCell(5).setCellValue(usuario.getRol().getNombre());
                row.createCell(6).setCellValue(usuario.isActivo() ? "Activo" : "Inactivo");
                
                Cell lastAccessCell = row.createCell(7);
                if (usuario.getUltimoAcceso() != null) {
                    lastAccessCell.setCellValue(usuario.getUltimoAcceso().toString());
                    lastAccessCell.setCellStyle(dateStyle);
                }
            }
            
            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Guardar archivo
            String filePath = REPORTS_DIR + File.separator + fileName;
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            
            logger.info("Reporte de usuarios generado: {}", filePath);
            return filePath;
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de usuarios", e);
            throw new RuntimeException("Error al generar reporte: " + e.getMessage());
        }
    }

    public static String generarReporteRoles(List<Rol> roles) {
        String fileName = generarNombreArchivo("Roles");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Roles");
            
            // Crear estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            
            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Nombre", "Descripción", "Estado"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            int rowNum = 1;
            for (Rol rol : roles) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(rol.getId());
                row.createCell(1).setCellValue(rol.getNombre());
                row.createCell(2).setCellValue(rol.getDescripcion());
                row.createCell(3).setCellValue(rol.isActivo() ? "Activo" : "Inactivo");
            }
            
            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Guardar archivo
            String filePath = REPORTS_DIR + File.separator + fileName;
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            
            logger.info("Reporte de roles generado: {}", filePath);
            return filePath;
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de roles", e);
            throw new RuntimeException("Error al generar reporte: " + e.getMessage());
        }
    }

    private static String generarNombreArchivo(String tipo) {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("Reporte_%s_%s.xlsx", tipo, timestamp);
    }

    private static CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
        return style;
    }
    
    public static String getRutaReportes() {
        try {
            Path path = Paths.get(REPORTS_DIR).toAbsolutePath();
            return path.toString();
        } catch (Exception e) {
            logger.error("Error al obtener ruta de reportes", e);
            return null;
        }
    }

    public static void abrirDirectorioReportes() {
        try {
            Path path = Paths.get(REPORTS_DIR).toAbsolutePath();
            File file = path.toFile();
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            logger.error("Error al abrir directorio de reportes", e);
        }
    }
}
