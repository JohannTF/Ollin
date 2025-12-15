package ipn.mx.isc.sismosapp.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ipn.mx.isc.sismosapp.backend.model.requests.DeviceTokenRequest;
import ipn.mx.isc.sismosapp.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registrar(@RequestBody DeviceTokenRequest request) {
        notificationService.registrarToken(request.getToken(), request.getPlatform());
        return ResponseEntity.ok().build();
    }
}
