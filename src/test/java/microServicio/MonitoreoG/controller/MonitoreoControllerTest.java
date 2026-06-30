package microServicio.MonitoreoG.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import microServicio.MonitoreoG.model.Alerta;
import microServicio.MonitoreoG.model.HealthCheck;
import microServicio.MonitoreoG.service.MonitoreoService;

@WebMvcTest(MonitoreoController.class)
class MonitoreoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MonitoreoService service;

    @Test
    void testGetEstado() throws Exception {
        when(service.obtenerEstadoActual())
                .thenReturn(Map.of("LOGIN", "ACTIVO", "ENVIOS", "CAIDO"));

        mockMvc.perform(get("/api/v1/monitoreo/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.LOGIN").value("ACTIVO"))
                .andExpect(jsonPath("$.ENVIOS").value("CAIDO"));
    }

    @Test
    void testGetHealthChecks_WithoutParams() throws Exception {
        when(service.obtenerHealthChecks(isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/monitoreo/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void testGetHealthChecks_WithParams() throws Exception {
        Page<HealthCheck> page = new PageImpl<>(List.of(
                new HealthCheck(1L, "LOGIN", "ACTIVO", new Date(), null)));
        when(service.obtenerHealthChecks(eq("LOGIN"), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/monitoreo/health")
                        .param("origen", "LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].origen").value("LOGIN"))
                .andExpect(jsonPath("$.content[0].estado").value("ACTIVO"));
    }

    @Test
    void testGetAlertas_WithoutParams() throws Exception {
        when(service.obtenerAlertas(isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/monitoreo/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void testGetAlertas_WithParams() throws Exception {
        Page<Alerta> page = new PageImpl<>(List.of(
                new Alerta(1L, "MS LOGIN ca\u00eddo", "LOGIN",
                        "error", new Date(), "ACTIVA")));
        when(service.obtenerAlertas(eq("ACTIVA"), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/monitoreo/alertas")
                        .param("estado", "ACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].estado").value("ACTIVA"))
                .andExpect(jsonPath("$.content[0].titulo").value("MS LOGIN ca\u00eddo"));
    }

    @Test
    void testResolverAlerta_Success() throws Exception {
        Alerta alerta = new Alerta(1L, "MS LOGIN ca\u00eddo", "LOGIN",
                "error", new Date(), "RESUELTA");
        when(service.resolverAlerta(1L)).thenReturn(alerta);

        mockMvc.perform(patch("/api/v1/monitoreo/alertas/1/resolver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTA"));
    }

    @Test
    void testResolverAlerta_NotFound() throws Exception {
        when(service.resolverAlerta(99L))
                .thenThrow(new RuntimeException("Alerta no encontrada"));

        mockMvc.perform(patch("/api/v1/monitoreo/alertas/99/resolver"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testResolverAlerta_AlreadyResolved() throws Exception {
        when(service.resolverAlerta(1L))
                .thenThrow(new IllegalStateException("La alerta ya estaba resuelta"));

        mockMvc.perform(patch("/api/v1/monitoreo/alertas/1/resolver"))
                .andExpect(status().isConflict())
                .andExpect(content().string("La alerta ya estaba resuelta"));
    }
}
