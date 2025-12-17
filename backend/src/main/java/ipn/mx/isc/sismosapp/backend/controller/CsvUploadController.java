package ipn.mx.isc.sismosapp.backend.controller;

import com.opencsv.exceptions.CsvException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipn.mx.isc.sismosapp.backend.model.entities.SismoH;
import ipn.mx.isc.sismosapp.backend.repository.SismoHRepository;
import ipn.mx.isc.sismosapp.backend.service.CsvUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Upload CSV", description = "API para subir archivos CSV del SSN")
public class CsvUploadController {

    @Autowired
    private CsvUploadService csvUploadService;

    @Autowired
    private SismoHRepository sismoHRepository;

    @Operation(summary = "Subir archivo CSV del SSN")
    @PostMapping("/upload/csv")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("El archivo está vacío"));
            }

            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Solo se permiten archivos CSV"));
            }

            // Procesar de forma asíncrona
            csvUploadService.procesarCsvAsync(file);
            
            return ResponseEntity.ok(new SuccessResponse(
                "Archivo recibido. El procesamiento ha iniciado en segundo plano. " +
                "Esto puede tomar varios minutos. Verifica el progreso en /api/sismosh/stats"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                new ErrorResponse("Error procesando el archivo: " + e.getMessage())
            );
        }
    }

    @GetMapping("/upload/form")
    public ResponseEntity<String> showUploadForm() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Subir CSV - SSN</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .container { max-width: 600px; margin: 0 auto; }
                    button { padding: 10px 20px; margin: 10px 5px; cursor: pointer; }
                    #result { margin-top: 20px; padding: 15px; border-radius: 5px; }
                    .success { background: #d4edda; color: #155724; }
                    .error { background: #f8d7da; color: #721c24; }
                    .processing { background: #fff3cd; color: #856404; }
                    input[type="file"] { margin: 10px 0; }
                    .links { margin-top: 30px; padding-top: 20px; border-top: 2px solid #ddd; }
                    a { color: #007bff; text-decoration: none; margin-right: 15px; }
                    a:hover { text-decoration: underline; }
                    .stats { background: #e7f3fe; padding: 15px; margin: 20px 0; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Subir archivo CSV del SSN</h1>
                    <p>Archivos grandes (365k+ registros) se procesan en segundo plano</p>
                    
                    <div class="stats" id="currentStats">
                        <strong>Estadísticas actuales:</strong>
                        <p id="statsInfo">Cargando...</p>
                        <button onclick="checkStats()">Actualizar</button>
                    </div>
                    
                    <form id="uploadForm">
                        <input type="file" id="fileInput" accept=".csv" required>
                        <br>
                        <button type="submit">Subir CSV</button>
                    </form>
                    
                    <div id="result"></div>
                    
                    <div class="links">
                        <h3>Ver datos:</h3>
                        <a href="/api/sismosh/view">Ver sismos históricos (Tabla)</a><br>
                        <a href="/api/sismosh/stats">Ver estadísticas (JSON)</a>
                    </div>
                </div>
                
                <script>
                    async function checkStats() {
                        try {
                            const response = await fetch('/api/sismosh/stats');
                            const data = await response.json();
                            document.getElementById('statsInfo').innerHTML = 
                                `Total: ${data.total.toLocaleString()} sismos`;
                        } catch (error) {
                            document.getElementById('statsInfo').innerHTML = 'Error cargando stats';
                        }
                    }
                    
                    document.getElementById('uploadForm').addEventListener('submit', async (e) => {
                        e.preventDefault();
                        const formData = new FormData();
                        const file = document.getElementById('fileInput').files[0];
                        formData.append('file', file);
                        
                        const resultDiv = document.getElementById('result');
                        resultDiv.className = 'processing';
                        resultDiv.innerHTML = `Subiendo ${file.name}...`;
                        
                        try {
                            const response = await fetch('/api/upload/csv', {
                                method: 'POST',
                                body: formData
                            });
                            const data = await response.json();
                            
                            if (response.ok) {
                                resultDiv.className = 'success';
                                resultDiv.innerHTML = `
                                    <strong>✓ ${data.message}</strong><br><br>
                                    El archivo se está procesando. Actualiza las estadísticas cada minuto para ver el progreso.
                                `;
                                // Auto-actualizar stats cada 5 segundos
                                setInterval(checkStats, 5000);
                            } else {
                                resultDiv.className = 'error';
                                resultDiv.innerHTML = `<strong>✗ Error:</strong><br>${data.mensaje}`;
                            }
                        } catch (error) {
                            resultDiv.className = 'error';
                            resultDiv.innerHTML = `<strong>✗ Error:</strong><br>${error.message}`;
                        }
                    });
                    
                    // Cargar stats iniciales
                    checkStats();
                </script>
            </body>
            </html>
            """;
        return ResponseEntity.ok(html);
    }

    // ========== ENDPOINTS PARA VER DATOS ==========

    @GetMapping("/sismosh/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long total = sismoHRepository.count();
        
        List<SismoH> recientes = sismoHRepository.findAll(
            PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "fechaHora"))
        ).getContent();
        
        List<SismoH> antiguos = sismoHRepository.findAll(
            PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "fechaHora"))
        ).getContent();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("masReciente", recientes.isEmpty() ? null : recientes.get(0));
        stats.put("masAntiguo", antiguos.isEmpty() ? null : antiguos.get(0));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/sismosh")
    public ResponseEntity<List<SismoH>> getSismos(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size
    ) {
        var result = sismoHRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaHora"))
        );
        return ResponseEntity.ok(result.getContent());
    }

    @GetMapping("/sismosh/view")
    public ResponseEntity<String> viewData() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Sismos Históricos - SSN</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    table { border-collapse: collapse; width: 100%; margin-top: 20px; font-size: 14px; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #4CAF50; color: white; position: sticky; top: 0; }
                    tr:nth-child(even) { background-color: #f2f2f2; }
                    .stats { background: #e7f3fe; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
                    .loading { color: #666; font-style: italic; }
                    button { padding: 10px 20px; margin: 5px; cursor: pointer; background: #4CAF50; color: white; border: none; border-radius: 3px; }
                    button:hover { background: #45a049; }
                    button:disabled { background: #ccc; cursor: not-allowed; }
                    .controls { margin: 20px 0; }
                    input[type="number"] { padding: 8px; width: 100px; }
                </style>
            </head>
            <body>
                <h1>Sismos Históricos del SSN</h1>
                
                <div class="stats" id="stats">
                    <h2>Estadísticas</h2>
                    <p class="loading">Cargando...</p>
                </div>

                <div class="controls">
                    <button onclick="loadData(0)">Primera</button>
                    <button onclick="loadData(currentPage - 1)" id="prevBtn">◄ Anterior</button>
                    <span>Página: <strong id="pageNum">1</strong></span>
                    <button onclick="loadData(currentPage + 1)" id="nextBtn">Siguiente ►</button>
                    <input type="number" id="pageInput" placeholder="Ir a..." min="0">
                    <button onclick="goToPage()">Ir</button>
                    <button onclick="location.href='/api/upload/form'">← Volver a subir</button>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>Fecha</th>
                            <th>Hora</th>
                            <th>Mag</th>
                            <th>Lat</th>
                            <th>Lon</th>
                            <th>Prof (km)</th>
                            <th>Ubicación</th>
                            <th>Estatus</th>
                        </tr>
                    </thead>
                    <tbody id="tableBody">
                        <tr><td colspan="8" class="loading">Cargando datos...</td></tr>
                    </tbody>
                </table>

                <script>
                    let currentPage = 0;
                    const pageSize = 50;

                    async function loadStats() {
                        try {
                            const response = await fetch('/api/sismosh/stats');
                            const data = await response.json();
                            
                            document.getElementById('stats').innerHTML = `
                                <h2>Estadísticas</h2>
                                <p><strong>Total:</strong> ${data.total.toLocaleString()} sismos</p>
                                ${data.masReciente ? `
                                    <p><strong>Más reciente:</strong> ${data.masReciente.fecha} ${data.masReciente.hora} - 
                                    M${data.masReciente.magnitud} - ${data.masReciente.referenciaLocalizacion}</p>
                                ` : ''}
                                ${data.masAntiguo ? `
                                    <p><strong>Más antiguo:</strong> ${data.masAntiguo.fecha} ${data.masAntiguo.hora} - 
                                    M${data.masAntiguo.magnitud} - ${data.masAntiguo.referenciaLocalizacion}</p>
                                ` : ''}
                            `;
                        } catch (error) {
                            document.getElementById('stats').innerHTML = 
                                '<p style="color:red">Error cargando estadísticas</p>';
                        }
                    }

                    async function loadData(page) {
                        if (page < 0) return;
                        currentPage = page;
                        
                        try {
                            const response = await fetch(`/api/sismosh?page=${page}&size=${pageSize}`);
                            const data = await response.json();
                            
                            if (data.length === 0 && page > 0) {
                                alert('No hay más datos');
                                return;
                            }
                            
                            const tbody = document.getElementById('tableBody');
                            tbody.innerHTML = data.map(s => `
                                <tr>
                                    <td>${s.fecha}</td>
                                    <td>${s.hora}</td>
                                    <td>${s.magnitud}</td>
                                    <td>${s.latitud.toFixed(2)}</td>
                                    <td>${s.longitud.toFixed(2)}</td>
                                    <td>${s.profundidad}</td>
                                    <td>${s.referenciaLocalizacion}</td>
                                    <td>${s.estatus}</td>
                                </tr>
                            `).join('');
                            
                            document.getElementById('pageNum').textContent = page + 1;
                            document.getElementById('prevBtn').disabled = page === 0;
                            document.getElementById('nextBtn').disabled = data.length < pageSize;
                            
                        } catch (error) {
                            document.getElementById('tableBody').innerHTML = 
                                '<tr><td colspan="8" style="color:red">Error cargando datos</td></tr>';
                        }
                    }
                    
                    function goToPage() {
                        const page = parseInt(document.getElementById('pageInput').value);
                        if (!isNaN(page) && page >= 0) {
                            loadData(page);
                        }
                    }

                    // Cargar datos iniciales
                    loadStats();
                    loadData(0);
                </script>
            </body>
            </html>
            """;
        return ResponseEntity.ok(html);
    }

    private record ErrorResponse(String mensaje) {}
    private record SuccessResponse(String message) {}
}