package com.plataforma.arrendamientos.data.model

// Demo data matching the web app's mock data

object MockData {

    val MOCK_USERS = listOf(
        User(
            id = "user-1",
            nombre = "Carlos Rodríguez",
            correo = "carlos@example.com",
            rol = UserRole.DUENO,
            avatar = null
        ),
        User(
            id = "user-2",
            nombre = "María González",
            correo = "maria@example.com",
            rol = UserRole.INQUILINO,
            avatar = null
        )
    )

    val MOCK_PROPERTIES = listOf(
        Property(
            id = "prop-1",
            titulo = "Apartamento moderno en Escazú",
            descripcion = "Hermoso apartamento completamente amueblado con vista a la ciudad. Incluye todos los servicios básicos y acceso a piscina y gimnasio.",
            precio = 950.0,
            moneda = Currency.USD,
            provincia = "San José",
            canton = "Escazú",
            distrito = "San Rafael",
            tipo = PropertyType.APARTAMENTO,
            estado = PropertyStatus.ALQUILADA,
            imagenes = listOf(
                "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800",
                "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800"
            ),
            duenoId = "user-1",
            caracteristicas = PropertyFeatures(
                habitaciones = 2,
                banos = 2,
                parqueos = 1,
                areaM2 = 85.0,
                mascotas = false,
                amueblado = true,
                aguaIncluida = true,
                luzIncluida = false,
                internetIncluido = true,
                seguridad = true,
                piscina = true,
                gimnasio = true
            )
        ),
        Property(
            id = "prop-2",
            titulo = "Casa familiar en Santa Ana",
            descripcion = "Amplia casa de dos pisos en urbanización privada. Jardín, cochera doble y cuarto de servicio.",
            precio = 1200.0,
            moneda = Currency.USD,
            provincia = "San José",
            canton = "Santa Ana",
            distrito = "Pozos",
            tipo = PropertyType.CASA,
            estado = PropertyStatus.DISPONIBLE,
            imagenes = listOf(
                "https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=800"
            ),
            duenoId = "user-1",
            caracteristicas = PropertyFeatures(
                habitaciones = 4,
                banos = 3,
                parqueos = 2,
                areaM2 = 220.0,
                mascotas = true,
                amueblado = false,
                aguaIncluida = false,
                luzIncluida = false,
                internetIncluido = false,
                seguridad = true,
                piscina = false,
                gimnasio = false
            )
        ),
        Property(
            id = "prop-3",
            titulo = "Oficina ejecutiva en Lindora",
            descripcion = "Oficina moderna en edificio corporativo. Parqueo incluido, sala de reuniones compartida.",
            precio = 450000.0,
            moneda = Currency.CRC,
            provincia = "San José",
            canton = "Santa Ana",
            distrito = "Lindora",
            tipo = PropertyType.OFICINA,
            estado = PropertyStatus.DISPONIBLE,
            imagenes = listOf(
                "https://images.unsplash.com/photo-1497366216548-37526070297c?w=800"
            ),
            duenoId = "user-1",
            caracteristicas = PropertyFeatures(
                habitaciones = 0,
                banos = 1,
                parqueos = 2,
                areaM2 = 45.0,
                mascotas = false,
                amueblado = true,
                aguaIncluida = true,
                luzIncluida = true,
                internetIncluido = true,
                seguridad = true,
                piscina = false,
                gimnasio = false
            )
        )
    )

    val MOCK_CONTRACT = Contract(
        id = "contract-1",
        invitacionId = "inv-1",
        propiedadId = "prop-1",
        duenoId = "user-1",
        inquilinoId = "user-2",
        montoMensual = 950.0,
        montoDeposito = 1900.0,
        moneda = Currency.USD,
        fechaInicio = "2024-01-01",
        estado = ContractStatus.ACTIVO,
        estadoDeposito = DepositStatus.PAGADO
    )

    val MOCK_PAYMENTS = listOf(
        Payment(
            id = "pay-1",
            tipo = PaymentType.DEPOSITO,
            contratoId = "contract-1",
            propiedadId = "prop-1",
            inquilinoId = "user-2",
            duenoId = "user-1",
            mes = 1,
            anio = 2024,
            monto = 1900.0,
            moneda = Currency.USD,
            estado = PaymentStatus.APROBADO,
            fechaSubida = "2024-01-02T10:00:00",
            fechaRevision = "2024-01-02T14:00:00"
        ),
        Payment(
            id = "pay-2",
            tipo = PaymentType.MENSUALIDAD,
            contratoId = "contract-1",
            propiedadId = "prop-1",
            inquilinoId = "user-2",
            duenoId = "user-1",
            mes = 1,
            anio = 2024,
            monto = 950.0,
            moneda = Currency.USD,
            estado = PaymentStatus.APROBADO,
            fechaSubida = "2024-01-31T09:00:00",
            fechaRevision = "2024-01-31T16:00:00"
        ),
        Payment(
            id = "pay-3",
            tipo = PaymentType.MENSUALIDAD,
            contratoId = "contract-1",
            propiedadId = "prop-1",
            inquilinoId = "user-2",
            duenoId = "user-1",
            mes = 2,
            anio = 2024,
            monto = 950.0,
            moneda = Currency.USD,
            estado = PaymentStatus.PENDIENTE,
            fechaSubida = "2024-02-28T10:00:00"
        )
    )

    val PROVINCES = listOf(
        "San José", "Alajuela", "Cartago", "Heredia",
        "Guanacaste", "Puntarenas", "Limón"
    )
}
