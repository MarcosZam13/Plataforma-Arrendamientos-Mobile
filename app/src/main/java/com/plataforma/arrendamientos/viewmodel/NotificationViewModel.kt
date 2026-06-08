package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plataforma.arrendamientos.data.model.AppNotification
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val notifications: StateFlow<List<AppNotification>> = dataRepository.notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun refreshNotifications(userId: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.refreshNotifications(userId)
                .onFailure { _error.update { it.message } }
            _isLoading.update { false }
        }
    }

    fun getByUser(userId: String) = dataRepository.getNotificationsByUser(userId)

    fun getUnreadCount(userId: String) = dataRepository.getUnreadCountByUser(userId)

    fun markAsRead(id: String) {
        viewModelScope.launch {
            dataRepository.markNotificationReadApi(id)
                .onFailure { dataRepository.markNotificationRead(id) }
        }
    }

    fun markAllAsRead(userId: String) = dataRepository.markAllNotificationsRead(userId)

    fun clearError() = _error.update { null }
}
