# Logix OptiFlow — App móvil (Android)

Cliente Android de OptiFlow para pacientes y personal de ópticas. Consume la API REST del backend desplegado en Render o un servidor local.

## Funcionalidades por rol

| Rol | Qué puede hacer en la app |
|-----|---------------------------|
| **Paciente** | Buscar ópticas, guardar favoritas, reservar y ver citas, ver recetas, revisar pedidos y su estado, pagar con Mercado Pago, calificar ópticas y recibir notificaciones |
| **Asesor de ventas** | Armar cotizaciones, registrar ventas y pagos en tienda, consultar inventario |
| **Optometrista** | Registrar ficha clínica, historia y receta del paciente |
| **Técnico** | Gestionar órdenes de trabajo del laboratorio |
| **Administrador** | Todo lo anterior, más alertas de stock, reportes, personal, proveedores y horarios |

Flujo principal: horario → cita → ficha clínica y receta → cotización → venta → pago → orden de trabajo → entrega, con notificaciones push en cada cambio.

## Backend

| Entorno | Base URL |
|---------|----------|
| **Producción (Render)** | `https://logix-optiflow-back-end.onrender.com/api/v1/` |
| **Local (emulador)** | `http://10.0.2.2:8080/api/v1/` |

- **Swagger UI:** [https://logix-optiflow-back-end.onrender.com/swagger-ui/index.html](https://logix-optiflow-back-end.onrender.com/swagger-ui/index.html)
- Render en plan free puede dormir el servicio: la primera petición puede tardar 1–2 minutos. Abre Swagger en el navegador para despertarlo.

Para comprobar que el backend responde (no lo levanta):

```powershell
.\scripts\check-backend.ps1 -Target prod
.\scripts\check-backend.ps1 -Target local
```

## Ejecutar la app

1. Abre el proyecto en **Android Studio** y sincroniza Gradle.
2. En **Build Variants** elige:
   - **`prodDebug`**: API en Render.
   - **`localDebug`**: backend corriendo en tu PC en el puerto 8080.
3. **Run** en emulador o dispositivo.

Opcional en `local.properties` (no se sube a git): `api.base.url.prod` / `api.base.url.local`. Ver `local.properties.example`. Para un celular físico con backend local usa la IP de tu PC, por ejemplo `http://192.168.1.10:8080/api/v1/`.

## Stack técnico

- Kotlin, Jetpack Compose, Material 3, Navigation Compose
- Arquitectura por capas: `data` (Retrofit, DTOs, repositorios), `domain` (modelos y casos de uso) y `presentation` (pantallas y ViewModels)
- Hilt para inyección de dependencias
- Retrofit + OkHttp + Moshi
- Sesión y token en DataStore
- Firebase Cloud Messaging para notificaciones push
- Product flavors `local` / `prod` → `BuildConfig.API_BASE_URL`

## Repos relacionados

- Backend: **Logix-OptiFlow-Back-End**
- Reporte: **Logix-OptiFlow-Report**
