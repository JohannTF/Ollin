package ipn.mx.isc.sismosapp.backend.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import ipn.mx.isc.sismosapp.backend.model.entities.SismoH;
import ipn.mx.isc.sismosapp.backend.repository.SismoHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CsvUploadService {

    private static final Logger logger = LoggerFactory.getLogger(CsvUploadService.class);
    private static final int BATCH_SIZE = 5000; // Aumentado para mayor velocidad

    @Autowired
    private SismoHRepository sismoHRepository;

    /**
     * Procesa el CSV de forma asíncrona para no bloquear
     */
    public CompletableFuture<String> procesarCsvAsync(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return procesarCsv(file);
            } catch (Exception e) {
                logger.error("Error procesando CSV: {}", e.getMessage(), e);
                return "Error: " + e.getMessage();
            }
        });
    }

    @Transactional
    public String procesarCsv(MultipartFile file) throws IOException, CsvException {
        logger.info("Iniciando procesamiento de CSV: {} ({} bytes)", 
            file.getOriginalFilename(), file.getSize());

        long startTime = System.currentTimeMillis();
        List<SismoH> batch = new ArrayList<>(BATCH_SIZE);
        int lineasProcesadas = 0;
        int lineasOmitidas = 0;
        int duplicados = 0;
        int totalGuardados = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> records = csvReader.readAll();
            logger.info("Total de líneas leídas: {}", records.size());

            // Saltar las primeras 4 líneas de cabecera del SSN
            for (int i = 4; i < records.size(); i++) {
                String[] row = records.get(i);

                try {
                    if (row.length < 10) {
                        lineasOmitidas++;
                        continue;
                    }

                    SismoH sismo = new SismoH();
                    sismo.setFecha(row[0].trim());
                    sismo.setHora(row[1].trim());
                    sismo.setMagnitud(Double.parseDouble(row[2].trim()));
                    sismo.setLatitud(Double.parseDouble(row[3].trim()));
                    sismo.setLongitud(Double.parseDouble(row[4].trim()));
                    sismo.setProfundidad(Double.parseDouble(row[5].trim()));
                    sismo.setReferenciaLocalizacion(row[6].trim());
                    sismo.setFechaUtc(row[7].trim());
                    sismo.setHoraUtc(row[8].trim());
                    sismo.setEstatus(row[9].trim());

                    // Convertir a OffsetDateTime (zona horaria de México)
                    String fechaHoraStr = row[0].trim() + "T" + row[1].trim() + "-06:00";
                    OffsetDateTime fechaHora = OffsetDateTime.parse(fechaHoraStr);
                    sismo.setFechaHora(fechaHora);

                    batch.add(sismo);
                    lineasProcesadas++;

                    // Guardar por lotes grandes para máxima velocidad
                    if (batch.size() >= BATCH_SIZE) {
                        int guardados = guardarBatch(batch);
                        totalGuardados += guardados;
                        duplicados += (batch.size() - guardados);
                        batch.clear();
                        
                        if (lineasProcesadas % 50000 == 0) {
                            logger.info("Progreso: {} líneas procesadas, {} guardadas", 
                                lineasProcesadas, totalGuardados);
                        }
                    }

                } catch (Exception e) {
                    logger.warn("Error en línea {}: {}", i, e.getMessage());
                    lineasOmitidas++;
                }
            }

            // Guardar los registros restantes
            if (!batch.isEmpty()) {
                int guardados = guardarBatch(batch);
                totalGuardados += guardados;
                duplicados += (batch.size() - guardados);
                batch.clear();
            }
        }

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        String mensaje = String.format(
            "CSV procesado en %d segundos. Procesadas: %d, Guardadas: %d, Omitidas: %d, Duplicados: %d",
            duration, lineasProcesadas, totalGuardados, lineasOmitidas, duplicados
        );

        logger.info(mensaje);
        return mensaje;
    }

    private int guardarBatch(List<SismoH> batch) {
        try {
            List<SismoH> guardados = sismoHRepository.saveAll(batch);
            return guardados.size();
        } catch (Exception e) {
            logger.error("Error guardando batch: {}", e.getMessage());
            // Intentar guardar uno por uno en caso de error
            int count = 0;
            for (SismoH sismo : batch) {
                try {
                    sismoHRepository.save(sismo);
                    count++;
                } catch (Exception ex) {
                    // Ignorar duplicados
                }
            }
            return count;
        }
    }
}