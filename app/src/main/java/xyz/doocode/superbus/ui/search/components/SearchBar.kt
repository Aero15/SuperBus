package xyz.doocode.superbus.ui.search.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text(text = placeholder) },
        leadingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Effacer"
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Recherche"
                )
            }
        },
        trailingIcon = {
            FilterDropdown(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
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

    IconButton(onClick = { expanded = true }) {
        val iconTint =
            if (selectedFilter == SearchFilterOption.NONE) LocalContentColor.current
            else MaterialTheme.colorScheme.primary

        when (selectedFilter) {
            SearchFilterOption.NONE -> {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Aucun filtre",
                    tint = iconTint
                )
            }

            SearchFilterOption.BUS_TRAMS -> {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = "Bus & trams",
                    tint = iconTint
                )
            }

            SearchFilterOption.VELOCITE -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                    contentDescription = "Vélocité",
                    tint = iconTint
                )
            }
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
