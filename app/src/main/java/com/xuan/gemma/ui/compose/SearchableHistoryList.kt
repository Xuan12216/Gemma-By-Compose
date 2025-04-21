package com.xuan.gemma.ui.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xuan.gemma.R
import com.xuan.gemma.database.Message
import com.xuan.gemma.`object`.Constant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableHistoryList(
    listHistory: List<Message>,
    onItemClicked: (Message) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onItemLongClick: (DropDownItem, Message) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(active) { if (!active) { text = "" } }

    val filteredList = if (text.isEmpty()) { listHistory }
    else {
        listHistory.filter { message ->
            message.title.contains(text, ignoreCase = true) ||
                    message.messages.any { it.message.contains(text, ignoreCase = true) }
        }
    }

    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        inputField = {
            SearchBarDefaults. InputField(
                query = text,
                onQueryChange = { text = it },
                onSearch = { },
                expanded = active,
                onExpandedChange = { onActiveChange(it) },
                placeholder = { Text(stringResource(id = R.string.searchEditText)) },
                leadingIcon = { Icon(Icons. Default. Search, contentDescription = null) },
                trailingIcon = {
                    if (active) {
                        IconButton(onClick = {
                            scope.launch { onActiveChange(false) }
                            text = ""
                        }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )
        },
        expanded = active,
        onExpandedChange = { onActiveChange(it) },
        windowInsets = if (active) SearchBarDefaults.windowInsets else WindowInsets(0.dp)
    ){
        var previousDate = ""
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(280L)
            isVisible = true
        }

        if (isVisible && active) {
            LazyColumn {
                itemsIndexed(filteredList) { index, item ->
                    val currentDate = item.date.substring(0, 10) // Extract "yyyy/MM/dd"
                    val showDate = if (index == 0) true else currentDate != previousDate
                    previousDate = currentDate

                    HistoryItem(
                        message = item,
                        dropdownItems = listOf(
                            DropDownItem(Constant.PIN),
                            DropDownItem(Constant.RENAME),
                            DropDownItem(Constant.DELETE),
                        ),
                        onItemClick = { message ->
                            onItemClicked(message)
                            scope.launch { onActiveChange(false) }
                            text = ""
                        },
                        onItemLongClick = { dropDownItem, message ->
                            onItemLongClick(dropDownItem, message)
                        },
                        showDate = showDate // Pass the flag to show date
                    )
                }
            }
        }
    }
}
