package xyz.doocode.superbus.ui.search.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class SearchFilterOption {
    NONE,
    BUS_TRAMS,
    VELOCITE
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: SearchFilterOption,
    onFilterSelected: (SearchFilterOption) -> Unit,
    placeholder: String = "Rechercher",
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val hidePlaceholder = isFocused || selectedFilter != SearchFilterOption.NONE
    val placeholderAlpha by animateFloatAsState(
        targetValue = if (hidePlaceholder) 0f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "placeholderAlpha"
    )
    val canClear = query.isNotEmpty() || selectedFilter != SearchFilterOption.NONE

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .onFocusChanged { isFocused = it.isFocused },
        placeholder = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = placeholder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = placeholderAlpha },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        },
        leadingIcon = {
            FilterDropdown(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    onQueryChange("")
                    onFilterSelected(SearchFilterOption.NONE)
                    focusManager.clearFocus()
                },
                enabled = canClear
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Réinitialiser la recherche",
                    tint = if (canClear) MaterialTheme.colorScheme.onSurfaceVariant
                    else LocalContentColor.current.copy(alpha = 0.35f)
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
        })
    )
}

@Composable
private fun FilterDropdown(
    selectedFilter: SearchFilterOption,
    onFilterSelected: (SearchFilterOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isActive = selectedFilter != SearchFilterOption.NONE
    val activeLabel = when (selectedFilter) {
        SearchFilterOption.NONE -> ""
        SearchFilterOption.BUS_TRAMS -> "Bus & trams"
        SearchFilterOption.VELOCITE -> "Vélocité"
    }
    val activeIcon = when (selectedFilter) {
        SearchFilterOption.NONE -> Icons.Default.Search
        SearchFilterOption.BUS_TRAMS -> Icons.Default.DirectionsBus
        SearchFilterOption.VELOCITE -> Icons.AutoMirrored.Filled.DirectionsBike
    }

    if (isActive) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 6.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .clickable { expanded = true }
                .padding(start = 12.dp, end = 14.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = activeIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 6.dp)
            )
            Text(
                text = activeLabel,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ouvrir les filtres",
                tint = LocalContentColor.current
            )
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        SearchFilterOption.entries.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = when (option) {
                            SearchFilterOption.NONE -> "Aucun filtre"
                            SearchFilterOption.BUS_TRAMS -> "Bus & trams"
                            SearchFilterOption.VELOCITE -> "Vélocité"
                        }
                    )
                },
                leadingIcon = {
                    when (option) {
                        SearchFilterOption.NONE -> {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }

                        SearchFilterOption.BUS_TRAMS -> {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null
                            )
                        }

                        SearchFilterOption.VELOCITE -> {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                                contentDescription = null
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (option == selectedFilter) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Sélectionné",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                onClick = {
                    onFilterSelected(option)
                    expanded = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(SearchFilterOption.NONE) }

    SearchBar(
        query = query,
        onQueryChange = { query = it },
        selectedFilter = filter,
        onFilterSelected = { filter = it }
    )
}
