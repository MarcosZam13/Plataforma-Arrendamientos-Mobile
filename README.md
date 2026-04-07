# Plataforma de Arrendamientos - Android

Aplicación móvil para Android de la **Plataforma de Arrendamientos Costa Rica**, desarrollada en Kotlin con Jetpack Compose. Es la versión móvil nativa del proyecto web universitario.

## Stack tecnológico

| Categoría | Tecnología |
|-----------|-----------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Repository Pattern |
| Navegación | Navigation Compose |
| Inyección de dependencias | Hilt |
| Imágenes | Coil |
| Persistencia local | DataStore (sesión) |
| Red (preparado) | Retrofit + OkHttp |
| Base de datos (preparado) | Room |

## Estructura del proyecto

```
app/src/main/java/com/plataforma/arrendamientos/
├── MainActivity.kt
├── PlataformaApp.kt
├── data/
│   ├── model/
│   │   ├── Models.kt          # Todos los data classes y enums
│   │   └── MockData.kt        # Datos de demo
│   └── repository/
│       ├── AuthRepository.kt  # Autenticación + DataStore
│       └── DataRepository.kt  # Propiedades, pagos, contratos, etc.
├── di/
│   └── AppModule.kt           # Hilt modules
├── ui/
│   ├── theme/
│   │   ├── Color.kt           # Paleta de colores (matching web)
│   │   ├── Type.kt            # Tipografía (Inter font)
│   │   └── Theme.kt           # MaterialTheme config
│   ├── navigation/
│   │   ├── Screen.kt          # Sealed class con todas las rutas
│   │   └── AppNavigation.kt   # NavHost + todas las rutas
│   ├── components/
│   │   └── SharedComponents.kt # PropertyCard, StatCard, StatusBadge, etc.
│   └── screens/
│       ├── public_screens/    # Landing, Login, Registro, Propiedades, etc.
│       ├── dueno/             # Dashboard dueño + 10 pantallas
│       └── inquilino/         # Dashboard inquilino + 6 pantallas
└── viewmodel/
    ├── AuthViewModel.kt
    ├── PropertyViewModel.kt
    ├── PaymentViewModel.kt
    ├── InvitationViewModel.kt
    └── MessageViewModel.kt
```

## Pantallas implementadas

### Públicas (6)
- `LandingScreen` - Hero section, features, CTA
- `LoginScreen` - Email/contraseña, credenciales demo
- `RegistroScreen` - Registro con selección de rol
- `PropiedadesScreen` - Catálogo con filtros (provincia, tipo)
- `PropiedadDetalleScreen` - Detalle completo con amenidades
- `RecuperarContrasenaScreen` - Recuperación de contraseña
- `AceptarInvitacionScreen` - Aceptar invitación de arrendamiento

### Dueño (10)
- `DuenoDashboardScreen` - Stats, acciones rápidas, pagos pendientes
- `MisPropiedadesScreen` - Lista con editar/eliminar
- `NuevaPropiedadScreen` - Formulario completo de nueva propiedad
- `EditarPropiedadScreen` - Editar propiedad existente
- `InvitacionesScreen` - Lista de invitaciones enviadas
- `NuevaInvitacionScreen` - Crear invitación + copiar enlace
- `PagosRecibidosScreen` - Aprobar/rechazar comprobantes
- `MensajesScreen` - Conversaciones con inquilinos
- `HistorialScreen` - Historial de pagos con totales
- `NotificacionesScreen` - Centro de notificaciones
- `PerfilScreen` - Perfil + configuración + logout

### Inquilino (6)
- `InquilinoDashboardScreen` - Estado contrato, pagos, acciones
- `MiContratoScreen` - Detalles del contrato activo
- `SubirComprobanteScreen` - Subir foto de comprobante
- `MensajesInquilinoScreen` - Mensajes con propietario
- `HistorialInquilinoScreen` - Historial de pagos propios
- `PerfilInquilinoScreen` - Perfil + logout

## Sistema de colores

Idéntico al proyecto web:

| Token | Luz | Oscuro |
|-------|-----|--------|
| Primary | `#0979B0` | `#4AAFC7` |
| Background | `#F5F5F7` | `#111111` |
| Surface | `#FFFFFF` | `#1C1C1E` |
| Success | `#16A34A` | mismo |
| Warning | `#D97706` | mismo |
| Error | `#D4183D` | mismo |

## Credenciales de prueba

```
Dueño:     carlos@example.com / 123456
Inquilino: maria@example.com  / 123456
```

## Cómo abrir en Android Studio

1. Abrir Android Studio (Hedgehog o superior)
2. **File → Open** → seleccionar esta carpeta
3. Esperar que sincronice Gradle
4. **Run → Run 'app'** en un emulador o dispositivo físico (API 26+)

> **Nota:** Las fuentes Inter deben agregarse en `app/src/main/res/font/`:
> - `inter_regular.ttf`
> - `inter_medium.ttf`
> - `inter_semibold.ttf`
> - `inter_bold.ttf`
>
> Descarga gratuita en [Google Fonts - Inter](https://fonts.google.com/specimen/Inter)

## Integración con backend (Azure APIM)

El proyecto está preparado para conectarse al mismo backend del proyecto web.
Configura la URL en `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://tu-azure-apim.azure-api.net/\"")
```

## Autores

Proyecto universitario - Diseño de Plataformas Móviles
