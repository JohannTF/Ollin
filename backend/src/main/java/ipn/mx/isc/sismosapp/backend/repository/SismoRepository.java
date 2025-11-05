package ipn.mx.isc.sismosapp.backend.repository;

import ipn.mx.isc.sismosapp.backend.model.Sismo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SismoRepository extends JpaRepository<Sismo, String> {

    Optional<Sismo> findByFechaHoraAndLatitudAndLongitudAndMagnitud(
        OffsetDateTime fechaHora,
        Double latitud,
        Double longitud,
        Double magnitud
    );

    List<Sismo> findAllByOrderByFechaHoraDesc();

    @Query("SELECT s FROM Sismo s WHERE s.fechaHora >= :fechaInicio ORDER BY s.fechaHora DESC")
    List<Sismo> findRecentSismos(@Param("fechaInicio") OffsetDateTime fechaInicio);

    @Query("SELECT s FROM Sismo s WHERE s.magnitud >= :magnitudMinima ORDER BY s.fechaHora DESC")
    List<Sismo> findByMagnitudMinima(@Param("magnitudMinima") Double magnitudMinima);
}
