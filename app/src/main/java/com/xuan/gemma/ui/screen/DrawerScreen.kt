package com.xuan.gemma.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xuan.gemma.R
import com.xuan.gemma.data.NavigationItem
import com.xuan.gemma.util.PickImageFunc
import com.xuan.gemma.util.PickImageUsingCamera
import com.xuan.gemma.util.RecordFunc
import kotlinx.coroutines.launch

object MainFunc {

    private val items = listOf(
        NavigationItem(
            title = "All",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        NavigationItem(
            title = "Urgent",
            selectedIcon = Icons.Filled.Info,
            unselectedIcon = Icons.Outlined.Info,
            badgeCount = 45
        ),
        NavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyDrawerLayout(
        drawerState: DrawerState,
        recordFunc: RecordFunc,
        pickImageFunc: PickImageFunc,
        pickImageUsingCamera: PickImageUsingCamera
    ) {

        val scope = rememberCoroutineScope()
        var selectedItemIndex by rememberSaveable { mutableIntStateOf(-1) }
        var text by remember { mutableStateOf("") }
        var active by remember { mutableStateOf(false) }

        LaunchedEffect(drawerState.currentValue) { if (!drawerState.isOpen) { active = false } }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet (
                    modifier = Modifier.fillMaxWidth(0.85f)
                ){
                    SearchBar(
                        query = text,
                        onQueryChange = { text = it },
                        onSearch = { active = false },
                        active = active,
                        onActiveChange = { active = it },
                        placeholder = { Text(stringResource(id = R.string.searchEditText)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .padding(if (!active) 16.dp else 0.dp)
                            .fillMaxWidth(),
                    ){}

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        itemsIndexed(items) { index, item ->
                            NavigationDrawerItem(
                                label = {
                                    Text(text = item.title)
                                },
                                selected = index == selectedItemIndex,
                                onClick = {
                                    selectedItemIndex = index
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                },
                                badge = {
                                    item.badgeCount?.let {
                                        Text(text = it.toString())
                                    }
                                },
                                modifier = Modifier
                                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                }
            }
        ) {
            Scaffold { paddingValues ->
                ChatRoute(
                    drawerState = drawerState,
                    recordFunc = recordFunc,
                    pickImageFunc = pickImageFunc,
                    pickImageUsingCamera = pickImageUsingCamera,
                    paddingValues = paddingValues,
                    active = active,
                    onActiveChange = { active = it }
                )
            }
        }
    }
}
