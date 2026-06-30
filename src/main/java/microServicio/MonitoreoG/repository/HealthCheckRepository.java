package microServicio.MonitoreoG.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microServicio.MonitoreoG.model.HealthCheck;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {

    List<HealthCheck> findTop1ByOrigenOrderByFechaDesc(String origen);

    Optional<HealthCheck> findTopByOrigenOrderByFechaDesc(String origen);

    Page<HealthCheck> findByOrigenAndFechaBetween(String origen, Date inicio, Date fin, Pageable pageable);

    Page<HealthCheck> findByOrigen(String origen, Pageable pageable);

    Page<HealthCheck> findByFechaBetween(Date inicio, Date fin, Pageable pageable);
}
