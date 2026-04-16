package xyz.doocode.superbus.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MainFeaturesGrid(onItemClick: (MenuFeature) -> Unit, columns: Int = 2) {
    Column(modifier = Modifier.padding(horizontal = if (columns == 2) 16.dp else 0.dp)) {
        val topFeatures = listOf(/*MenuFeature.MAP,*/ MenuFeature.VELOCITE,
            MenuFeature.FAVORITES,
            MenuFeature.LINES
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topFeatures.forEach { feature ->
                CompactFeatureTile(
                    feature = feature,
                    onClick = { onItemClick(feature) },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val remainingFeatures = MenuFeature.entries.filter { !topFeatures.contains(it) }

        remainingFeatures.chunked(2).forEachIndexed { index, rowFeatures ->
            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowFeatures.forEach { feature ->
                    FeatureTile(
                        feature = feature,
                        onClick = { onItemClick(feature) },
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                    )
                }
                if (rowFeatures.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CompactFeatureTile(
    feature: MenuFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (feature) {
        //MenuFeature.MAP -> MaterialTheme.colorScheme.tertiary
        MenuFeature.VELOCITE -> MaterialTheme.colorScheme.tertiary
        MenuFeature.FAVORITES -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (feature) {
        //MenuFeature.MAP -> MaterialTheme.colorScheme.onTertiary
        MenuFeature.VELOCITE -> MaterialTheme.colorScheme.onTertiary
        MenuFeature.FAVORITES -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.label,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Composable
fun FeatureTile(
    feature: MenuFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: androidx.compose.ui.unit.Dp = 32.dp
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(iconSize)
            )
            Text(
                text = feature.label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeaturesGridPreview() {
    MaterialTheme {
        MainFeaturesGrid(onItemClick = {})
    }
}
