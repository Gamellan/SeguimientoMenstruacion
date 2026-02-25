package com.seguimiento.menstruacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.seguimiento.menstruacion.data.AppPreferences
import com.seguimiento.menstruacion.data.PeriodDatabase
import com.seguimiento.menstruacion.data.PeriodRepository
import com.seguimiento.menstruacion.notifications.ReminderScheduler
import com.seguimiento.menstruacion.ui.PeriodTrackerScreen
import com.seguimiento.menstruacion.ui.PeriodTrackerViewModel
import com.seguimiento.menstruacion.ui.PeriodTrackerViewModelFactory
import com.seguimiento.menstruacion.ui.theme.PeriodAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PeriodTrackerViewModel by lazy {
        ViewModelProvider(
            this,
            PeriodTrackerViewModelFactory(
                repository = PeriodRepository(
                    PeriodDatabase.getDatabase(applicationContext).periodRecordDao()
                ),
                preferences = AppPreferences(applicationContext),
                reminderScheduler = ReminderScheduler(applicationContext)
            )
        )[PeriodTrackerViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeriodAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PeriodTrackerScreen(viewModel = viewModel)
                }
            }
        }
    }
}
