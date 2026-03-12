package xyz.doocode.superbus.ui.lines

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.gson.Gson
import xyz.doocode.superbus.ui.linedetails.LineDetailsActivity
import xyz.doocode.superbus.ui.theme.SuperBusTheme

class LinesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperBusTheme {
                LinesScreen(
                    onNavigateBack = { finish() },
                    onVariantSelected = { line, variante ->
                        val intent = Intent(this, LineDetailsActivity::class.java)
                        intent.putExtra("EXTRA_LINE_ID", line.id)
                        intent.putExtra("EXTRA_VARIANT_ID", variante.id)
                        // Pass the full line object as JSON to display header info and allow switching variants
                        intent.putExtra("EXTRA_LINE_JSON", Gson().toJson(line))
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
