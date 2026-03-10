package xyz.doocode.superbus.ui.lines

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import xyz.doocode.superbus.ui.theme.SuperBusTheme

class LinesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperBusTheme {
                LinesScreen(
                    onNavigateBack = { finish() },
                    onLineClick = { line ->
                        // Show line details - for now just a toast as details activity is not specified
                        // Or if StopDetailsActivity can show Line Details? Probably not.
                        // I'll show a toast for now.
                        Toast.makeText(
                            this,
                            "Ligne ${line.numLignePublic} sélectionnée",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}
