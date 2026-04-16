package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val payments: StateFlow<List<Payment>> = dataRepository.payments

    fun getPaymentsByContract(contratoId: String) =
        dataRepository.getPaymentsByContract(contratoId)

    fun getPaymentsByOwner(duenoId: String) =
        dataRepository.getPaymentsByOwner(duenoId)

    fun getPendingPayments(duenoId: String) =
        dataRepository.getPendingPaymentsByOwner(duenoId)

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
        tipo: PaymentType = PaymentType.MENSUALIDAD
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
        dataRepository.addPayment(payment)
        dataRepository.addNotification(
            AppNotification(
                id = "notif-${System.currentTimeMillis()}",
                userId = duenoId,
                tipo = NotificationType.PAGO_RECIBIDO,
                titulo = "Nuevo comprobante de pago",
                mensaje = "El inquilino ha subido un comprobante de pago.",
                leida = false,
                fecha = System.currentTimeMillis().toString()
            )
        )
    }

    fun approvePayment(id: String, inquilinoId: String) {
        dataRepository.approvePayment(id)
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
    }

    fun rejectPayment(id: String, motivo: String, inquilinoId: String) {
        dataRepository.rejectPayment(id, motivo)
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
    }
}
