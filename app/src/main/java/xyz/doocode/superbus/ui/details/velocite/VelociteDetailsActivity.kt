package xyz.doocode.superbus.ui.details.velocite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import xyz.doocode.superbus.ui.theme.SuperBusTheme

class VelociteDetailsActivity : ComponentActivity() {
    private val viewModel: VelociteDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val stationId = intent.getIntExtra(EXTRA_STATION_ID, -1)
        val stationName = intent.getStringExtra(EXTRA_STATION_NAME) ?: ""

        if (stationId != -1) {
            viewModel.setStationId(stationId, stationName)
        }

        setContent {
            SuperBusTheme {
                VelociteDetailsScreen(
                    stationName = stationName,
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_STATION_ID = "extra_station_id"
        const val EXTRA_STATION_NAME = "extra_station_name"
    }
}
