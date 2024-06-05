package com.xuan.gemini.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xuan.gemini.R
import com.xuan.gemini.data.NavigationItem
import com.xuan.gemini.ui.compose.MainLayout
import com.xuan.gemini.util.PickImageFunc
import com.xuan.gemini.util.PickImageUsingCamera
import com.xuan.gemini.util.RecordFunc
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

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet (
                    modifier = Modifier.fillMaxWidth(0.85f)
                ){
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        label = { Text(stringResource(id = R.string.searchEditText)) },
                        shape = RoundedCornerShape(50.dp),
                        keyboardOptions = KeyboardOptions.Default,
                        keyboardActions = KeyboardActions.Default,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(id = R.string.searchEditText)
                            )
                        }
                    )

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
                MainLayout(
                    drawerState = drawerState,
                    recordFunc = recordFunc,
                    pickImageFunc = pickImageFunc,
                    pickImageUsingCamera = pickImageUsingCamera,
                    paddingValues = paddingValues
                )
            }
        }
    }
}
