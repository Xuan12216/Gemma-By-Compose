package com.xuan.gemma.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (active) {
            LazyColumn {
                itemsIndexed(filteredList) { _, item ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable {
                                onItemClicked(item)
                                scope.launch { onActiveChange(false) }
                            }
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
