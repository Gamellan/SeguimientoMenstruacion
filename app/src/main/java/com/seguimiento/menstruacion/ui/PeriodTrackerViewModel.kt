package com.seguimiento.menstruacion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.seguimiento.menstruacion.data.PeriodPredictions
import com.seguimiento.menstruacion.data.PeriodRecord
import com.seguimiento.menstruacion.data.PeriodRepository
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PeriodFormState(
    val startDate: String = "",
    val endDate: String = "",
    val flowLevel: String = "Medio",
    val symptomsText: String = "",
    val painLevel: String = "5",
    val notes: String = "",
    val error: String? = null
)

data class PeriodTrackerUiState(
    val form: PeriodFormState = PeriodFormState(),
    val records: List<PeriodRecord> = emptyList(),
    val predictions: PeriodPredictions = PeriodPredictions(null, null, 28)
)

class PeriodTrackerViewModel(
    private val repository: PeriodRepository
) : ViewModel() {

    private val formState = MutableStateFlow(PeriodFormState())

    val uiState: StateFlow<PeriodTrackerUiState> = combine(
        formState,
        repository.observeRecords()
    ) { form, records ->
        PeriodTrackerUiState(
            form = form,
            records = records,
            predictions = repository.buildPredictions(records)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PeriodTrackerUiState()
    )

    fun onStartDateChanged(value: String) = formState.update { it.copy(startDate = value, error = null) }
    fun onEndDateChanged(value: String) = formState.update { it.copy(endDate = value, error = null) }
    fun onFlowChanged(value: String) = formState.update { it.copy(flowLevel = value, error = null) }
    fun onSymptomsChanged(value: String) = formState.update { it.copy(symptomsText = value, error = null) }
    fun onPainChanged(value: String) = formState.update { it.copy(painLevel = value, error = null) }
    fun onNotesChanged(value: String) = formState.update { it.copy(notes = value, error = null) }

    fun saveRecord() {
        val form = formState.value
        val startDate = parseDate(form.startDate)
        val endDate = parseDate(form.endDate)
        val pain = form.painLevel.toIntOrNull()

        if (startDate == null || endDate == null) {
            formState.update { it.copy(error = "Formato de fecha inválido. Usa YYYY-MM-DD") }
            return
        }

        if (endDate.isBefore(startDate)) {
            formState.update { it.copy(error = "La fecha de fin no puede ser anterior al inicio") }
            return
        }

        if (pain == null || pain !in 1..10) {
            formState.update { it.copy(error = "El nivel de dolor debe estar entre 1 y 10") }
            return
        }

        val record = PeriodRecord(
            id = 0,
            startDate = startDate,
            endDate = endDate,
            flowLevel = form.flowLevel,
            symptoms = form.symptomsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
            painLevel = pain,
            notes = form.notes
        )

        viewModelScope.launch {
            repository.addRecord(record)
            formState.update {
                PeriodFormState(
                    flowLevel = it.flowLevel
                )
            }
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
    private val repository: PeriodRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeriodTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PeriodTrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel class no soportada")
    }
}
