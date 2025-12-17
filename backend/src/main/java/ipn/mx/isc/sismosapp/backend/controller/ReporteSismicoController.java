package ipn.mx.isc.sismosapp.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipn.mx.isc.sismosapp.backend.model.dto.ReporteSismicoDTO;
import ipn.mx.isc.sismosapp.backend.service.PdfExportService;
import ipn.mx.isc.sismosapp.backend.service.ReporteSismicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes Sísmicos", description = "API para generación de reportes estadísticos de actividad sísmica")
public class ReporteSismicoController {

    @Autowired
    private ReporteSismicoService reporteSismicoService;

    @Autowired
    private PdfExportService pdfExportService;

    // ========== ENDPOINTS JSON ==========

    @Operation(
        summary = "Obtener reporte trimestral automático (últimos 3 meses)",
        description = "Genera un reporte estadístico de los sismos registrados en los últimos 3 meses. " +
                      "Este endpoint es público y no requiere autenticación."
    )
    @GetMapping("/trimestral")
    public ResponseEntity<ReporteSismicoDTO> getReporteTrimestral() {
        ReporteSismicoDTO reporte = reporteSismicoService.generarReporteTrimestral();
        return ResponseEntity.ok(reporte);
    }

    @Operation(
        summary = "Obtener reporte semestral automático (últimos 6 meses)",
        description = "Genera un reporte estadístico de los sismos registrados en los últimos 6 meses."
    )
    @GetMapping("/semestral")
    public ResponseEntity<ReporteSismicoDTO> getReporteSemestral() {
        ReporteSismicoDTO reporte = reporteSismicoService.generarReporteSemestral();
        return ResponseEntity.ok(reporte);
    }

    @Operation(
        summary = "Obtener reporte anual automático (último año)",
        description = "Genera un reporte estadístico de los sismos registrados en el último año."
    )
    @GetMapping("/anual")
    public ResponseEntity<ReporteSismicoDTO> getReporteAnual() {
        ReporteSismicoDTO reporte = reporteSismicoService.generarReporteAnual();
        return ResponseEntity.ok(reporte);
    }

