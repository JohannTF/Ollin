package ipn.mx.isc.sismosapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ipn.mx.isc.sismosapp.backend.model.entities.SismoH;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface SismoHRepository extends JpaRepository<SismoH, String> {

    Optional<SismoH> findByFechaHoraAndLatitudAndLongitudAndMagnitud(
        OffsetDateTime fechaHora,
        Double latitud,
        Double longitud,
        Double magnitud
    );
}