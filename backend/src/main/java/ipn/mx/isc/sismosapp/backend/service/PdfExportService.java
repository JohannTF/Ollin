package ipn.mx.isc.sismosapp.backend.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import ipn.mx.isc.sismosapp.backend.model.dto.ReporteSismicoDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PdfExportService {

    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Genera un PDF del reporte sísmico
     */
    public byte[] generarReportePdf(ReporteSismicoDTO reporte) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Fuentes
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // === HEADER ===
        Paragraph header = new Paragraph("REPORTE SÍSMICO")
            .setFont(boldFont)
            .setFontSize(20)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(new DeviceRgb(102, 126, 234));
        document.add(header);

        Paragraph tipo = new Paragraph(reporte.getTipoReporte())
            .setFont(boldFont)
            .setFontSize(16)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10);
        document.add(tipo);

        // Periodo
        String periodo = String.format("Periodo: %s al %s",
            reporte.getFechaInicio().format(DATE_FORMATTER),
            reporte.getFechaFin().format(DATE_FORMATTER)
        );
        document.add(new Paragraph(periodo)
            .setFont(normalFont)
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5));

        // Fecha de generación
        document.add(new Paragraph("Generado: " + 
            reporte.getFechaGeneracion().format(DATE_FORMATTER))
            .setFont(normalFont)
            .setFontSize(9)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20)
            .setFontColor(ColorConstants.GRAY));

        // === ESTADÍSTICAS PRINCIPALES ===
        document.add(new Paragraph("Estadísticas Principales")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginBottom(10));

        Table statsTable = new Table(4);
        statsTable.setWidth(UnitValue.createPercentValue(100));
        
        // Headers
        statsTable.addHeaderCell(createHeaderCell("Total Sismos", boldFont));
        statsTable.addHeaderCell(createHeaderCell("Mag. Promedio", boldFont));
        statsTable.addHeaderCell(createHeaderCell("Mag. Máxima", boldFont));
        statsTable.addHeaderCell(createHeaderCell("Prof. Promedio", boldFont));
        
        // Values
        statsTable.addCell(createDataCell(
            String.format("%,d", reporte.getTotalSismos()), normalFont));
        statsTable.addCell(createDataCell(
            String.format("%.2f", reporte.getMagnitudPromedio()), normalFont));
        statsTable.addCell(createDataCell(
            String.format("%.1f", reporte.getMagnitudMaxima()), normalFont));
        statsTable.addCell(createDataCell(
            String.format("%.1f km", reporte.getProfundidadPromedio()), normalFont));

        document.add(statsTable);
        document.add(new Paragraph("\n"));

        // === DISTRIBUCIÓN POR MAGNITUD ===
        document.add(new Paragraph("Distribución por Magnitud")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginBottom(10));

        Table magnitudTable = new Table(2);
        magnitudTable.setWidth(UnitValue.createPercentValue(100));
        magnitudTable.addHeaderCell(createHeaderCell("Rango", boldFont));
        magnitudTable.addHeaderCell(createHeaderCell("Cantidad", boldFont));

        for (Map.Entry<String, Long> entry : reporte.getDistribucionPorMagnitud().entrySet()) {
            magnitudTable.addCell(createDataCell(entry.getKey(), normalFont));
            magnitudTable.addCell(createDataCell(
                String.format("%,d", entry.getValue()), normalFont));
        }

        document.add(magnitudTable);
        document.add(new Paragraph("\n"));

        // === TOP ESTADOS ===
        document.add(new Paragraph("Top 10 Estados con Mayor Actividad")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginBottom(10));

        Table estadosTable = new Table(2);
        estadosTable.setWidth(UnitValue.createPercentValue(100));
        estadosTable.addHeaderCell(createHeaderCell("Estado", boldFont));
        estadosTable.addHeaderCell(createHeaderCell("Sismos", boldFont));

        reporte.getDistribucionPorEstado().entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> {
                estadosTable.addCell(createDataCell(entry.getKey(), normalFont));
                estadosTable.addCell(createDataCell(
                    String.format("%,d", entry.getValue()), normalFont));
            });

        document.add(estadosTable);
        
        // Nueva página para sismos más fuertes
        document.add(new AreaBreak());

        // === TOP 10 SISMOS MÁS FUERTES ===
        document.add(new Paragraph("Top 10 Sismos Más Fuertes")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginBottom(10));

        Table sismosTable = new Table(new float[]{1, 2, 2, 1, 4});
        sismosTable.setWidth(UnitValue.createPercentValue(100));
        
        sismosTable.addHeaderCell(createHeaderCell("#", boldFont));
        sismosTable.addHeaderCell(createHeaderCell("Fecha", boldFont));
        sismosTable.addHeaderCell(createHeaderCell("Hora", boldFont));
        sismosTable.addHeaderCell(createHeaderCell("Mag.", boldFont));
        sismosTable.addHeaderCell(createHeaderCell("Ubicación", boldFont));

        int index = 1;
        for (ReporteSismicoDTO.SismoResumenDTO sismo : reporte.getSismosMasFuertes()) {
            sismosTable.addCell(createDataCell(String.valueOf(index++), normalFont));
            sismosTable.addCell(createDataCell(sismo.getFecha(), normalFont));
            sismosTable.addCell(createDataCell(sismo.getHora(), normalFont));
            sismosTable.addCell(createDataCell(
                String.format("%.1f", sismo.getMagnitud()), normalFont)
                .setFontColor(getColorMagnitud(sismo.getMagnitud()))
                .setBold());
            sismosTable.addCell(createDataCell(sismo.getUbicacion(), normalFont));
        }

        document.add(sismosTable);

        // Footer
        document.add(new Paragraph("\n\n"));
        document.add(new Paragraph("Reporte generado por Sistema de Monitoreo Sísmico")
            .setFont(normalFont)
            .setFontSize(8)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.GRAY));

        document.close();
        return baos.toByteArray();
    }

    private Cell createHeaderCell(String content, PdfFont font) {
        return new Cell()
            .add(new Paragraph(content).setFont(font).setFontSize(10))
            .setBackgroundColor(new DeviceRgb(102, 126, 234))
            .setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(8)
            .setBorder(Border.NO_BORDER);
    }

    private Cell createDataCell(String content, PdfFont font) {
        return new Cell()
            .add(new Paragraph(content).setFont(font).setFontSize(9))
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(6);
    }

    private DeviceRgb getColorMagnitud(Double magnitud) {
        if (magnitud >= 7.0) return new DeviceRgb(229, 57, 53);      // Rojo
        if (magnitud >= 6.0) return new DeviceRgb(255, 111, 0);      // Naranja oscuro
        if (magnitud >= 5.0) return new DeviceRgb(255, 160, 0);      // Naranja
        if (magnitud >= 4.0) return new DeviceRgb(253, 216, 53);     // Amarillo
        return new DeviceRgb(67, 160, 71);                            // Verde
    }
}