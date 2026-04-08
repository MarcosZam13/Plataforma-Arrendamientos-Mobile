# Plataforma de Arrendamientos CR — Android

Aplicación móvil nativa para Android de la Plataforma de Arrendamientos de Costa Rica. Construida con Kotlin y Jetpack Compose, es la adaptación mobile de la [versión web](https://github.com/Pochonski/Plataforma-de-Arrendamientos-CR), replicando fielmente su diseño, flujos de usuario y funcionalidades para el mercado costarricense de alquileres.

---

## Tecnologías principales

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material3 |
| Arquitectura | MVVM + Hilt (inyección de dependencias) |
| Navegación | Navigation Compose (rutas tipo-seguras) |
| Estado | StateFlow / MutableStateFlow |
| Persistencia local | Room Database + DataStore Preferences |
| Red | Retrofit 2 + OkHttp + kotlinx.serialization |
| Imágenes | Coil Compose |
| Coroutines | kotlinx.coroutines |
| Build System | Gradle 8.10.2 (Kotlin DSL) + Version Catalog |
| Min SDK | 26 (Android 8.0+) |
| Target SDK | 35 (Android 15) |

---

## Funcionalidades

### Para Dueños
- Dashboard con estadísticas en tiempo real (propiedades activas, ingresos del mes, pagos pendientes)
- Gestión completa de propiedades: crear, editar, eliminar
- Generación de links únicos de invitación con token compartible
- Revisión y aprobación / rechazo de comprobantes de pago
- Historial completo de transacciones
- Mensajería directa con inquilinos
- Notificaciones automáticas por eventos

### Para Inquilinos
- Catálogo de propiedades con filtros avanzados (provincia, tipo, precio)
- Vista detallada de propiedades con amenidades
- Aceptación de invitaciones de contrato mediante deep links
- Subida de comprobantes de pago (SINPE / transferencia)
- Gestión del contrato activo
- Comunicación directa con el dueño
- Historial de pagos

### General
- Autenticación con rol (Dueño / Inquilino)
- Sesión persistente con DataStore
- Tema claro / oscuro (Material3)
- Deep links: `arrendamientos://invitacion/{token}`
- Diseño responsivo adaptado a todas las pantallas Android

---

## Estructura del proyecto

```
app/src/main/java/com/plataforma/arrendamientos/
│
├── data/
│   ├── model/
│   │   ├── Models.kt          # Entidades, enums y data classes
│   │   └── MockData.kt        # Datos demo para desarrollo
│   └── repository/
│       ├── AuthRepository.kt  # Login, registro, sesión (DataStore)
│       └── DataRepository.kt  # Propiedades, pagos, mensajes, notificaciones
│
├── di/
│   └── AppModule.kt           # Módulo Hilt (repositorios singleton)
│
├── ui/
│   ├── components/
│   │   └── SharedComponents.kt  # Componentes reutilizables (cards, badges, etc.)
│   ├── navigation/
│   │   ├── Screen.kt           # Rutas de navegación (sealed class)
│   │   └── AppNavigation.kt    # Grafo de navegación con routing por rol
│   ├── screens/
│   │   ├── public_screens/     # Landing, Login, Registro, Propiedades, Detalle
│   │   ├── dueno/              # Dashboard, Propiedades, Invitaciones, Pagos, Mensajes
│   │   └── inquilino/          # Dashboard, Contrato, Comprobante, Mensajes
│   └── theme/
│       ├── Color.kt            # Paleta de colores (#0979B0 primario)
│       ├── Theme.kt            # Tema Material3 claro/oscuro
│       └── Type.kt             # Sistema tipográfico
│
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── PropertyViewModel.kt
│   ├── InvitationViewModel.kt
│   ├── PaymentViewModel.kt
│   └── MessageViewModel.kt
│
├── MainActivity.kt
└── PlataformaApp.kt            # Application class (@HiltAndroidApp)
```

---

## Pantallas implementadas

| Sección | Pantalla |
|---|---|
| **Pública** | Landing, Login, Registro, Propiedades, Detalle de propiedad, Recuperar contraseña, Aceptar invitación |
| **Dueño** | Dashboard, Mis propiedades, Nueva propiedad, Editar propiedad, Invitaciones, Nueva invitación, Pagos recibidos, Mensajes, Historial, Notificaciones, Perfil |
| **Inquilino** | Dashboard, Mi contrato, Subir comprobante, Mensajes, Historial, Notificaciones, Perfil |

**Total: 25+ pantallas** — equivalente a la versión web.

---

## Sistema de colores

Idéntico al proyecto web:

| Token | Claro | Oscuro |
|---|---|---|
| Primary | `#0979B0` | `#4AAFC7` |
| Background | `#F5F5F7` | `#111111` |
| Surface | `#FFFFFF` | `#1C1C1E` |
| Success | `#16A34A` | `#16A34A` |
| Warning | `#D97706` | `#D97706` |
| Error | `#D4183D` | `#D4183D` |

---

## Requisitos

- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17+
- Android SDK 35
- Dispositivo o emulador con Android 8.0+ (API 26+)

---

## Instalación y ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/TU_USUARIO/Plataforma-Arrendamientos-Android.git
cd Plataforma-Arrendamientos-Android

# 2. Abrir en Android Studio
# File > Open > seleccionar la carpeta del proyecto

# 3. Esperar la sincronización de Gradle (automática)

# 4. Ejecutar
# Run > Run 'app'  (emulador o dispositivo físico)
```

> El build output se redirige a `C:/AndroidBuild/PlataformaArrendamientos/` para evitar conflictos con sincronización de OneDrive/nube.

---

## Credenciales de demo

| Rol | Email | Contraseña |
|---|---|---|
| Dueño | `carlos@example.com` | `123456` |
| Inquilino | `maria@example.com` | `123456` |

---

## Arquitectura

La app sigue el patrón **MVVM** con separación clara de capas:

```
UI Layer  (Jetpack Compose Screens)
    ↕
ViewModel Layer  (StateFlow / MutableStateFlow)
    ↕
Repository Layer  (AuthRepository / DataRepository)
    ↕
Data Sources  (MockData / DataStore / Room / Retrofit)
```

- **Hilt** gestiona la inyección de dependencias en todos los niveles
- **StateFlow** proporciona reactividad unidireccional
- **Navigation Compose** con rutas tipo-seguras y routing condicional por rol

---

## Integración con backend (Azure APIM)

El proyecto está preparado para conectarse al mismo backend del proyecto web. Configura la URL base en `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://tu-azure-apim.azure-api.net/\"")
```

---

## Relación con la versión web

| Aspecto | Web | Android |
|---|---|---|
| Framework UI | React 18 + Tailwind CSS v4 | Jetpack Compose + Material3 |
| Gestión de estado | Context API | StateFlow + MVVM |
| Navegación | React Router 7 | Navigation Compose |
| Autenticación | Context + localStorage | DataStore Preferences |
| Componentes UI | Shadcn/ui + MUI | Material3 + componentes custom |
| Color primario | `#0979B0` | `#0979B0` ✓ |
| Roles | Dueño / Inquilino | Dueño / Inquilino ✓ |
| Pantallas | 25 páginas | 25+ pantallas ✓ |
| Deep links | URLs web | `arrendamientos://invitacion/{token}` |

---

## Roadmap

- [ ] Integración con backend API (Azure APIM)
- [ ] Subida real de imágenes a almacenamiento en la nube
- [ ] Notificaciones push (Firebase Cloud Messaging)
- [ ] Autenticación biométrica
- [ ] Modo offline completo con Room
- [ ] Integración con SINPE Móvil
- [ ] Verificación de identidad
- [ ] Publicación en Google Play Store

---

## Licencia

MIT License — proyecto de código abierto, disponible para contribuciones de la comunidad.

---

> Versión web: [Plataforma-de-Arrendamientos-CR](https://github.com/Pochonski/Plataforma-de-Arrendamientos-CR)
