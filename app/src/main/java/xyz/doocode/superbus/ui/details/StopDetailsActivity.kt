package xyz.doocode.superbus.ui.details

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import xyz.doocode.superbus.ui.theme.SuperBusTheme

class StopDetailsActivity : ComponentActivity() {

    companion object {
        const val EXTRA_STOP_NAME = "extra_stop_name"
        const val EXTRA_STOP_ID = "extra_stop_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val stopName = intent.getStringExtra(EXTRA_STOP_NAME)
        val stopId = intent.getStringExtra(EXTRA_STOP_ID)

        setContent {
            SuperBusTheme {
                StopDetailsScreen(
                    stopName = stopName,
                    stopId = stopId,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
