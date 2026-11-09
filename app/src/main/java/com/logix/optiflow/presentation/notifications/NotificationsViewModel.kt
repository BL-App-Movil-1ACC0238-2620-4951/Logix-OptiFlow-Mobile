package com.logix.optiflow.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.notification.NotificationItem
import com.logix.optiflow.domain.repository.notification.NotificationRepository
import com.logix.optiflow.domain.usecase.notification.GetNotificationsUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientSessionUseCase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationSectionUi(
    val title: String,
    val items: List<NotificationRowUi>,
)

data class NotificationRowUi(
    val item: NotificationItem,
    val timeLabel: String,
    val isRead: Boolean,
)

data class NotificationsUiState(
    val sections: List<NotificationSectionUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class NotificationsViewModel(
    private val getPatientSession: GetPatientSessionUseCase,
    private val getNotifications: GetNotificationsUseCase,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val session =
                    getPatientSession()
                        ?: error("Inicia sesión para ver tus notificaciones.")
                val readIds = notificationRepository.getReadIds()
                val items = getNotifications(session.patientId)
                val rows =
                    items.map { item ->
                        NotificationRowUi(
                            item = item,
                            timeLabel = formatRelativeTime(item.occurredAt),
                            isRead = item.id in readIds,
                        )
                    }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sections = groupByDay(rows),
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.toUserMessage())
                }
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            val ids = _uiState.value.sections.flatMap { section -> section.items.map { row -> row.item.id } }
            notificationRepository.markAllRead(ids)
            refresh()
        }
    }

    fun onNotificationClick(id: String) {
        viewModelScope.launch {
            notificationRepository.markRead(id)
            refresh()
        }
    }

    private fun groupByDay(rows: List<NotificationRowUi>): List<NotificationSectionUi> {
        if (rows.isEmpty()) return emptyList()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val todayItems = mutableListOf<NotificationRowUi>()
        val yesterdayItems = mutableListOf<NotificationRowUi>()
        val olderItems = mutableListOf<NotificationRowUi>()
        rows.forEach { row ->
            val date = row.item.occurredAt.atZone(zone).toLocalDate()
            when (date) {
                today -> todayItems.add(row)
                yesterday -> yesterdayItems.add(row)
                else -> olderItems.add(row)
            }
        }
        val sections = mutableListOf<NotificationSectionUi>()
        if (todayItems.isNotEmpty()) {
            sections.add(NotificationSectionUi(title = "Hoy", items = todayItems))
        }
        if (yesterdayItems.isNotEmpty()) {
            sections.add(NotificationSectionUi(title = "Ayer", items = yesterdayItems))
        }
        if (olderItems.isNotEmpty()) {
            sections.add(NotificationSectionUi(title = "Anteriores", items = olderItems))
        }
        return sections
    }

    private fun formatRelativeTime(instant: Instant): String {
        val hours = ChronoUnit.HOURS.between(instant, Instant.now())
        return when {
            hours < 1 -> "Hace un momento"
            hours < 24 -> "Hace $hours h"
            hours < 48 -> "Ayer"
            else -> "Hace ${ChronoUnit.DAYS.between(instant, Instant.now())} días"
        }
    }
}
