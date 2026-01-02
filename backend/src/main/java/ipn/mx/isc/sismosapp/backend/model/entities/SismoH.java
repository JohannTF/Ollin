package ipn.mx.isc.sismosapp.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sismosh", indexes = {
    @Index(name = "idx_sismoh_fecha_hora", columnList = "fechaHora"),
    @Index(name = "idx_sismoh_magnitud", columnList = "magnitud"),
    @Index(name = "idx_sismoh_fecha_utc", columnList = "fechaUtc")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SismoH {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String fecha;

    @Column(nullable = false)
    private String hora;

    @Column(nullable = false)
    private Double magnitud;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(nullable = false)
    private Double profundidad;

    @Column(nullable = false, length = 500)
    private String referenciaLocalizacion;

    @Column(nullable = false)
    private String fechaUtc;

    @Column(nullable = false)
    private String horaUtc;

    @Column(nullable = false, length = 50)
    private String estatus;

    @Column(nullable = false)
    private OffsetDateTime fechaHora;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}