package com.xuan.gemma.ui.screen

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.xuan.gemma.R
import com.xuan.gemma.data.NavigationItem
import com.xuan.gemma.database.Message
import com.xuan.gemma.`object`.Constant
import com.xuan.gemma.ui.compose.AppBar
import com.xuan.gemma.ui.compose.DeleteMessageDialog
import com.xuan.gemma.ui.compose.DropDownItem
import com.xuan.gemma.ui.compose.RenameMessageDialog
import com.xuan.gemma.ui.compose.SearchableHistoryList
import com.xuan.gemma.viewmodel.DrawerViewModel
import kotlinx.coroutines.launch

@Composable
fun MyDrawerLayout(
    drawerState: DrawerState,
) {
    val scope = rememberCoroutineScope()
    val viewModel: DrawerViewModel = viewModel(factory = DrawerViewModel.getFactory(LocalContext.current))

    // 控制抽屜打開/關閉時的邏輯
    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen) viewModel.active = false
        viewModel.isRefreshListHistory = true
    }

    // 刷新消息列表
    LaunchedEffect(viewModel.isRefreshListHistory) {
        if (viewModel.isRefreshListHistory) viewModel.refreshListHistory(viewModel.getType())
    }

    // 如果點擊重命名，顯示彈出框
    if (viewModel.showRenameDialog && viewModel.renameMessage != null) {
        RenameMessageDialog(
            message = viewModel.renameMessage!!,
            repository = viewModel.repository,
            onDismiss = {
                viewModel.showRenameDialog = false
                viewModel.isRefreshListHistory = true
            }
        )
    }

    if (viewModel.showDeleteDialog && viewModel.deleteMessage != null) {
        DeleteMessageDialog(
            message = viewModel.deleteMessage!!,
            repository = viewModel.repository,
            onDismiss = {
                viewModel.showDeleteDialog = false
                viewModel.isRefreshListHistory = true
            }
        )
    }

    // 主體結構
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {

            val animatedWidth by animateFloatAsState(
                targetValue = if (viewModel.active) 1f else 0.75f,
                animationSpec = tween(
                    durationMillis = 250,
                    delayMillis = 150,
                    easing = LinearOutSlowInEasing
                ),
                label = "Drawer Width Animation"
            )

            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(animatedWidth)
            ){
                DrawerContent(
                    items = viewModel.items,
                    selectedItemIndex = viewModel.selectedItemIndex,
                    active = viewModel.active,
                    onItemClicked = { index ->
                        viewModel.selectedItemIndex = index
                        scope.launch { drawerState.close() }
                    },
                    onSearchHistoryItemClicked = { viewModel.selectedMessage = it },
                    listHistory = viewModel.listHistory,
                    onActiveChange = { viewModel.active = it },
                    onItemLongClick = { dropDownItem, message ->
                        when (dropDownItem.text) {
                            Constant.PIN -> {
                                scope.launch {
                                    viewModel.repository.insertOrUpdateMessage(message.copy(isPinned = !message.isPinned))
                                    viewModel.isRefreshListHistory = true
                                }
                            }
                            Constant.RENAME -> {
                                viewModel.renameMessage = message
                                viewModel.showRenameDialog = true
                            }
                            Constant.DELETE -> {
                                viewModel.deleteMessage = message
                                viewModel.showDeleteDialog = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        Scaffold { paddingValues ->
            when (viewModel.selectedItemIndex) {
                0 -> ChatRoute(
                    drawerState = drawerState,
                    paddingValues = paddingValues,
                    active = viewModel.active,
                    onActiveChange = { viewModel.active = it },
                    type = viewModel.getType(),
                    selectedMessage = viewModel.selectedMessage,
                    onSelectedMessageClear = {
                        viewModel.selectedMessage = null
                        scope.launch { drawerState.close() }
                    }
                )
                1 -> GeminiLayout(
                    drawerState = drawerState,
                    paddingValues = paddingValues,
                    active = viewModel.active,
                    onActiveChange = { viewModel.active = it },
                    type = viewModel.getType(),
                    selectedMessage = viewModel.selectedMessage,
                    onSelectedMessageClear = {
                        viewModel.selectedMessage = null
                        scope.launch { drawerState.close() }
                    }
                )
                2 -> DrawerScreen("Urgent", drawerState, paddingValues)
                3 -> DrawerScreen("Settings", drawerState, paddingValues)
                else -> DrawerScreen("None", drawerState, paddingValues)
            }
        }
    }
}

@Composable
fun DrawerContent(
    items: List<NavigationItem>,
    selectedItemIndex: Int,
    active: Boolean,
    onItemClicked: (Int) -> Unit,
    onSearchHistoryItemClicked: (Message) -> Unit,
    listHistory: List<Message>,
    onActiveChange: (Boolean) -> Unit,
    onItemLongClick: (DropDownItem, Message) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = if (active) 0.dp else 16.dp, end = if (active) 0.dp else 16.dp)
            .fillMaxSize()
    ) {
        //title and logo
        if (!active) DrawerHeader(items[selectedItemIndex].title)

        //history search
        SearchableHistoryList(
            listHistory = listHistory,
            onItemClicked = onSearchHistoryItemClicked,
            active = active,
            onActiveChange = onActiveChange,
            onItemLongClick = {  dropDownItem, message ->
                onItemLongClick(dropDownItem, message)
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        //drawer item
        LazyColumn {
            itemsIndexed(items) { index, item ->
                DrawerItem(
                    item = item,
                    selected = index == selectedItemIndex,
                    onClick = { onItemClicked(index) }
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DrawerHeader(title: String) {
    Row(
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 4.dp)
    ) {
        Text(
            text = title,
            maxLines = 1,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        GlideImage(
            model = R.drawable.sparkle_resting,
            contentDescription = "logo",
            modifier = Modifier
                .padding(start = 8.dp)
                .width(28.dp)
                .height(28.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun DrawerItem(
    item: NavigationItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(text = item.title) },
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.title
            )
        },
        badge = { item.badgeCount?.let { Text(text = it.toString()) } },
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
fun DrawerScreen(
    title: String,
    drawerState: DrawerState,
    paddingValues: PaddingValues
) {
    val scope = rememberCoroutineScope()
    AppBar(
        textInputEnabled = true,
        iconBtn1Onclick = { scope.launch { drawerState.open() } },
        iconBtn1Painter = painterResource(id = R.drawable.baseline_menu_24),
        iconBtn1Content = "Open Drawer Navigation",
        animatedText = ""
    )
}
