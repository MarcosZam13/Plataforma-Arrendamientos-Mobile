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
class PaymentViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val payments: StateFlow<List<Payment>> = dataRepository.payments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun getPaymentsByContract(contratoId: String) = dataRepository.getPaymentsByContract(contratoId)
    fun getPaymentsByOwner(duenoId: String) = dataRepository.getPaymentsByOwner(duenoId)
    fun getPendingPayments(duenoId: String) = dataRepository.getPendingPaymentsByOwner(duenoId)

    fun refreshPayments(userId: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            dataRepository.refreshPayments(userId)
                .onFailure { _error.update { it.message } }
            _isLoading.update { false }
        }
    }

    fun submitPayment(
        contratoId: String,
        propiedadId: String,
        inquilinoId: String,
        duenoId: String,
        monto: Double,
        moneda: Currency,
        mes: Int,
        anio: Int,
        comprobante: String?,
        tipo: PaymentType = PaymentType.MENSUALIDAD,
        onSuccess: () -> Unit = {}
    ) {
        val payment = Payment(
            id = "pay-${System.currentTimeMillis()}",
            tipo = tipo,
            contratoId = contratoId,
            propiedadId = propiedadId,
            inquilinoId = inquilinoId,
            duenoId = duenoId,
            mes = mes,
            anio = anio,
            monto = monto,
            moneda = moneda,
            comprobante = comprobante,
            estado = PaymentStatus.PENDIENTE,
            fechaSubida = System.currentTimeMillis().toString()
        )
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.createPaymentApi(payment)
                .onSuccess { onSuccess() }
                .onFailure { _error.update { it.message } }
            _isLoading.update { false }
        }
    }

    fun approvePayment(id: String, inquilinoId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.updatePaymentStatusApi(id, "aprobado")
                .onSuccess {
                    dataRepository.addNotification(
                        AppNotification(
                            id = "notif-${System.currentTimeMillis()}",
                            userId = inquilinoId,
                            tipo = NotificationType.PAGO_APROBADO,
                            titulo = "Pago aprobado",
                            mensaje = "Tu pago ha sido aprobado por el propietario.",
                            leida = false,
                            fecha = System.currentTimeMillis().toString()
                        )
                    )
                    onSuccess()
                }
                .onFailure { _error.update { it.message } }
            _isLoading.update { false }
        }
    }

    fun rejectPayment(id: String, motivo: String, inquilinoId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.updatePaymentStatusApi(id, "rechazado", motivo)
                .onSuccess {
                    dataRepository.addNotification(
                        AppNotification(
                            id = "notif-${System.currentTimeMillis()}",
                            userId = inquilinoId,
                            tipo = NotificationType.PAGO_RECHAZADO,
                            titulo = "Pago rechazado",
                            mensaje = "Tu pago ha sido rechazado. Motivo: $motivo",
                            leida = false,
                            fecha = System.currentTimeMillis().toString()
                        )
                    )
                    onSuccess()
                }
                .onFailure { _error.update { it.message } }
            _isLoading.update { false }
        }
    }

    fun clearError() = _error.update { null }
}
