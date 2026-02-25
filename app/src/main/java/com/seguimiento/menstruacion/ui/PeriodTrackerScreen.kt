package com.seguimiento.menstruacion.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.seguimiento.menstruacion.R
import com.seguimiento.menstruacion.data.PeriodPredictions
import com.seguimiento.menstruacion.data.PeriodRecord
import com.seguimiento.menstruacion.data.PeriodStatistics
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val flowOptions = listOf("Ligero", "Medio", "Abundante")
private val painOptions = (1..10).map { it.toString() }
private val weekDayLabels = listOf("L", "M", "X", "J", "V", "S", "D")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodTrackerScreen(viewModel: PeriodTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.showOnboarding) {
        OnboardingScreen(onFinish = viewModel::completeOnboarding)
        return
    }

    val screenTitle = when (uiState.currentScreen) {
        AppScreen.HOME -> stringResource(R.string.title_home)
        AppScreen.SETTINGS -> stringResource(R.string.title_settings)
        AppScreen.STATISTICS -> stringResource(R.string.title_statistics)
        AppScreen.CREATE_RECORD -> stringResource(R.string.title_create_record)
        AppScreen.HISTORY -> stringResource(R.string.title_history)
        AppScreen.EDIT_RECORD -> stringResource(R.string.title_edit_record)
    }

    Scaffold(
        topBar = {
            if (uiState.currentScreen != AppScreen.HOME) {
                TopAppBar(title = { Text(screenTitle) })
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = uiState.currentScreen,
                onGoHome = viewModel::goToHome,
                onGoCreate = viewModel::goToCreateRecord,
                onGoHistory = viewModel::goToHistory,
                onGoSettings = viewModel::goToSettings
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    records = uiState.records,
                    predictions = uiState.predictions,
                    visibleMonth = uiState.visibleMonth,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onGoToStatistics = viewModel::goToStatistics,
                    onGoToCreate = viewModel::goToCreateRecord,
                    onGoToHistory = viewModel::goToHistory
                )

                AppScreen.STATISTICS -> StatisticsScreen(
                    statistics = uiState.statistics,
                    onBack = viewModel::goToHome
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    remindersEnabled = uiState.remindersEnabled,
                    onSetRemindersEnabled = viewModel::setRemindersEnabled,
                    onBack = viewModel::goToHome
                )

                AppScreen.CREATE_RECORD -> RecordFormScreen(
                    viewModel = viewModel,
                    isEditing = false,
                    onBack = viewModel::goToHome
                )

                AppScreen.HISTORY -> HistoryScreen(
                    records = uiState.records,
                    onEdit = viewModel::editRecord,
                    onDelete = viewModel::deleteRecord,
                    onBack = viewModel::goToHome
                )

                AppScreen.EDIT_RECORD -> RecordFormScreen(
                    viewModel = viewModel,
                    isEditing = true,
                    onBack = viewModel::cancelEdit
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    records: List<PeriodRecord>,
    predictions: PeriodPredictions,
    visibleMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onGoToStatistics: () -> Unit,
    onGoToCreate: () -> Unit,
    onGoToHistory: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CycleCalendar(
                        records = records,
                        predictions = predictions,
                        visibleMonth = visibleMonth,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.label_next_period), style = MaterialTheme.typography.titleSmall)
                    Text(
                        predictions.nextPeriodDate?.format(formatter) ?: stringResource(R.string.value_not_enough_data),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(R.string.label_ovulation_estimated, predictions.ovulationDate?.format(formatter) ?: stringResource(R.string.value_not_enough_data)))
                    Text(stringResource(R.string.label_cycle_avg_days, predictions.averageCycleLengthDays), style = MaterialTheme.typography.bodySmall)
                    PrimaryActionButton(text = stringResource(R.string.action_view_statistics), onClick = onGoToStatistics)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryActionButton(text = stringResource(R.string.action_create), onClick = onGoToCreate, modifier = Modifier.weight(1f))
                PrimaryActionButton(text = stringResource(R.string.action_history), onClick = onGoToHistory, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatisticsScreen(
    statistics: PeriodStatistics,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.label_summary), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.label_cycle_duration_avg, statistics.averageCycleLengthDays))
                Text(stringResource(R.string.label_period_duration_avg, statistics.averagePeriodLengthDays))
                Text(stringResource(R.string.label_cycle_variability, statistics.cycleVariabilityDays))
                Text(stringResource(R.string.label_pain_avg, statistics.averagePainLevel))
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_back))
        }
    }
}

@Composable
private fun SettingsScreen(
    remindersEnabled: Boolean,
    onSetRemindersEnabled: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onSetRemindersEnabled(granted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.label_notifications))
                    Text(stringResource(R.string.label_notifications_desc), style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = { checked ->
                        if (!checked) {
                            onSetRemindersEnabled(false)
                            return@Switch
                        }

                        val needsPermission =
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED

                        if (needsPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onSetRemindersEnabled(true)
                        }
                    }
                )
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_back))
        }
    }
}

