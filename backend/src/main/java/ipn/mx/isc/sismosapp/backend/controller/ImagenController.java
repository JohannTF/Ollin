package ipn.mx.isc.sismosapp.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes")
@CrossOrigin(origins = "*")
public class ImagenController {

    /**
     * Endpoint para convertir una imagen a Base64
     * Acepta JPG, PNG, GIF
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImagen(@RequestParam("file") MultipartFile file) {
        try {
            // Validar que sea una imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El archivo debe ser una imagen (JPG, PNG, GIF)");
                return ResponseEntity.badRequest().body(error);
            }

            // Validar tamaño (máximo 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La imagen no debe superar 5MB");
                return ResponseEntity.badRequest().body(error);
            }

            // Convertir a Base64
            byte[] bytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            
            // Crear data URI completo
            String dataUri = "data:" + contentType + ";base64," + base64Image;

            Map<String, String> response = new HashMap<>();
            response.put("imagenUrl", dataUri);
            response.put("mensaje", "Imagen procesada exitosamente");
            response.put("size", String.valueOf(file.getSize()));
            response.put("type", contentType);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al procesar la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint para validar si una URL Base64 es válida
     */
    @PostMapping("/validar")
    public ResponseEntity<Map<String, Object>> validarImagen(@RequestBody Map<String, String> request) {
        String imagenUrl = request.get("imagenUrl");
        
        Map<String, Object> response = new HashMap<>();
        
        if (imagenUrl == null || imagenUrl.isEmpty()) {
            response.put("valida", false);
            response.put("mensaje", "URL de imagen vacía");
            return ResponseEntity.ok(response);
        }

        // Validar formato Data URI
        if (imagenUrl.startsWith("data:image/")) {
            response.put("valida", true);
            response.put("mensaje", "Imagen válida");
            
            // Extraer información
            try {
                String[] parts = imagenUrl.split(",");
                if (parts.length == 2) {
                    String header = parts[0];
                    String base64Data = parts[1];
                    
                    response.put("tipo", header.split(";")[0].replace("data:", ""));
                    response.put("tamaño", base64Data.length());
                }
            } catch (Exception e) {
                response.put("info", "No se pudo extraer información adicional");
            }
            
            return ResponseEntity.ok(response);
        } else {
            response.put("valida", false);
            response.put("mensaje", "Formato de imagen no válido. Debe ser data URI (data:image/...)");
            return ResponseEntity.ok(response);
        }
    }
}