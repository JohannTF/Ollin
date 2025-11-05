package ipn.mx.isc.sismosapp.backend.controller;

import ipn.mx.isc.sismosapp.backend.dto.SismoDTO;
import ipn.mx.isc.sismosapp.backend.service.SismoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sismos")
// @CrossOrigin(origins = "*")
public class SismoController {

    @Autowired
    private SismoService sismoService;

    @GetMapping
    public ResponseEntity<List<SismoDTO>> obtenerTodosLosSismos(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size
    ) {
        List<SismoDTO> sismos = sismoService.obtenerTodosLosSismos(page, size);
        return ResponseEntity.ok(sismos);
    }

    @GetMapping("/recientes")
    public ResponseEntity<List<SismoDTO>> obtenerSismosRecientes(
        @RequestParam(defaultValue = "24") int horas
    ) {
        List<SismoDTO> sismos = sismoService.obtenerSismosRecientes(horas);
        return ResponseEntity.ok(sismos);
    }

    @GetMapping("/magnitud")
    public ResponseEntity<List<SismoDTO>> obtenerSismosPorMagnitud(
        @RequestParam(defaultValue = "3.0") Double minima
    ) {
        List<SismoDTO> sismos = sismoService.obtenerSismosPorMagnitud(minima);
        return ResponseEntity.ok(sismos);
    }
}
