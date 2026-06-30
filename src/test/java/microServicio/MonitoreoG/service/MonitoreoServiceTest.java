package microServicio.MonitoreoG.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import microServicio.MonitoreoG.model.Alerta;
import microServicio.MonitoreoG.model.HealthCheck;
import microServicio.MonitoreoG.repository.AlertaRepository;
import microServicio.MonitoreoG.repository.HealthCheckRepository;

@ExtendWith(MockitoExtension.class)
class MonitoreoServiceTest {

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MonitoreoService service;

    @Captor
    private ArgumentCaptor<HealthCheck> healthCheckCaptor;

    @Captor
    private ArgumentCaptor<Alerta> alertaCaptor;

    private static final List<String> MS_NAMES = List.of(
            "LOGIN", "REGISTRO_USUARIO", "INVENTARIO", "ENVIOS",
            "TIENDA_WEB", "SUCURSAL", "VENTAS", "PROVEEDORES");

    @Test
    void testCheckAllServices_Success() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{\"status\":\"UP\"}");
        when(healthCheckRepository.save(any(HealthCheck.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.checkAllServices();

        verify(healthCheckRepository, times(8)).save(healthCheckCaptor.capture());
        assertThat(healthCheckCaptor.getAllValues())
                .hasSize(8)
                .allSatisfy(hc -> {
                    assertThat(hc.getEstado()).isEqualTo("ACTIVO");
                    assertThat(hc.getMensaje()).isNull();
                    assertThat(MS_NAMES).contains(hc.getOrigen());
                });
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void testCheckAllServices_Failure() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));
        when(healthCheckRepository.save(any(HealthCheck.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(alertaRepository.findTopByLugarAndEstadoOrderByFechaDesc(anyString(), eq("ACTIVA")))
                .thenReturn(Optional.empty());
        when(alertaRepository.save(any(Alerta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.checkAllServices();

        verify(healthCheckRepository, times(8)).save(healthCheckCaptor.capture());
        assertThat(healthCheckCaptor.getAllValues())
                .hasSize(8)
                .allSatisfy(hc -> {
                    assertThat(hc.getEstado()).isEqualTo("CAIDO");
                    assertThat(hc.getMensaje()).isEqualTo("Connection refused");
                });

        verify(alertaRepository, times(8)).save(alertaCaptor.capture());
        assertThat(alertaCaptor.getAllValues())
                .hasSize(8)
                .allSatisfy(a -> {
                    assertThat(a.getEstado()).isEqualTo("ACTIVA");
                    assertThat(a.getTitulo()).startsWith("MS ");
                    assertThat(a.getTitulo()).endsWith(" ca\u00eddo");
                    assertThat(MS_NAMES).contains(a.getLugar());
                });
    }

    @Test
    void testCheckAllServices_Failure_AlertaYaActiva() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));
        when(healthCheckRepository.save(any(HealthCheck.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Alerta existing = new Alerta(1L, "MS LOGIN ca\u00eddo", "LOGIN",
                "error", new Date(), "ACTIVA");
        when(alertaRepository.findTopByLugarAndEstadoOrderByFechaDesc(anyString(), eq("ACTIVA")))
                .thenReturn(Optional.of(existing));

        service.checkAllServices();

        verify(healthCheckRepository, times(8)).save(any(HealthCheck.class));
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void testObtenerEstadoActual() {
        for (int i = 0; i < MS_NAMES.size(); i++) {
            String name = MS_NAMES.get(i);
            HealthCheck hc = new HealthCheck((long) i + 1, name, "ACTIVO", new Date(), null);
            when(healthCheckRepository.findTopByOrigenOrderByFechaDesc(name))
                    .thenReturn(Optional.of(hc));
        }

        Map<String, String> estado = service.obtenerEstadoActual();

        assertThat(estado).hasSize(8);
        assertThat(estado.keySet()).containsAll(MS_NAMES);
        assertThat(estado).allSatisfy((key, value) -> assertThat(value).isEqualTo("ACTIVO"));
    }

    @Test
    void testObtenerHealthChecks_WithoutFilters() {
        when(healthCheckRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<HealthCheck> result = service.obtenerHealthChecks(null, null, null, 0, 20);

        assertThat(result).isEmpty();
        verify(healthCheckRepository).findAll(any(Pageable.class));
    }

    @Test
    void testObtenerHealthChecks_WithOrigenFilter() {
        Page<HealthCheck> page = new PageImpl<>(List.of(
                new HealthCheck(1L, "LOGIN", "ACTIVO", new Date(), null)));
        when(healthCheckRepository.findByOrigen(eq("LOGIN"), any(Pageable.class)))
                .thenReturn(page);

        Page<HealthCheck> result = service.obtenerHealthChecks("LOGIN", null, null, 0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getOrigen()).isEqualTo("LOGIN");
    }

    @Test
    void testObtenerHealthChecks_WithFechaFilters() {
        Date inicio = new Date(1000);
        Date fin = new Date(2000);
        when(healthCheckRepository.findByFechaBetween(eq(inicio), eq(fin), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<HealthCheck> result = service.obtenerHealthChecks(null, inicio, fin, 0, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void testObtenerAlertas_WithoutFilters() {
        when(alertaRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Alerta> result = service.obtenerAlertas(null, null, null, 0, 20);

        assertThat(result).isEmpty();
        verify(alertaRepository).findAll(any(Pageable.class));
    }

    @Test
    void testObtenerAlertas_WithEstadoFilter() {
        Page<Alerta> page = new PageImpl<>(List.of(
                new Alerta(1L, "MS LOGIN ca\u00eddo", "LOGIN", "error", new Date(), "ACTIVA")));
        when(alertaRepository.findByEstado(eq("ACTIVA"), any(Pageable.class)))
                .thenReturn(page);

        Page<Alerta> result = service.obtenerAlertas("ACTIVA", null, null, 0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getEstado()).isEqualTo("ACTIVA");
    }

    @Test
    void testObtenerAlertas_WithFechaFilters() {
        Date inicio = new Date(1000);
        Date fin = new Date(2000);
        when(alertaRepository.findByFechaBetween(eq(inicio), eq(fin), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Alerta> result = service.obtenerAlertas(null, inicio, fin, 0, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void testResolverAlerta_Success() {
        Alerta alerta = new Alerta(1L, "MS LOGIN ca\u00eddo", "LOGIN",
                "error", new Date(), "ACTIVA");
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(alerta));
        when(alertaRepository.save(any(Alerta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Alerta result = service.resolverAlerta(1L);

        assertThat(result.getEstado()).isEqualTo("RESUELTA");
        verify(alertaRepository).save(alerta);
    }

    @Test
    void testResolverAlerta_AlreadyResolved() {
        Alerta alerta = new Alerta(1L, "MS LOGIN ca\u00eddo", "LOGIN",
                "error", new Date(), "RESUELTA");
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(alerta));

        assertThatThrownBy(() -> service.resolverAlerta(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La alerta ya estaba resuelta");

        verify(alertaRepository, never()).save(any(Alerta.class));
    }
}
