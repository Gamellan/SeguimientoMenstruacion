package com.seguimiento.menstruacion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.seguimiento.menstruacion.data.AppPreferences
import com.seguimiento.menstruacion.data.PeriodPredictions
import com.seguimiento.menstruacion.data.PeriodRecord
import com.seguimiento.menstruacion.data.PeriodRepository
import com.seguimiento.menstruacion.data.PeriodStatistics
import com.seguimiento.menstruacion.notifications.ReminderScheduler
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PeriodFormState(
    val editingRecordId: Long? = null,
    val startDate: String = "",
    val endDate: String = "",
    val isOngoing: Boolean = false,
    val flowLevel: String = "Medio",
    val selectedSymptoms: Set<String> = emptySet(),
    val customSymptomsText: String = "",
    val painLevel: String = "5",
    val notes: String = "",
    val error: String? = null
)

data class PeriodTrackerUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val showOnboarding: Boolean = true,
    val remindersEnabled: Boolean = false,
    val visibleMonth: YearMonth = YearMonth.now(),
    val form: PeriodFormState = PeriodFormState(),
    val records: List<PeriodRecord> = emptyList(),
    val predictions: PeriodPredictions = PeriodPredictions(null, null, 28),
    val statistics: PeriodStatistics = PeriodStatistics(28, 5, 0, 0)
)

enum class AppScreen {
    HOME,
    SETTINGS,
    STATISTICS,
    CREATE_RECORD,
    HISTORY,
    EDIT_RECORD
}

