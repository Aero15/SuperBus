package xyz.doocode.superbus.ui.details.velocite.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import androidx.core.net.toUri

@Composable
fun VelociteAddressCard(station: Station) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val uri = ("geo:${station.position.latitude},${station.position.longitude}" +
                            "?q=${station.position.latitude},${station.position.longitude}" +
                            "(${Uri.encode(station.name)})").toUri()
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
                .padding(start = 18.dp, top = 12.dp, bottom = 12.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = "Ouvrir dans Maps",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            // Address
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Emplacement",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = station.address, style = MaterialTheme.typography.bodyMedium)
            }

            // Station number
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                        append("#")
                    }
                    withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)) {
                        append("${station.number}")
                    }
                }
            )
        }
    }
}
