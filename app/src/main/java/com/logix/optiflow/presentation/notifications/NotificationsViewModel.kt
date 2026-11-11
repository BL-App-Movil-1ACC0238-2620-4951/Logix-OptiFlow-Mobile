package com.logix.optiflow.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.notification.NotificationItem
import com.logix.optiflow.domain.usecase.notification.AnswerSatisfactionSurveyUseCase
import com.logix.optiflow.domain.usecase.notification.GetNotificationsUseCase
import com.logix.optiflow.domain.usecase.notification.MarkNotificationReadUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getPatientSession: GetPatientSessionUseCase,
    private val getNotifications: GetNotificationsUseCase,
    private val markNotificationRead: MarkNotificationReadUseCase,
    private val answerSatisfactionSurvey: AnswerSatisfactionSurveyUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = getPatientSession()
            if (session == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Inicia sesión para ver tus notificaciones.") }
                return@launch
            }
            getNotifications(session.patientId)
                .onSuccess { items ->
                    val rows =
                        items.map { item ->
                            NotificationRowUi(
                                item = item,
                                timeLabel = formatRelativeTime(item.occurredAt),
                                isRead = item.read,
                            )
                        }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sections = groupByDay(rows),
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }

    /** El backend marca de una en una (`PATCH /notifications/{id}`), así que se envían todas las no leídas. */
    fun markAllRead() {
        viewModelScope.launch {
            val unread = _uiState.value.sections.flatMap { it.items }.filter { !it.isRead }.map { it.item.id }
            if (unread.isEmpty()) return@launch
            unread.map { id -> async { markNotificationRead(id) } }.awaitAll()
            refresh()
        }
    }

    fun onNotificationClick(row: NotificationRowUi) {
        if (row.isRead) return
        viewModelScope.launch {
            markNotificationRead(row.item.id).onSuccess { refresh() }
        }
    }

    /** Responde la encuesta de satisfacción; [onDone] recibe el error o `null`. */
    fun answerSurvey(id: UUID, rating: Int, comment: String, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val result = answerSatisfactionSurvey(id, rating, comment)
            onDone(result.exceptionOrNull()?.toUserMessage())
            if (result.isSuccess) refresh()
        }
    }

    private fun groupByDay(rows: List<NotificationRowUi>): List<NotificationSectionUi> {
        if (rows.isEmpty()) return emptyList()
        val zone = STORE_ZONE
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
