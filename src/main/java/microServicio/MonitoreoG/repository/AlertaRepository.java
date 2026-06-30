package microServicio.MonitoreoG.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microServicio.MonitoreoG.model.Alerta;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByLugarAndEstadoOrderByFechaDesc(String lugar, String estado);

    Optional<Alerta> findTopByLugarAndEstadoOrderByFechaDesc(String lugar, String estado);

    Page<Alerta> findByEstado(String estado, Pageable pageable);

    Page<Alerta> findByEstadoAndFechaBetween(String estado, Date inicio, Date fin, Pageable pageable);

    Page<Alerta> findByFechaBetween(Date inicio, Date fin, Pageable pageable);
}
