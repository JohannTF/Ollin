package ipn.mx.isc.sismosapp.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sismos", indexes = {
    @Index(name = "idx_fecha_hora", columnList = "fechaHora"),
    @Index(name = "idx_magnitud", columnList = "magnitud")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sismo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private OffsetDateTime fechaHora;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(nullable = false)
    private Double magnitud;

    @Column(nullable = false)
    private Double profundidadKm;

    @Column(nullable = false, length = 500)
    private String lugar;

    @Column(nullable = false, length = 10)
    private String fuente = "SSN";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
