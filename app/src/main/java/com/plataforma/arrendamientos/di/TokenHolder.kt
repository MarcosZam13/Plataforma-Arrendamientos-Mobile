package com.plataforma.arrendamientos.di

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenHolder @Inject constructor() {
    @Volatile var token: String? = null
}
