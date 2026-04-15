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

## Instalación y ejecución local

```bash
# 1. Clonar el repositorio
git clone https://github.com/MarcosZamora/Plataforma-Arrendamientos-Android.git
cd Plataforma-Arrendamientos-Android

# 2. Abrir en Android Studio
# File > Open > seleccionar la carpeta del proyecto

# 3. Esperar la sincronización de Gradle (automática)

# 4. Ejecutar
# Run > Run 'app'  (emulador o dispositivo físico)
```

> El build output se redirige a `C:/AndroidBuild/PlataformaArrendamientos/` en Windows local para evitar conflictos con OneDrive. En CI/GitHub Actions usa el directorio estándar `build/`.

---

## CI/CD — Firebase App Distribution

El proyecto usa **GitHub Actions** para generar y distribuir automáticamente una APK a los testers cada vez que se hace push a la rama `release`.

### Flujo de trabajo

```
push a 'release'
       │
       ▼
GitHub Actions (ubuntu-latest)
       │
       ├─ 1. Checkout del código
       ├─ 2. Configurar JDK 17
       ├─ 3. Compilar APK debug (./gradlew assembleDebug)
       ├─ 4. Subir APK como artefacto en GitHub
       └─ 5. Distribuir a testers via Firebase App Distribution
                    │
                    └─ Notificación automática por email a cada tester
```

### Configurar el proyecto para un repo nuevo

#### Paso 1 — Crear un proyecto en Firebase

1. Ir a [console.firebase.google.com](https://console.firebase.google.com)
2. Crear un nuevo proyecto (ej. `plataforma-arrendamientos`)
3. Agregar una app Android con package name `com.plataforma.arrendamientos`
4. Ir a **App Distribution** en el menú lateral y habilitarlo

#### Paso 2 — Crear cuenta de servicio de Firebase

1. En Firebase Console → Configuración del proyecto → Cuentas de servicio
2. Hacer clic en **Generar nueva clave privada** → descargar el JSON
3. Abrir el archivo JSON descargado y copiar todo su contenido

#### Paso 3 — Obtener el App ID de Firebase

1. En Firebase Console → Configuración del proyecto → Tus apps
2. Copiar el **ID de la aplicación** (formato: `1:XXXXXXXXXX:android:YYYYYYYY`)

#### Paso 4 — Agregar Secrets en GitHub

En el repositorio → **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Valor |
|--------|-------|
| `FIREBASE_APP_ID` | ID de la app de Firebase (`1:XXX:android:YYY`) |
| `FIREBASE_CREDENTIALS` | Contenido completo del JSON de cuenta de servicio |

#### Paso 5 — Agregar testers en Firebase

1. En Firebase Console → App Distribution → Testers & Groups
2. Crear un grupo llamado **`testers`** (exactamente así, es lo que usa el workflow)
3. Agregar los emails de los testers (profesor + compañeros)
4. Cada tester recibirá un email de invitación de Firebase para instalar la app

#### Paso 6 — Crear y hacer push a la rama `release`

```bash
# Crear la rama release desde main
git checkout -b release
git push origin release

# Para publicar una nueva versión:
git checkout main
# ... hacer los cambios ...
git commit -m "feat: nueva funcionalidad"
git checkout release
git merge main
git push origin release   # ← Esto dispara el workflow automáticamente
```

### Instalación de la app (instrucciones para testers)

1. Revisar el email de invitación de Firebase App Distribution
2. Hacer clic en **"Get started"** en el email
3. Instalar la app de **Firebase App Tester** en el dispositivo Android
4. Abrir Firebase App Tester e iniciar sesión con el email registrado
5. La app aparecerá disponible para descargar
6. En Ajustes de Android → habilitar **"Instalar aplicaciones desconocidas"** para Firebase App Tester
7. Descargar e instalar la APK desde Firebase App Tester

> Las notificaciones de nuevas versiones llegan automáticamente por email y dentro de la app Firebase App Tester.

### Monitoreo de builds

- **GitHub Actions**: Pestaña `Actions` del repositorio en GitHub
- **Firebase Console**: App Distribution → Releases
- Los artefactos (APK) también se guardan en GitHub Actions por 30 días

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

- [x] Tema claro / oscuro con persistencia (SharedPreferences)
- [x] CI/CD con Firebase App Distribution + GitHub Actions
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
