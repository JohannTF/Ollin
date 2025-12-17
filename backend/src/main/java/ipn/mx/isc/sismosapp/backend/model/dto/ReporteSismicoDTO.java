package ipn.mx.isc.sismosapp.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte estadístico de actividad sísmica")
public class ReporteSismicoDTO {
    
    @Schema(description = "Tipo de reporte", example = "TRIMESTRAL")
    private String tipoReporte;
    
    @Schema(description = "Fecha de inicio del periodo", example = "2024-09-12T00:00:00")
    private LocalDateTime fechaInicio;
    
    @Schema(description = "Fecha de fin del periodo", example = "2024-12-12T23:59:59")
    private LocalDateTime fechaFin;
    
    @Schema(description = "Fecha en que se generó el reporte", example = "2024-12-12T21:30:00")
    private LocalDateTime fechaGeneracion;
    
    @Schema(description = "Total de sismos en el periodo")
    private Long totalSismos;
    
    @Schema(description = "Magnitud promedio")
    private Double magnitudPromedio;
    
    @Schema(description = "Magnitud máxima registrada")
    private Double magnitudMaxima;
    
    @Schema(description = "Magnitud mínima registrada")
    private Double magnitudMinima;
    
    @Schema(description = "Profundidad promedio en km")
    private Double profundidadPromedio;
    
    @Schema(description = "Profundidad máxima en km")
    private Double profundidadMaxima;
    
    @Schema(description = "Profundidad mínima en km")
    private Double profundidadMinima;
    
    @Schema(description = "Distribución de sismos por rango de magnitud")
    private Map<String, Long> distribucionPorMagnitud;
    
    @Schema(description = "Distribución de sismos por estado")
    private Map<String, Long> distribucionPorEstado;
    
    @Schema(description = "Distribución de sismos por mes")
    private Map<String, Long> distribucionPorMes;
    
    @Schema(description = "Top 10 sismos más fuertes del periodo")
    private List<SismoResumenDTO> sismosMasFuertes;
    
    @Schema(description = "Sismos por día (para gráficas)")
    private Map<String, Long> sismosPorDia;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SismoResumenDTO {
        private String fecha;
        private String hora;
        private Double magnitud;
        private String ubicacion;
        private Double profundidad;
    }
}