class PeriodTrackerViewModel(
    private val repository: PeriodRepository,
    private val preferences: AppPreferences,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private data class CoreUiData(
        val form: PeriodFormState,
        val records: List<PeriodRecord>,
        val showOnboarding: Boolean,
        val remindersEnabled: Boolean
    )

    private val formState = MutableStateFlow(PeriodFormState())
    private val onboardingState = MutableStateFlow(!preferences.isOnboardingCompleted())
    private val remindersEnabledState = MutableStateFlow(preferences.isRemindersEnabled())
    private val currentScreenState = MutableStateFlow(AppScreen.HOME)
    private val visibleMonthState = MutableStateFlow(YearMonth.now())

    private val predefinedSymptoms = listOf(
        "Cólicos",
        "Cefalea",
        "Hinchazón",
        "Acné",
        "Fatiga",
        "Náuseas",
        "Cambios de humor"
    )

    private val coreUiData = combine(
        formState,
        repository.observeRecords(),
        onboardingState,
        remindersEnabledState
    ) { form, records, showOnboarding, remindersEnabled ->
        CoreUiData(
            form = form,
            records = records,
            showOnboarding = showOnboarding,
            remindersEnabled = remindersEnabled
        )
    }

    val uiState: StateFlow<PeriodTrackerUiState> = combine(
        coreUiData,
        currentScreenState,
        visibleMonthState
    ) { core, currentScreen, visibleMonth ->
        PeriodTrackerUiState(
            currentScreen = currentScreen,
            showOnboarding = core.showOnboarding,
            remindersEnabled = core.remindersEnabled,
            visibleMonth = visibleMonth,
            form = core.form,
            records = core.records,
            predictions = repository.buildPredictions(core.records),
            statistics = repository.buildStatistics(core.records)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PeriodTrackerUiState()
    )

    init {
        viewModelScope.launch {
            repository.observeRecords().collect { records ->
                if (remindersEnabledState.value) {
                    reminderScheduler.schedule(repository.buildPredictions(records))
                    if (records.any { it.isOngoing }) {
                        reminderScheduler.scheduleDailyOngoingReminder()
                    } else {
                        reminderScheduler.cancelDailyOngoingReminder()
                    }
                }
            }
        }
    }

    fun onPeriodRangeChanged(startDate: String, endDate: String?) {
        formState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate ?: "",
                isOngoing = endDate == null,
                error = null
            )
        }
    }

    fun onOngoingChanged(isOngoing: Boolean) {
        formState.update {
            it.copy(
                isOngoing = isOngoing,
                endDate = if (isOngoing) "" else it.endDate,
                error = null
            )
        }
    }

    fun onFlowChanged(value: String) = formState.update { it.copy(flowLevel = value, error = null) }
    fun onCustomSymptomsChanged(value: String) = formState.update { it.copy(customSymptomsText = value, error = null) }
    fun onPainChanged(value: String) = formState.update { it.copy(painLevel = value, error = null) }
    fun onNotesChanged(value: String) = formState.update { it.copy(notes = value, error = null) }

    fun availableSymptoms(): List<String> = predefinedSymptoms

    fun togglePredefinedSymptom(symptom: String) {
        formState.update { current ->
            val updated = current.selectedSymptoms.toMutableSet()
            if (updated.contains(symptom)) {
                updated.remove(symptom)
            } else {
                updated.add(symptom)
            }
            current.copy(selectedSymptoms = updated, error = null)
        }
    }

    fun completeOnboarding() {
        onboardingState.value = false
        preferences.setOnboardingCompleted(true)
    }

    fun goToHome() {
        currentScreenState.value = AppScreen.HOME
    }

    fun goToSettings() {
        currentScreenState.value = AppScreen.SETTINGS
    }

    fun goToStatistics() {
        currentScreenState.value = AppScreen.STATISTICS
    }

    fun goToCreateRecord() {
        formState.value = PeriodFormState(flowLevel = formState.value.flowLevel)
        currentScreenState.value = AppScreen.CREATE_RECORD
    }

    fun goToHistory() {
        currentScreenState.value = AppScreen.HISTORY
    }

    fun nextMonth() {
        visibleMonthState.value = visibleMonthState.value.plusMonths(1)
    }

    fun previousMonth() {
        visibleMonthState.value = visibleMonthState.value.minusMonths(1)
    }

    fun setRemindersEnabled(enabled: Boolean) {
        remindersEnabledState.value = enabled
        preferences.setRemindersEnabled(enabled)

        if (enabled) {
            reminderScheduler.schedule(uiState.value.predictions)
            if (uiState.value.records.any { it.isOngoing }) {
                reminderScheduler.scheduleDailyOngoingReminder()
            }
        } else {
            reminderScheduler.cancelAll()
        }
    }

    fun shouldAttemptAutoEnableNotifications(): Boolean {
        return !preferences.isAutoNotificationAttempted() && !remindersEnabledState.value
    }

    fun markAutoEnableNotificationsAttempted() {
        preferences.setAutoNotificationAttempted(true)
    }

    fun completeAutoEnableNotificationsAttempt(granted: Boolean) {
        preferences.setAutoNotificationAttempted(true)
        if (granted) {
            setRemindersEnabled(true)
        }
    }

    fun editRecord(record: PeriodRecord) {
        val predefined = record.symptoms.filter { predefinedSymptoms.contains(it) }.toSet()
        val custom = record.symptoms.filterNot { predefinedSymptoms.contains(it) }.joinToString(", ")
        formState.value = PeriodFormState(
            editingRecordId = record.id,
            startDate = record.startDate.toString(),
            endDate = record.endDate?.toString().orEmpty(),
            isOngoing = record.isOngoing,
            flowLevel = record.flowLevel,
            selectedSymptoms = predefined,
            customSymptomsText = custom,
            painLevel = record.painLevel.toString(),
            notes = record.notes,
            error = null
        )
        currentScreenState.value = AppScreen.EDIT_RECORD
    }

    fun cancelEdit() {
        formState.value = PeriodFormState(flowLevel = formState.value.flowLevel)
        currentScreenState.value = AppScreen.HISTORY
    }

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            if (formState.value.editingRecordId == recordId) {
                cancelEdit()
            }
        }
    }

    fun saveRecord() {
        val form = formState.value
        val startDate = parseDate(form.startDate)
        val endDate = parseDate(form.endDate)
        val pain = form.painLevel.toIntOrNull()
        val effectiveIsOngoing = form.isOngoing || endDate == null

        if (startDate == null) {
            formState.update { it.copy(error = "Select the start date of the period") }
            return
        }

        if (!effectiveIsOngoing && endDate != null && endDate.isBefore(startDate)) {
            formState.update { it.copy(error = "End date cannot be earlier than start date") }
            return
        }

        if (pain == null || pain !in 1..10) {
            formState.update { it.copy(error = "Pain level must be between 1 and 10") }
            return
        }

        val customSymptoms = form.customSymptomsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val symptoms = (form.selectedSymptoms + customSymptoms).toList()

        val record = PeriodRecord(
            id = form.editingRecordId ?: 0,
            startDate = startDate,
            endDate = if (effectiveIsOngoing) null else endDate,
            isOngoing = effectiveIsOngoing,
            flowLevel = form.flowLevel,
            symptoms = symptoms,
            painLevel = pain,
            notes = form.notes
        )

        viewModelScope.launch {
            if (form.editingRecordId == null) {
                repository.addRecord(record)
                currentScreenState.value = AppScreen.HOME
            } else {
                repository.updateRecord(record)
                currentScreenState.value = AppScreen.HISTORY
            }
            formState.update { PeriodFormState(flowLevel = it.flowLevel) }
        }
    }

    private fun parseDate(value: String): LocalDate? {
        return try {
            LocalDate.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

class PeriodTrackerViewModelFactory(
    private val repository: PeriodRepository,
    private val preferences: AppPreferences,
    private val reminderScheduler: ReminderScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeriodTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PeriodTrackerViewModel(repository, preferences, reminderScheduler) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class")
    }
}
