package ipn.mx.isc.sismosapp.backend.service;

import ipn.mx.isc.sismosapp.backend.model.dto.ReporteSismicoDTO;
import ipn.mx.isc.sismosapp.backend.model.entities.SismoH;
import ipn.mx.isc.sismosapp.backend.repository.SismoHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteSismicoService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteSismicoService.class);

    @Autowired
    private SismoHRepository sismoHRepository;

    /**
     * Genera reporte automático de los últimos 3 meses
     */
    public ReporteSismicoDTO generarReporteTrimestral() {
        LocalDateTime fechaFin = LocalDateTime.now();
        LocalDateTime fechaInicio = fechaFin.minusMonths(3);
        
        return generarReporte(fechaInicio, fechaFin, "TRIMESTRAL_AUTOMATICO");
    }

    /**
     * Genera reporte automático de los últimos 6 meses
     */
    public ReporteSismicoDTO generarReporteSemestral() {
        LocalDateTime fechaFin = LocalDateTime.now();
        LocalDateTime fechaInicio = fechaFin.minusMonths(6);
        
        return generarReporte(fechaInicio, fechaFin, "SEMESTRAL_AUTOMATICO");
    }

    /**
     * Genera reporte automático del último año
     */
    public ReporteSismicoDTO generarReporteAnual() {
        LocalDateTime fechaFin = LocalDateTime.now();
        LocalDateTime fechaInicio = fechaFin.minusYears(1);
        
        return generarReporte(fechaInicio, fechaFin, "ANUAL_AUTOMATICO");
    }

    /**
     * Genera reporte personalizado por rango de fechas
     */
    public ReporteSismicoDTO generarReportePersonalizado(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        return generarReporte(fechaInicio, fechaFin, "PERSONALIZADO");
    }

    /**
     * Método principal que genera el reporte con todas las estadísticas
     */
    private ReporteSismicoDTO generarReporte(LocalDateTime fechaInicio, LocalDateTime fechaFin, String tipo) {
        logger.info("Generando reporte {} desde {} hasta {}", tipo, fechaInicio, fechaFin);
        
        // Convertir a OffsetDateTime para la consulta
        OffsetDateTime odtInicio = fechaInicio.atOffset(ZoneOffset.of("-06:00"));
        OffsetDateTime odtFin = fechaFin.atOffset(ZoneOffset.of("-06:00"));
        
        // Obtener todos los sismos del periodo
        List<SismoH> sismos = sismoHRepository.findAll().stream()
            .filter(s -> !s.getFechaHora().isBefore(odtInicio) && !s.getFechaHora().isAfter(odtFin))
            .collect(Collectors.toList());
        
        if (sismos.isEmpty()) {
            logger.warn("No se encontraron sismos en el periodo especificado");
            return crearReporteVacio(fechaInicio, fechaFin, tipo);
        }

        ReporteSismicoDTO reporte = new ReporteSismicoDTO();
        reporte.setTipoReporte(tipo);
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setTotalSismos((long) sismos.size());

        // Calcular estadísticas de magnitud
        DoubleSummaryStatistics magnitudStats = sismos.stream()
            .mapToDouble(SismoH::getMagnitud)
            .summaryStatistics();
        
        reporte.setMagnitudPromedio(magnitudStats.getAverage());
        reporte.setMagnitudMaxima(magnitudStats.getMax());
        reporte.setMagnitudMinima(magnitudStats.getMin());

        // Calcular estadísticas de profundidad
        DoubleSummaryStatistics profundidadStats = sismos.stream()
            .mapToDouble(SismoH::getProfundidad)
            .summaryStatistics();
        
        reporte.setProfundidadPromedio(profundidadStats.getAverage());
        reporte.setProfundidadMaxima(profundidadStats.getMax());
        reporte.setProfundidadMinima(profundidadStats.getMin());

        // Distribución por rango de magnitud
        reporte.setDistribucionPorMagnitud(calcularDistribucionPorMagnitud(sismos));

        // Distribución por estado
        reporte.setDistribucionPorEstado(calcularDistribucionPorEstado(sismos));

        // Distribución por mes
        reporte.setDistribucionPorMes(calcularDistribucionPorMes(sismos));

        // Top 10 sismos más fuertes
        reporte.setSismosMasFuertes(obtenerSismosMasFuertes(sismos, 10));

        // Sismos por día (para gráficas temporales)
        reporte.setSismosPorDia(calcularSismosPorDia(sismos));

        logger.info("Reporte generado exitosamente con {} sismos", sismos.size());
        return reporte;
    }

    private ReporteSismicoDTO crearReporteVacio(LocalDateTime inicio, LocalDateTime fin, String tipo) {
        ReporteSismicoDTO reporte = new ReporteSismicoDTO();
        reporte.setTipoReporte(tipo);
        reporte.setFechaInicio(inicio);
        reporte.setFechaFin(fin);
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setTotalSismos(0L);
        reporte.setMagnitudPromedio(0.0);
        reporte.setDistribucionPorMagnitud(new HashMap<>());
        reporte.setDistribucionPorEstado(new HashMap<>());
        reporte.setDistribucionPorMes(new HashMap<>());
        reporte.setSismosMasFuertes(new ArrayList<>());
        reporte.setSismosPorDia(new HashMap<>());
        return reporte;
    }

    private Map<String, Long> calcularDistribucionPorMagnitud(List<SismoH> sismos) {
        Map<String, Long> distribucion = new LinkedHashMap<>();
        
        distribucion.put("< 3.0", sismos.stream().filter(s -> s.getMagnitud() < 3.0).count());
        distribucion.put("3.0 - 3.9", sismos.stream().filter(s -> s.getMagnitud() >= 3.0 && s.getMagnitud() < 4.0).count());
        distribucion.put("4.0 - 4.9", sismos.stream().filter(s -> s.getMagnitud() >= 4.0 && s.getMagnitud() < 5.0).count());
        distribucion.put("5.0 - 5.9", sismos.stream().filter(s -> s.getMagnitud() >= 5.0 && s.getMagnitud() < 6.0).count());
        distribucion.put("6.0 - 6.9", sismos.stream().filter(s -> s.getMagnitud() >= 6.0 && s.getMagnitud() < 7.0).count());
        distribucion.put(">= 7.0", sismos.stream().filter(s -> s.getMagnitud() >= 7.0).count());
        
        return distribucion;
    }

    private Map<String, Long> calcularDistribucionPorEstado(List<SismoH> sismos) {
        return sismos.stream()
            .map(this::extraerEstado)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                estado -> estado,
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(15) // Top 15 estados
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    private String extraerEstado(SismoH sismo) {
        String ubicacion = sismo.getReferenciaLocalizacion();
        if (ubicacion == null || ubicacion.isEmpty()) {
            return "DESCONOCIDO";
        }
        
        // Extrae el estado que aparece al final después de la última coma
        int ultimaComa = ubicacion.lastIndexOf(',');
        if (ultimaComa != -1 && ultimaComa < ubicacion.length() - 1) {
            return ubicacion.substring(ultimaComa + 1).trim();
        }
        
        return "DESCONOCIDO";
    }

    private Map<String, Long> calcularDistribucionPorMes(List<SismoH> sismos) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        return sismos.stream()
            .collect(Collectors.groupingBy(
                s -> s.getFechaHora().format(formatter),
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    private List<ReporteSismicoDTO.SismoResumenDTO> obtenerSismosMasFuertes(List<SismoH> sismos, int limite) {
        return sismos.stream()
            .sorted(Comparator.comparingDouble(SismoH::getMagnitud).reversed())
            .limit(limite)
            .map(s -> new ReporteSismicoDTO.SismoResumenDTO(
                s.getFecha(),
                s.getHora(),
                s.getMagnitud(),
                s.getReferenciaLocalizacion(),
                s.getProfundidad()
            ))
            .collect(Collectors.toList());
    }

    private Map<String, Long> calcularSismosPorDia(List<SismoH> sismos) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        return sismos.stream()
            .collect(Collectors.groupingBy(
                s -> s.getFechaHora().format(formatter),
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
}