    @Operation(
        summary = "Generar reporte personalizado por fechas",
        description = "Genera un reporte estadístico para un rango de fechas específico."
    )
    @GetMapping("/personalizado")
    public ResponseEntity<?> getReportePersonalizado(
        @Parameter(description = "Fecha de inicio", example = "2024-01-01T00:00:00")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
        
        @Parameter(description = "Fecha de fin", example = "2024-12-31T23:59:59")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) {
        try {
            ReporteSismicoDTO reporte = reporteSismicoService.generarReportePersonalizado(fechaInicio, fechaFin);
            return ResponseEntity.ok(reporte);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ========== ENDPOINTS PDF EXPORT ⭐ ==========

    @Operation(
        summary = "Exportar reporte trimestral en PDF",
        description = "Descarga el reporte trimestral en formato PDF"
    )
    @GetMapping("/trimestral/pdf")
    public ResponseEntity<byte[]> exportarReporteTrimestralPdf() {
        try {
            ReporteSismicoDTO reporte = reporteSismicoService.generarReporteTrimestral();
            byte[] pdfBytes = pdfExportService.generarReportePdf(reporte);
            
            String filename = generarNombreArchivo("trimestral");
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Exportar reporte semestral en PDF",
        description = "Descarga el reporte semestral en formato PDF"
    )
    @GetMapping("/semestral/pdf")
    public ResponseEntity<byte[]> exportarReporteSemestralPdf() {
        try {
            ReporteSismicoDTO reporte = reporteSismicoService.generarReporteSemestral();
            byte[] pdfBytes = pdfExportService.generarReportePdf(reporte);
            
            String filename = generarNombreArchivo("semestral");
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Exportar reporte anual en PDF",
        description = "Descarga el reporte anual en formato PDF"
    )
    @GetMapping("/anual/pdf")
    public ResponseEntity<byte[]> exportarReporteAnualPdf() {
        try {
            ReporteSismicoDTO reporte = reporteSismicoService.generarReporteAnual();
            byte[] pdfBytes = pdfExportService.generarReportePdf(reporte);
            
            String filename = generarNombreArchivo("anual");
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Exportar reporte personalizado en PDF",
        description = "Descarga el reporte personalizado en formato PDF"
    )
    @GetMapping("/personalizado/pdf")
    public ResponseEntity<?> exportarReportePersonalizadoPdf(
        @Parameter(description = "Fecha de inicio", example = "2024-01-01T00:00:00")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
        
        @Parameter(description = "Fecha de fin", example = "2024-12-31T23:59:59")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) {
        try {
            ReporteSismicoDTO reporte = reporteSismicoService.generarReportePersonalizado(fechaInicio, fechaFin);
            byte[] pdfBytes = pdfExportService.generarReportePdf(reporte);
            
            String filename = generarNombreArchivo("personalizado");
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error generando PDF: " + e.getMessage()));
        }
    }

    // ========== VISTA WEB ==========

    @GetMapping("/view")
    public ResponseEntity<String> viewReporte() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Reportes Sísmicos</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        margin: 20px;
                        background: #f5f5f5;
                    }
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                        background: white;
                        padding: 30px;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
                    h2 { color: #34495e; margin-top: 30px; }
                    .stats-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                        gap: 20px;
                        margin: 20px 0;
                    }
                    .stat-card {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 20px;
                        border-radius: 8px;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .stat-card h3 {
                        margin: 0;
                        font-size: 14px;
                        opacity: 0.9;
                    }
                    .stat-card .value {
                        font-size: 32px;
                        font-weight: bold;
                        margin: 10px 0;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin: 20px 0;
                    }
                    th, td {
                        padding: 12px;
                        text-align: left;
                        border-bottom: 1px solid #ddd;
                    }
                    th {
                        background: #3498db;
                        color: white;
                    }
                    tr:hover { background: #f5f5f5; }
                    button {
                        background: #3498db;
                        color: white;
                        padding: 12px 24px;
                        border: none;
                        border-radius: 4px;
                        cursor: pointer;
                        font-size: 16px;
                        margin: 5px;
                    }
                    button:hover { background: #2980b9; }
                    button.pdf {
                        background: #e74c3c;
                    }
                    button.pdf:hover {
                        background: #c0392b;
                    }
                    .loading {
                        text-align: center;
                        padding: 40px;
                        color: #7f8c8d;
                    }
                    .chart-container {
                        background: #f8f9fa;
                        padding: 20px;
                        border-radius: 8px;
                        margin: 20px 0;
                    }
                    .distribution-item {
                        display: flex;
                        align-items: center;
                        margin: 10px 0;
                    }
                    .distribution-label {
                        min-width: 150px;
                        font-weight: bold;
                    }
                    .distribution-bar {
                        background: #3498db;
                        height: 30px;
                        border-radius: 4px;
                        display: flex;
                        align-items: center;
                        padding: 0 10px;
                        color: white;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>📊 Reportes Sísmicos del SSN</h1>
                    
                    <div>
                        <button onclick="cargarReporte('trimestral')">📅 3 Meses</button>
                        <button onclick="cargarReporte('semestral')">📅 6 Meses</button>
                        <button onclick="cargarReporte('anual')">📅 1 Año</button>
                        <button class="pdf" onclick="descargarPdf('trimestral')">📄 PDF 3M</button>
                        <button class="pdf" onclick="descargarPdf('semestral')">📄 PDF 6M</button>
                        <button class="pdf" onclick="descargarPdf('anual')">📄 PDF 1A</button>
                        <button onclick="location.href='/api/upload/form'">⬅️ Volver</button>
                    </div>

                    <div id="contenido" class="loading">
                        <p>Selecciona un periodo de reporte para comenzar</p>
                    </div>
                </div>

                <script>
                    function descargarPdf(tipo) {
                        window.location.href = `/api/reportes/${tipo}/pdf`;
                    }

                    async function cargarReporte(tipo) {
                        document.getElementById('contenido').innerHTML = '<p class="loading">Generando reporte...</p>';
                        
                        try {
                            const response = await fetch(`/api/reportes/${tipo}`);
                            const data = await response.json();
                            mostrarReporte(data);
                        } catch (error) {
                            document.getElementById('contenido').innerHTML = 
                                '<p style="color:red">Error cargando reporte: ' + error.message + '</p>';
                        }
                    }

                    function mostrarReporte(reporte) {
                        const periodo = `${formatearFecha(reporte.fechaInicio)} al ${formatearFecha(reporte.fechaFin)}`;
                        
                        let html = `
                            <h2>Reporte ${reporte.tipoReporte}</h2>
                            <p><strong>Periodo:</strong> ${periodo}</p>
                            <p><strong>Generado:</strong> ${formatearFecha(reporte.fechaGeneracion)}</p>

                            <div class="stats-grid">
                                <div class="stat-card">
                                    <h3>Total de Sismos</h3>
                                    <div class="value">${reporte.totalSismos.toLocaleString()}</div>
                                </div>
                                <div class="stat-card">
                                    <h3>Magnitud Promedio</h3>
                                    <div class="value">${reporte.magnitudPromedio.toFixed(2)}</div>
                                </div>
                                <div class="stat-card">
                                    <h3>Magnitud Máxima</h3>
                                    <div class="value">${reporte.magnitudMaxima.toFixed(1)}</div>
                                </div>
                                <div class="stat-card">
                                    <h3>Profundidad Promedio</h3>
                                    <div class="value">${reporte.profundidadPromedio.toFixed(1)} km</div>
                                </div>
                            </div>

                            <h2>📈 Distribución por Magnitud</h2>
                            <div class="chart-container">
                                ${renderDistribucion(reporte.distribucionPorMagnitud)}
                            </div>

                            <h2>🗺️ Top Estados con Mayor Actividad</h2>
                            <div class="chart-container">
                                ${renderDistribucion(reporte.distribucionPorEstado)}
                            </div>

                            <h2>🔥 Top 10 Sismos Más Fuertes</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Fecha</th>
                                        <th>Hora</th>
                                        <th>Magnitud</th>
                                        <th>Profundidad</th>
                                        <th>Ubicación</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${reporte.sismosMasFuertes.map((s, i) => `
                                        <tr>
                                            <td>${i + 1}</td>
                                            <td>${s.fecha}</td>
                                            <td>${s.hora}</td>
                                            <td><strong>${s.magnitud}</strong></td>
                                            <td>${s.profundidad} km</td>
                                            <td>${s.ubicacion}</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>

                            <h2>📅 Actividad por Mes</h2>
                            <div class="chart-container">
                                ${renderDistribucion(reporte.distribucionPorMes)}
                            </div>
                        `;

                        document.getElementById('contenido').innerHTML = html;
                    }

                    function renderDistribucion(distribucion) {
                        const max = Math.max(...Object.values(distribucion));
                        
                        return Object.entries(distribucion)
                            .map(([key, value]) => {
                                const percentage = (value / max) * 100;
                                return `
                                    <div class="distribution-item">
                                        <div class="distribution-label">${key}</div>
                                        <div class="distribution-bar" style="width: ${percentage}%">
                                            ${value.toLocaleString()}
                                        </div>
                                    </div>
                                `;
                            })
                            .join('');
                    }

                    function formatearFecha(fecha) {
                        return new Date(fecha).toLocaleString('es-MX', {
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                        });
                    }
                </script>
            </body>
            </html>
            """;
        return ResponseEntity.ok(html);
    }

    // ========== UTILIDADES ==========

    private String generarNombreArchivo(String tipo) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("reporte_sismico_%s_%s.pdf", tipo, fecha);
    }

    private record ErrorResponse(String mensaje) {}
}