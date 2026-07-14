# Monitoreo General

Supervision del estado de todos los microservicios. Ejecuta tareas programadas que consultan el health check de cada servicio y genera alertas automaticas.

## Puerto

**8089** | DB: `monitoreo_ms`

Usa `@EnableScheduling` para polling periodico de salud del sistema.

## Endpoints

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | `/api/v1/monitoreo/estado` | Estado actual de todos los servicios |
| GET | `/api/v1/monitoreo/health` | Historial de health checks (paginado, filtros por fecha) |
| GET | `/api/v1/monitoreo/alertas` | Alertas (paginado, filtros por estado/fecha) |
| PATCH | `/api/v1/monitoreo/alertas/{id}/resolver` | Marcar alerta como resuelta |

## Ejecucion

```cmd
cd MoniteoreoGeneral
.\mvnw.cmd spring-boot:run
```

**Nota:** Este servicio se lanza despues de los demas en `demo.bat` para evitar que intente monitorear servicios que aun no estan arrancados.

## Dependencias de repositorios

Este servicio depende de que los demas microservicios esten corriendo para poder monitorear su estado. Ver repositorios fuente en el README original de este directorio.
