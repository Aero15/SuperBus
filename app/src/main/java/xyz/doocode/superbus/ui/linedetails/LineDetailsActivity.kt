package xyz.doocode.superbus.ui.linedetails

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import xyz.doocode.superbus.ui.details.StopDetailsActivity
import xyz.doocode.superbus.ui.theme.SuperBusTheme

class LineDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperBusTheme {
                LineDetailsScreen(
                    onNavigateBack = { finish() },
                    onStopClick = { stop ->
                        val intent = android.content.Intent(this, StopDetailsActivity::class.java)
                        intent.putExtra(StopDetailsActivity.EXTRA_STOP_ID, stop.id)
                        intent.putExtra(StopDetailsActivity.EXTRA_STOP_NAME, stop.nom)
                        intent.putExtra(StopDetailsActivity.EXTRA_DETAILS_FROM_ID, true)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
