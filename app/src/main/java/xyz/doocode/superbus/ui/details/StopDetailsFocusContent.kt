package xyz.doocode.superbus.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.details.components.FocusArrivalCard

@Composable
fun StopDetailsFocusContent(
    arrivalsList: List<Pair<String, List<Temps>>>,
    focusedItemKey: String?,
    onFocusedItemChanged: (String) -> Unit
) {
    val initialPage = remember(focusedItemKey, arrivalsList) {
        if (focusedItemKey != null) {
            val index = arrivalsList.indexOfFirst { it.first == focusedItemKey }
            if (index >= 0) index else 0
        } else 0
    }

    val pagerState = rememberPagerState(initialPage = initialPage) {
        arrivalsList.size
    }

    LaunchedEffect(pagerState, arrivalsList) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (focusedItemKey != null && page in arrivalsList.indices) {
                onFocusedItemChanged(arrivalsList[page].first)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(0.dp),
                pageSpacing = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(this@BoxWithConstraints.maxHeight),
                verticalAlignment = Alignment.Top
            ) { page ->
                val (key, arrivals) = arrivalsList[page]
                val parts = key.split("|")
                FocusArrivalCard(
                    numLigne = parts.getOrNull(0) ?: "?",
                    destination = parts.getOrNull(1) ?: "?",
                    couleurFond = arrivals.first().couleurFond,
                    couleurTexte = arrivals.first().couleurTexte,
                    times = arrivals
                )
            }

            if (pagerState.pageCount > 1) {
                Row(
                    Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }
        }
    }
}