@Composable
private fun HistoryScreen(
    records: List<PeriodRecord>,
    onEdit: (PeriodRecord) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (records.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🩷", style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.label_no_records), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.label_no_records_desc),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(onClick = onBack) {
                            Text(stringResource(R.string.action_create_first_record))
                        }
                    }
                }
            }
        } else {
            items(records, key = { it.id }) { record ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val endLabel = record.endDate?.format(formatter) ?: stringResource(R.string.value_ongoing)
                        Text(
                            "${record.startDate.format(formatter)} - $endLabel",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(stringResource(R.string.label_flow_value, record.flowLevel))
                        Text(stringResource(R.string.label_pain_value, record.painLevel))
                        if (record.symptoms.isNotEmpty()) {
                            Text(stringResource(R.string.label_symptoms_value, record.symptoms.joinToString()))
                        }
                        if (record.notes.isNotBlank()) {
                            Text(stringResource(R.string.label_notes_value, record.notes))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onEdit(record) }) { Text(stringResource(R.string.action_edit)) }
                            TextButton(
                                onClick = { onDelete(record.id) },
                            ) {
                                Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordFormScreen(
    viewModel: PeriodTrackerViewModel,
    isEditing: Boolean,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val form = uiState.form
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) } }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.label_ongoing_period))
                Switch(
                    checked = form.isOngoing,
                    onCheckedChange = viewModel::onOngoingChanged
                )
            }
        }
        item {
            PeriodRangeField(
                startDate = form.startDate,
                endDate = form.endDate,
                isOngoing = form.isOngoing,
                onRangeSelected = viewModel::onPeriodRangeChanged
            )
        }
        item {
            FlowSelector(
                selected = form.flowLevel,
                onSelected = viewModel::onFlowChanged
            )
        }
        item { Text(stringResource(R.string.label_common_symptoms)) }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.availableSymptoms().forEach { symptom ->
                    FilterChip(
                        selected = form.selectedSymptoms.contains(symptom),
                        onClick = { viewModel.togglePredefinedSymptom(symptom) },
                        label = { Text(symptom) }
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = form.customSymptomsText,
                onValueChange = viewModel::onCustomSymptomsChanged,
                label = { Text(stringResource(R.string.label_other_symptoms)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            PainSelector(
                selected = form.painLevel,
                onSelected = viewModel::onPainChanged
            )
        }
        item {
            OutlinedTextField(
                value = form.notes,
                onValueChange = viewModel::onNotesChanged,
                label = { Text(stringResource(R.string.label_notes)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        form.error?.let { errorText ->
            item { Text(errorText, color = MaterialTheme.colorScheme.error) }
        }
        item {
            Button(onClick = viewModel::saveRecord, modifier = Modifier.fillMaxWidth()) {
                Text(if (isEditing) stringResource(R.string.action_update_record) else stringResource(R.string.action_save_record))
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentScreen: AppScreen,
    onGoHome: () -> Unit,
    onGoCreate: () -> Unit,
    onGoHistory: () -> Unit,
    onGoSettings: () -> Unit
) {
    Surface(tonalElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = stringResource(R.string.nav_home),
                selected = currentScreen == AppScreen.HOME,
                onClick = onGoHome
            )
            BottomNavItem(
                label = stringResource(R.string.nav_create),
                selected = currentScreen == AppScreen.CREATE_RECORD,
                onClick = onGoCreate
            )
            BottomNavItem(
                label = stringResource(R.string.nav_history),
                selected = currentScreen == AppScreen.HISTORY || currentScreen == AppScreen.EDIT_RECORD,
                onClick = onGoHistory
            )
            BottomNavItem(
                label = stringResource(R.string.nav_settings),
                selected = currentScreen == AppScreen.SETTINGS,
                onClick = onGoSettings
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        stringResource(R.string.onboard_title_1) to stringResource(R.string.onboard_desc_1),
        stringResource(R.string.onboard_title_2) to stringResource(R.string.onboard_desc_2),
        stringResource(R.string.onboard_title_3) to stringResource(R.string.onboard_desc_3)
    )
    var currentPage by remember { mutableStateOf(0) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.onboard_welcome), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.onboard_step, currentPage + 1, pages.size))
                Spacer(modifier = Modifier.height(16.dp))
                Text(pages[currentPage].first, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(pages[currentPage].second)
            }

            Button(
                onClick = {
                    if (currentPage < pages.lastIndex) currentPage += 1 else onFinish()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (currentPage < pages.lastIndex) stringResource(R.string.action_next) else stringResource(R.string.action_get_started))
            }
        }
    }
}

@Composable
private fun CycleCalendar(
    records: List<PeriodRecord>,
    predictions: PeriodPredictions,
    visibleMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    var horizontalDragAccumulated by remember { mutableStateOf(0f) }
    val swipeThreshold = 72f

    val firstDay = visibleMonth.atDay(1)
    val leadingEmptyCells = firstDay.dayOfWeek.value - 1
    val allDates = mutableListOf<LocalDate?>()

    repeat(leadingEmptyCells) { allDates.add(null) }
    (1..visibleMonth.lengthOfMonth()).forEach { day -> allDates.add(visibleMonth.atDay(day)) }
    while (allDates.size % 7 != 0) {
        allDates.add(null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(visibleMonth) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        horizontalDragAccumulated += dragAmount
                    },
                    onDragEnd = {
                        when {
                            horizontalDragAccumulated <= -swipeThreshold -> onNextMonth()
                            horizontalDragAccumulated >= swipeThreshold -> onPreviousMonth()
                        }
                        horizontalDragAccumulated = 0f
                    },
                    onDragCancel = {
                        horizontalDragAccumulated = 0f
                    }
                )
            }
    ) {
        AnimatedContent(
            targetState = visibleMonth,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "calendar_month"
        ) { month ->
            Text(
                "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekDayLabels.forEach { Text(it, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold) }
        }

        allDates.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val marker = date?.let { resolveMarker(it, records, predictions) } ?: DayMarker.NONE
                    val containerColor = when (marker) {
                        DayMarker.PERIOD -> MaterialTheme.colorScheme.primaryContainer
                        DayMarker.FERTILE -> MaterialTheme.colorScheme.tertiaryContainer
                        DayMarker.OVULATION -> MaterialTheme.colorScheme.secondaryContainer
                        DayMarker.NONE -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val textColor = when (marker) {
                        DayMarker.PERIOD -> MaterialTheme.colorScheme.onPrimaryContainer
                        DayMarker.FERTILE -> MaterialTheme.colorScheme.onTertiaryContainer
                        DayMarker.OVULATION -> MaterialTheme.colorScheme.onSecondaryContainer
                        DayMarker.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = date?.dayOfMonth?.toString() ?: "", color = textColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = onClick, modifier = modifier) {
        Text(text)
    }
}
private enum class DayMarker { NONE, PERIOD, FERTILE, OVULATION }

private fun resolveMarker(date: LocalDate, records: List<PeriodRecord>, predictions: PeriodPredictions): DayMarker {
    val isPeriodDay = records.any { record ->
        val end = record.endDate ?: LocalDate.now()
        !date.isBefore(record.startDate) && !date.isAfter(end)
    }
    if (isPeriodDay) return DayMarker.PERIOD

    val ovulation = predictions.ovulationDate
    if (ovulation != null) {
        if (date == ovulation) return DayMarker.OVULATION
        if (!date.isBefore(ovulation.minusDays(5)) && !date.isAfter(ovulation)) return DayMarker.FERTILE
    }
    return DayMarker.NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodRangeField(
    startDate: String,
    endDate: String,
    isOngoing: Boolean,
    onRangeSelected: (String, String?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val minDate = YearMonth.now().atDay(1)
    val minDateMillis = minDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val maxDateMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val rangePickerState = androidx.compose.material3.rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate.toDateMillisOrNull(),
        initialSelectedEndDateMillis = endDate.toDateMillisOrNull(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis in minDateMillis..maxDateMillis
            }
        }
    )

    val displayText = when {
        startDate.isBlank() -> stringResource(R.string.value_no_period_selected)
        isOngoing -> "${startDate.toDisplayDate()} - ${stringResource(R.string.value_ongoing)}"
        endDate.isBlank() -> startDate.toDisplayDate()
        else -> "${startDate.toDisplayDate()} - ${endDate.toDisplayDate()}"
    }

    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.label_period)) },
        modifier = Modifier.fillMaxWidth()
    )

    Button(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_select_period))
    }

    if (showPicker) {
        val persistSelection = {
            rangePickerState.selectedStartDateMillis?.let { startMillis ->
                val start = startMillis.toIsoLocalDate()
                val end = rangePickerState.selectedEndDateMillis?.toIsoLocalDate()
                onRangeSelected(start, end)
            }
        }

        DatePickerDialog(
            onDismissRequest = {
                persistSelection()
                showPicker = false
            },
            confirmButton = {
                TextButton(onClick = {
                    persistSelection()
                    showPicker = false
                }) {
                    Text(stringResource(R.string.action_accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DateRangePicker(state = rangePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlowSelector(
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_flow_amount)) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_select_amount))
        }

        DropdownMenu(
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

@Composable
private fun PainSelector(
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_pain_scale)) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_select_pain))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            painOptions.forEach { option ->
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

private fun String.toDateMillisOrNull(): Long? {
    return runCatching {
        LocalDate.parse(this)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun String.toDisplayDate(): String {
    return runCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrDefault(this)
}

private fun Long.toIsoLocalDate(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}
