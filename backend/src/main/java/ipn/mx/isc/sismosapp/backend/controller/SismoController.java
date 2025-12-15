package ipn.mx.isc.sismosapp.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipn.mx.isc.sismosapp.backend.model.dto.SismoDTO;
import ipn.mx.isc.sismosapp.backend.model.dto.SismoFilterDTO;
import ipn.mx.isc.sismosapp.backend.model.requests.SismoRequest;
import ipn.mx.isc.sismosapp.backend.service.SismoService;
import ipn.mx.isc.sismosapp.backend.service.SseEmitterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/sismos")
@Tag(name = "Sismos", description = "API para gestión y consulta de sismos en tiempo real")
public class SismoController {

    @Autowired
    private SismoService sismoService;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Operation(
        summary = "Obtener sismos con paginación",
        description = "Retorna una lista de sismos ordenados por fecha descendente. Usa cache Redis para la primera página."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de sismos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SismoDTO.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<SismoDTO>> obtenerTodosLosSismos(
        @Parameter(description = "Número de página (inicia en 0)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Cantidad de sismos por página", example = "100")
        @RequestParam(defaultValue = "100") int size
    ) {
        List<SismoDTO> sismos = sismoService.obtenerTodosLosSismos(page, size);
        return ResponseEntity.ok(sismos);
    }

    @Operation(
        summary = "Stream de sismos en tiempo real",
        description = "Establece una conexión SSE para recibir notificaciones de nuevos sismos en tiempo real"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Conexión SSE establecida exitosamente",
            content = @Content(mediaType = "text/event-stream")
        )
    })
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSismos() {
        return sseEmitterService.crearEmitter();
    }

    @Operation(
        summary = "Filtrar sismos con criterios dinámicos",
        description = "Permite filtrar sismos combinando múltiples criterios opcionales: magnitud, fechas, estado, profundidad. " +
                      "Si no se envían filtros, devuelve los 100 sismos más recientes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sismos filtrados exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SismoDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de filtrado inválidos (ej: rangos incoherentes)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/filtrar")
    public ResponseEntity<?> filtrarSismos(@RequestBody SismoFilterDTO filtros) {
        try {
            List<SismoDTO> sismos = sismoService.filtrarSismos(filtros);
            return ResponseEntity.ok(sismos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * DTO para respuestas de error
     */
    private record ErrorResponse(String mensaje) {}

    @Operation(
        summary = "Crear sismo manual",
        description = "Permite registrar un sismo inventado con fuente distinta a SSN"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sismo creado"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @PostMapping("/manual")
    public ResponseEntity<?> crearSismoManual(@Valid @RequestBody SismoRequest request) {
        try {
            SismoDTO dto = sismoService.crearSismoManual(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @Operation(
        summary = "Eliminar sismo por id",
        description = "Elimina un sismo registrado manualmente u obtenido del scraper"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Sismo eliminado"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSismo(@PathVariable String id) {
        boolean eliminado = sismoService.eliminarSismo(id);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
