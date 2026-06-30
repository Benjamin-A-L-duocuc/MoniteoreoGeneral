package microServicio.MonitoreoG.controller;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import microServicio.MonitoreoG.model.Alerta;
import microServicio.MonitoreoG.model.HealthCheck;
import microServicio.MonitoreoG.service.MonitoreoService;

@RestController
@RequestMapping("/api/v1/monitoreo")
public class MonitoreoController {

    @Autowired
    private MonitoreoService service;

    @GetMapping("/estado")
    public ResponseEntity<Map<String, String>> obtenerEstado() {
        return ResponseEntity.ok(service.obtenerEstadoActual());
    }

    @GetMapping("/health")
    public ResponseEntity<Page<HealthCheck>> obtenerHealthChecks(
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFin,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return ResponseEntity.ok(service.obtenerHealthChecks(origen, fechaInicio, fechaFin, pagina, tamano));
    }

    @GetMapping("/alertas")
    public ResponseEntity<Page<Alerta>> obtenerAlertas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFin,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return ResponseEntity.ok(service.obtenerAlertas(estado, fechaInicio, fechaFin, pagina, tamano));
    }

    @PatchMapping("/alertas/{id}/resolver")
    public ResponseEntity<?> resolverAlerta(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.resolverAlerta(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
