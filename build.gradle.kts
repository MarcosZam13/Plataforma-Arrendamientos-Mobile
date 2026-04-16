// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Redirige el build fuera de OneDrive para evitar conflictos de sincronización
layout.buildDirectory.set(file("C:/AndroidBuild/PlataformaArrendamientos/root"))

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
