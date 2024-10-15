package com.xuan.gemma.ui.compose

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.xuan.gemma.R
import com.xuan.gemma.database.Message
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableHistoryList(
    listHistory: List<Message>,
    onItemClicked: (Message) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val filteredList = if (text.isEmpty()) {
        listHistory
    }
    else {
        listHistory.filter { message ->
            message.title.contains(text, ignoreCase = true) ||
                    message.messages.any { it.message.contains(text, ignoreCase = true) }
        }
    }

    SearchBar(
        query = text,
        onQueryChange = { text = it },
        onSearch = {  },
        active = active,
        onActiveChange = { onActiveChange(it) },
        placeholder = { Text(stringResource(id = R.string.searchEditText)) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (active) {
                IconButton(onClick = {
                    scope.launch { onActiveChange(false) }
                    text = ""
                }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                }
            }
       },
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (active) {
            LazyColumn {
                itemsIndexed(filteredList) { _, item ->
                    HistoryItem(
                        message = item,
                        dropdownItems = listOf(
                            DropDownItem("Item 1"),
                            DropDownItem("Item 2"),
                            DropDownItem("Item 3"),
                        ),
                        onItemClick = { message ->
                            println("TestXuan: "+message.title+": "+message.date)
                            onItemClicked(message)
                            scope.launch { onActiveChange(false) }
                            text = ""
                        },
                        onItemLongClick = {
                            println("TestXuan: "+it.text)
                        }
                    )
                }
            }
        }
    }
}
