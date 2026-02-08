package xyz.doocode.superbus.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.dto.FavoriteStation
import xyz.doocode.superbus.ui.components.SearchBar

@Composable
fun FavoritesScreen(
    onStationClick: (FavoriteStation) -> Unit,
    onSearchClick: () -> Unit, // Navigate to search to add new
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        // --- Header & Search ---
        Text(
            text = "Favoris",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        /*SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            placeholder = "Rechercher...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )*/

        if (favorites.isNotEmpty()) {
            Text(
                text = "${favorites.size} station(s)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (favorites.isEmpty()) {
            if (searchQuery.isNotEmpty()) {
                // Empty search result
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucun favori ne correspond à votre recherche.")
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Aucun favori pour le moment.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onSearchClick) {
                            Text("Trouver une station")
                        }
                    }
                }
            }
        } else {
            // Grid of Tiles
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favorites, key = { it.id }) { station ->
                    FavoriteTile(
                        station = station,
                        onClick = { onStationClick(station) }
                    )
                }
            }
        }
    }
}
