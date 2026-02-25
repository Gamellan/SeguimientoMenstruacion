package com.seguimiento.menstruacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.seguimiento.menstruacion.data.PeriodDatabase
import com.seguimiento.menstruacion.data.PeriodRepository
import com.seguimiento.menstruacion.ui.PeriodTrackerScreen
import com.seguimiento.menstruacion.ui.PeriodTrackerViewModel
import com.seguimiento.menstruacion.ui.PeriodTrackerViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: PeriodTrackerViewModel by lazy {
        ViewModelProvider(
            this,
            PeriodTrackerViewModelFactory(
                repository = PeriodRepository(
                    PeriodDatabase.getDatabase(applicationContext).periodRecordDao()
                )
            )
        )[PeriodTrackerViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PeriodTrackerScreen(viewModel = viewModel)
                }
            }
        }
    }
}
