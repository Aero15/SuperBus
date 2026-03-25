package xyz.doocode.superbus.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.ui.components.EmptyDataView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LoadingView
import xyz.doocode.superbus.ui.details.components.ArrivalCard

@Composable
fun StopDetailsListContent(
    state: StopDetailsUiState,
    forcedExpandState: Boolean?,
    onRetry: () -> Unit,
    onItemLongClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        when (state) {
            is StopDetailsUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingView("Chargement des temps d'attente...")
                    }
                }
            }

            is StopDetailsUiState.Error -> {
                item { ErrorView(state.message, onRetry = onRetry) }
            }

            is StopDetailsUiState.Empty -> {
                item { EmptyDataView() }
            }

            is StopDetailsUiState.Success -> {
                val list = state.groupedArrivals.toList()
                items(list) { (key, arrivals) ->
                    val parts = key.split("|")
                    ArrivalCard(
                        numLigne = parts.getOrNull(0) ?: "?",
                        destination = parts.getOrNull(1) ?: "?",
                        couleurFond = arrivals.first().couleurFond,
                        couleurTexte = arrivals.first().couleurTexte,
                        times = arrivals.take(3),
                        initialExpoMode = list.size < 4,
                        forcedExpandState = forcedExpandState,
                        onLongClick = { onItemLongClick(key) }
                    )
                }
            }
        }
    }
}
