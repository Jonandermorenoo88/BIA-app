package com.bia.app.bia_app.service;

import com.bia.app.bia_app.model.BiaProyecto;
import com.bia.app.bia_app.model.ProcesoCritico;
import com.bia.app.bia_app.model.ActivoTecnologico;
import com.bia.app.bia_app.model.Persona;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportBiaToExcel(BiaProyecto bia) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("BIA - " + bia.getNombre().replaceAll("[^a-zA-Z0-9 ]", ""));
            
            // Header fonts & styles
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Row styles for heatmap logic
            CellStyle styleGreen = createRiskStyle(workbook, IndexedColors.LIGHT_GREEN.getIndex());
            CellStyle styleYellow = createRiskStyle(workbook, IndexedColors.LIGHT_YELLOW.getIndex());
            CellStyle styleRed = createRiskStyle(workbook, IndexedColors.RED.getIndex());
            CellStyle styleDarkRed = createRiskStyle(workbook, IndexedColors.DARK_RED.getIndex());
            
            // Text wrap style
            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            // Encabezados de tabla
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Nombre del Proceso", "Descripción", "RTO (Horas)", "RPO",
                "Impacto (1-5)", "Probabilidad (1-5)", "Criticidad Automática (2-10)", "Riesgo Total (Heatmap)",
                "Ciberactivos Vinculados", "Personal Clave Vinculado"
            };

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Datos
            int rowIdx = 1;
            List<ProcesoCritico> procesos = bia.getProcesos();
            
            if (procesos != null) {
                for (ProcesoCritico p : procesos) {
                    Row row = sheet.createRow(rowIdx++);
                    
                    int impacto = p.getImpacto() != null ? p.getImpacto() : 1;
                    int prob = p.getProbabilidad() != null ? p.getProbabilidad() : 1;
                    int criticidad = p.getCriticidad() != null ? p.getCriticidad() : 2;
                    int riesgoVisual = impacto * prob; // Heatmap usa Impacto * Prob

                    row.createCell(0).setCellValue(p.getNombre());
                    row.createCell(1).setCellValue(p.getDescripcion());
                    if (p.getRtoHoras() != null) row.createCell(2).setCellValue(p.getRtoHoras());
                    else row.createCell(2).setCellValue("");
                    row.createCell(3).setCellValue(p.getRpo());
                    row.createCell(4).setCellValue(impacto);
                    row.createCell(5).setCellValue(prob);
                    row.createCell(6).setCellValue(criticidad);
                    
                    // Columna Riesgo Total (Heatmap magic)
                    Cell riskCell = row.createCell(7);
                    riskCell.setCellValue(riesgoVisual);
                    if (riesgoVisual >= 15) riskCell.setCellStyle(styleDarkRed);
                    else if (riesgoVisual >= 10) riskCell.setCellStyle(styleRed);
                    else if (riesgoVisual >= 6) riskCell.setCellStyle(styleYellow);
                    else riskCell.setCellStyle(styleGreen);

                    // Formatear dependencias (lista String)
                    String activosStr = "";
                    if (p.getActivos() != null && !p.getActivos().isEmpty()) {
                        activosStr = p.getActivos().stream().map(ActivoTecnologico::getNombre).collect(Collectors.joining(", "));
                    }
                    Cell cellAct = row.createCell(8);
                    cellAct.setCellValue(activosStr);
                    cellAct.setCellStyle(wrapStyle);

                    String persStr = "";
                    if (p.getPersonas() != null && !p.getPersonas().isEmpty()) {
                        persStr = p.getPersonas().stream().map(Persona::getNombre).collect(Collectors.joining(", "));
                    }
                    Cell cellPer = row.createCell(9);
                    cellPer.setCellValue(persStr);
                    cellPer.setCellStyle(wrapStyle);
                }
            }

            // Auto-size all columns
            for (int i = 0; i < headers.length; i++) {
                if (i == 1 || i == 8 || i == 9) { // Las columnas descriptivas las dejamos a ancho fijo
                    sheet.setColumnWidth(i, 256 * 40);
                } else {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
    
    private CellStyle createRiskStyle(Workbook workbook, short bgColorIndex) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(bgColorIndex);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        
        if (bgColorIndex == IndexedColors.RED.getIndex() || bgColorIndex == IndexedColors.DARK_RED.getIndex()) {
            Font font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            style.setFont(font);
        }
        return style;
    }
}
