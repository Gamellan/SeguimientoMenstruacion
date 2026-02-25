package com.seguimiento.menstruacion.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val flowOptions = listOf("Ligero", "Medio", "Abundante")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodTrackerScreen(viewModel: PeriodTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento menstrual") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Predicciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Duración media del ciclo: ${uiState.predictions.averageCycleLengthDays} días")
                        Text(
                            "Próxima menstruación: ${uiState.predictions.nextPeriodDate?.format(formatter) ?: "Sin datos suficientes"}"
                        )
                        Text(
                            "Ovulación estimada: ${uiState.predictions.ovulationDate?.format(formatter) ?: "Sin datos suficientes"}"
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Nuevo registro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        DatePickerField(
                            label = "Fecha inicio",
                            value = uiState.form.startDate,
                            onDateSelected = viewModel::onStartDateChanged
                        )
                        DatePickerField(
                            label = "Fecha fin",
                            value = uiState.form.endDate,
                            onDateSelected = viewModel::onEndDateChanged
                        )
                        FlowSelector(
                            selected = uiState.form.flowLevel,
                            onSelected = viewModel::onFlowChanged
                        )
                        OutlinedTextField(
                            value = uiState.form.symptomsText,
                            onValueChange = viewModel::onSymptomsChanged,
                            label = { Text("Síntomas (separados por comas)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.form.painLevel,
                            onValueChange = viewModel::onPainChanged,
                            label = { Text("Dolor (1-10)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.form.notes,
                            onValueChange = viewModel::onNotesChanged,
                            label = { Text("Notas") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        uiState.form.error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }

                        Button(
                            onClick = viewModel::saveRecord,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar registro")
                        }
                    }
                }
            }

            item {
                Text("Historial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (uiState.records.isEmpty()) {
                item {
                    Text("Aún no hay registros")
                }
            } else {
                items(uiState.records, key = { it.id }) { record ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "${record.startDate.format(formatter)} - ${record.endDate.format(formatter)}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Flujo: ${record.flowLevel}")
                            Text("Dolor: ${record.painLevel}/10")
                            if (record.symptoms.isNotEmpty()) {
                                Text("Síntomas: ${record.symptoms.joinToString()}")
                            }
                            if (record.notes.isNotBlank()) {
                                Text("Notas: ${record.notes}")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = value.toDateMillisOrNull()
    )

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )

    Button(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Seleccionar $label")
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millis.toIsoLocalDate())
                    }
                    showPicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun String.toDateMillisOrNull(): Long? {
    return runCatching {
        LocalDate.parse(this)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun Long.toIsoLocalDate(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlowSelector(
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Cantidad de sangre") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            flowOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
