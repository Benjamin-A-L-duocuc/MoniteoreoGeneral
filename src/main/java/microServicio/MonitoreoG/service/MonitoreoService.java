package microServicio.MonitoreoG.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.transaction.Transactional;
import microServicio.MonitoreoG.model.Alerta;
import microServicio.MonitoreoG.model.HealthCheck;
import microServicio.MonitoreoG.repository.AlertaRepository;
import microServicio.MonitoreoG.repository.HealthCheckRepository;

@Service
@Transactional
public class MonitoreoService {

    @Autowired
    private HealthCheckRepository healthCheckRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final Map<String, String> MS_URLS = new LinkedHashMap<>();
    static {
        MS_URLS.put("LOGIN", "http://localhost:8092");
        MS_URLS.put("REGISTRO_USUARIO", "http://localhost:8093");
        MS_URLS.put("INVENTARIO", "http://localhost:8094");
        MS_URLS.put("ENVIOS", "http://localhost:8084");
        MS_URLS.put("TIENDA_WEB", "http://localhost:8085");
        MS_URLS.put("SUCURSAL", "http://localhost:8086");
        MS_URLS.put("VENTAS", "http://localhost:8087");
        MS_URLS.put("PROVEEDORES", "http://localhost:8098");
    }

    @Scheduled(fixedRate = 60000)
    public void checkAllServices() {
        for (Map.Entry<String, String> entry : MS_URLS.entrySet()) {
            String nombre = entry.getKey();
            String url = entry.getValue() + "/actuator/health";
            checkService(nombre, url);
        }
    }

    private void checkService(String nombre, String url) {
        HealthCheck check = new HealthCheck();
        check.setOrigen(nombre);
        check.setFecha(new Date());

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response != null && response.contains("\"status\":\"UP\"")) {
                check.setEstado("ACTIVO");
                check.setMensaje(null);
            } else {
                check.setEstado("CAIDO");
                check.setMensaje("Respuesta inesperada: " + response);
            }
        } catch (Exception e) {
            check.setEstado("CAIDO");
            check.setMensaje(e.getMessage());
        }

        healthCheckRepository.save(check);

        if ("CAIDO".equals(check.getEstado())) {
            Optional<Alerta> ultima = alertaRepository
                    .findTopByLugarAndEstadoOrderByFechaDesc(nombre, "ACTIVA");
            if (ultima.isEmpty()) {
                Alerta alerta = new Alerta();
                alerta.setTitulo("MS " + nombre + " caído");
                alerta.setLugar(nombre);
                alerta.setDescripcion(check.getMensaje() != null ? check.getMensaje() : "Sin respuesta");
                alerta.setFecha(new Date());
                alerta.setEstado("ACTIVA");
                alertaRepository.save(alerta);
            }
        }
    }

    public Map<String, String> obtenerEstadoActual() {
        Map<String, String> estados = new LinkedHashMap<>();
        for (String nombre : MS_URLS.keySet()) {
            Optional<HealthCheck> ultimo = healthCheckRepository
                    .findTopByOrigenOrderByFechaDesc(nombre);
            estados.put(nombre, ultimo.map(HealthCheck::getEstado).orElse("DESCONOCIDO"));
        }
        return estados;
    }

    public Page<HealthCheck> obtenerHealthChecks(String origen, Date fechaInicio, Date fechaFin, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "fecha"));
        if (origen != null && fechaInicio != null && fechaFin != null) {
            return healthCheckRepository.findByOrigenAndFechaBetween(origen, fechaInicio, fechaFin, pageable);
        } else if (origen != null) {
            return healthCheckRepository.findByOrigen(origen, pageable);
        } else if (fechaInicio != null && fechaFin != null) {
            return healthCheckRepository.findByFechaBetween(fechaInicio, fechaFin, pageable);
        } else {
            return healthCheckRepository.findAll(pageable);
        }
    }

    public Page<Alerta> obtenerAlertas(String estado, Date fechaInicio, Date fechaFin, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "fecha"));
        if (estado != null && fechaInicio != null && fechaFin != null) {
            return alertaRepository.findByEstadoAndFechaBetween(estado, fechaInicio, fechaFin, pageable);
        } else if (estado != null) {
            return alertaRepository.findByEstado(estado, pageable);
        } else if (fechaInicio != null && fechaFin != null) {
            return alertaRepository.findByFechaBetween(fechaInicio, fechaFin, pageable);
        } else {
            return alertaRepository.findAll(pageable);
        }
    }

    public Alerta resolverAlerta(Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        if ("RESUELTA".equals(alerta.getEstado())) {
            throw new IllegalStateException("La alerta ya estaba resuelta");
        }
        alerta.setEstado("RESUELTA");
        return alertaRepository.save(alerta);
    }
}
