package ipn.mx.isc.sismosapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import ipn.mx.isc.sismosapp.backend.model.entities.Sismo;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface SismoRepository extends JpaRepository<Sismo, String>, JpaSpecificationExecutor<Sismo> {

    Optional<Sismo> findByFechaHoraAndLatitudAndLongitudAndMagnitud(
        OffsetDateTime fechaHora,
        Double latitud,
        Double longitud,
        Double magnitud
    );
}
