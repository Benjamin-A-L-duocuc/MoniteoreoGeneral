# Monitoreo General — Centro de Control

## Que es

El "cuidador" del sistema. No vende libros ni gestiona inventario — su unico trabajo es **verificar que todos los demas microservicios esten funcionando**, registrar el historial de salud, y generar alertas cuando algo se cae. Piensa en el como el sistema de camaras de seguridad, pero para la infraestructura.

## Como monitorea

Cada **60 segundos**, el servicio hace un `GET` al endpoint `/actuator/health` de cada uno de los 8 microservicios:

| Servicio | Puerto |
|----------|--------|
| Login | 8092 |
| RegistroUsuarios | 8093 |
| Inventario | 8094 |
| Envios | 8084 |
| TiendaWeb | 8085 |
| Sucursal | 8086 |
| Ventas | 8087 |
| Proveedores | 8098 |

Si la respuesta contiene `"status":"UP"`, el servicio esta `ACTIVO`. Si no responde o da error, queda `CAIDO`.

## Sistema de alertas

Cuando un servicio cae por primera vez, se crea una alerta `ACTIVA` automaticamente. Si el servicio sigue caido en el proximo ciclo (60 segundos despues), **no se crea otra alerta** — se reutiliza la existente. Esto evita inundar el sistema de alertas repetidas.

Un operador puede **resolver** una alerta manualmente via `PATCH /alertas/{id}/resolver`. Si el servicio sigue caido despues de resolverla, se creara una nueva alerta en el proximo ciclo. Asi se genera un historial natural de incidentes.

## Dashboard

El endpoint `GET /api/v1/monitoreo/estado` devuelve un snapshot rapido del estado actual de todos los servicios. Ideal para un tablero en tiempo real que se actualiza periodicamente.

El endpoint `GET /api/v1/monitoreo/health` devuelve el historial completo de health checks con filtros por servicio y rango de fechas, con paginacion.

## Ejecutar

```cmd
cd MoniteoreoGeneral
.\mvnw.cmd spring-boot:run
```

Puerto: **8089** | DB: `monitoreo_ms`

> **Nota:** En `demo.bat` este servicio se lanza despues de los demas para evitar que intente monitorear servicios que aun no estan arrancados.

## Endpoints

| Metodo | Ruta | Que hace |
|--------|------|----------|
| GET | `/api/v1/monitoreo/estado` | Estado actual de todos los servicios |
| GET | `/api/v1/monitoreo/health` | Historial de health checks (filtros: origen, fechas, paginacion) |
| GET | `/api/v1/monitoreo/alertas` | Alertas (filtros: estado, fechas, paginacion) |
| PATCH | `/api/v1/monitoreo/alertas/{id}/resolver` | Resolver una alerta |
