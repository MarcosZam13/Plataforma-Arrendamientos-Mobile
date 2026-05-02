package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import com.plataforma.arrendamientos.data.model.AppNotification
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val notifications: StateFlow<List<AppNotification>> = dataRepository.notifications

    fun getByUser(userId: String) = dataRepository.getNotificationsByUser(userId)

    fun getUnreadCount(userId: String) = dataRepository.getUnreadCountByUser(userId)

    fun markAsRead(id: String) = dataRepository.markNotificationRead(id)

    fun markAllAsRead(userId: String) = dataRepository.markAllNotificationsRead(userId)
}