package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val properties: StateFlow<List<Property>> = dataRepository.properties

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.refreshProperties()
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun getPropertiesByOwner(duenoId: String) = dataRepository.getPropertiesByOwner(duenoId)

    fun getPropertyById(id: String) = dataRepository.getPropertyById(id)

    fun addProperty(property: Property, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.createPropertyApi(property)
                .onSuccess { onSuccess() }
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun updateProperty(property: Property, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.updatePropertyApi(property)
                .onSuccess { onSuccess() }
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun deleteProperty(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.deletePropertyApi(id)
                .onSuccess { onSuccess() }
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun searchProperties(query: String, provincia: String?, tipo: PropertyType?, maxPrecio: Double?): List<Property> {
        return dataRepository.properties.value.filter { prop ->
            val matchesQuery = query.isEmpty() ||
                prop.titulo.contains(query, ignoreCase = true) ||
                prop.descripcion.contains(query, ignoreCase = true) ||
                prop.canton.contains(query, ignoreCase = true)
            val matchesProvincia = provincia == null || prop.provincia == provincia
            val matchesTipo = tipo == null || prop.tipo == tipo
            val matchesPrecio = maxPrecio == null || prop.precio <= maxPrecio
            val isAvailable = prop.estado == PropertyStatus.DISPONIBLE
            matchesQuery && matchesProvincia && matchesTipo && matchesPrecio && isAvailable
        }
    }

    fun clearError() = _error.update { null }
}
