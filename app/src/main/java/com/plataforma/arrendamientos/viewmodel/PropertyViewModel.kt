package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PropertyViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val properties: StateFlow<List<Property>> = dataRepository.properties

    fun getPropertiesByOwner(duenoId: String) = dataRepository.getPropertiesByOwner(duenoId)

    fun getPropertyById(id: String) = dataRepository.getPropertyById(id)

    fun addProperty(property: Property) = dataRepository.addProperty(property)

    fun updateProperty(property: Property) = dataRepository.updateProperty(property)

    fun deleteProperty(id: String) = dataRepository.deleteProperty(id)

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
}
