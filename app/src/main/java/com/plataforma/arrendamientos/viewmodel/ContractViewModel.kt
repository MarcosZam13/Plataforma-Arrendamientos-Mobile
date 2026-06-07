package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plataforma.arrendamientos.data.model.Contract
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContractViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val contracts: StateFlow<List<Contract>> = dataRepository.contracts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isLoading.update { true }
            dataRepository.refreshContracts()
            _isLoading.update { false }
        }
    }

    fun getContractByUser(userId: String): Contract? = dataRepository.getContractByUser(userId)

    fun getContractByInquilino(inquilinoId: String): Contract? =
        dataRepository.getContractByInquilino(inquilinoId)

    fun getContractsByOwner(duenoId: String): List<Contract> =
        dataRepository.getContractsByOwner(duenoId)
}
