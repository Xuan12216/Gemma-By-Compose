package com.xuan.gemma.viewmodel

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.twotone.Star
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.xuan.gemma.data.NavigationItem
import com.xuan.gemma.database.Message
import com.xuan.gemma.database.MessageRepository

class DrawerViewModel(context: Context) : ViewModel() {
    var selectedItemIndex by mutableIntStateOf(0)
    var active by mutableStateOf(false)
    var listHistory: List<Message> by mutableStateOf(emptyList())
    var selectedMessage by mutableStateOf<Message?>(null)
    var renameMessage by mutableStateOf<Message?>(null)
    var showRenameDialog by mutableStateOf(false)
    var deleteMessage by mutableStateOf<Message?>(null)
    var showDeleteDialog by mutableStateOf(false)
    var isRefreshListHistory by mutableStateOf(false)

    val repository = MessageRepository(context)

    val items = listOf(
        NavigationItem("Gemma", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("Gemini", Icons.Filled.Star, Icons.TwoTone.Star),
        NavigationItem("Info", Icons.Filled.Info, Icons.Outlined.Info),
        NavigationItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    fun getType (): String { return items[selectedItemIndex].title }

    suspend fun refreshListHistory(type: String?) {
        listHistory = repository.getAllMessages(type)
        isRefreshListHistory = false
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return DrawerViewModel(context) as T
            }
        }
    }
}