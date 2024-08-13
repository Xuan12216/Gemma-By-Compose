package com.xuan.gemma.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.xuan.gemma.R
import com.xuan.gemma.data.NavigationItem
import com.xuan.gemma.database.Message
import com.xuan.gemma.database.MessageRepository
import com.xuan.gemma.ui.compose.AppBar
import com.xuan.gemma.ui.compose.SearchableHistoryList
import com.xuan.gemma.util.PickImageFunc
import com.xuan.gemma.util.PickImageUsingCamera
import com.xuan.gemma.util.RecordFunc
import kotlinx.coroutines.launch

object MainFunc {

    private val items = listOf(
        NavigationItem("Gemma", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("Gemini", Icons.Filled.Star, Icons.TwoTone.Star),
        NavigationItem("Urgent", Icons.Filled.Info, Icons.Outlined.Info),
        NavigationItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    @Composable
    fun MyDrawerLayout(
        drawerState: DrawerState,
        recordFunc: RecordFunc,
        pickImageFunc: PickImageFunc,
        pickImageUsingCamera: PickImageUsingCamera
    ) {
        val scope = rememberCoroutineScope()
        var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
        var active by remember { mutableStateOf(false) }
        val repository = MessageRepository(LocalContext.current)
        var listHistory: List<Message> by remember { mutableStateOf(emptyList()) }
        var selectedMessage by remember { mutableStateOf<Message?>(null) }

        LaunchedEffect(drawerState.currentValue) { if (!drawerState.isOpen) { active = false } }
        LaunchedEffect(active) { listHistory = repository.getAllMessages() }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet (
                    modifier = Modifier.fillMaxWidth(0.85f)
                ){
                    DrawerContent(
                        items = items,
                        selectedItemIndex = selectedItemIndex,
                        active = active,
                        onItemClicked = { index ->
                            selectedItemIndex = index
                            scope.launch { drawerState.close() }
                        },
                        onSearchHistoryItemClicked = { selectedMessage = it },
                        listHistory = listHistory,
                        onActiveChange = {
                            active = it
                            if (!active) { scope.launch { drawerState.close() } }
                        }
                    )
                }
            }
        ) {
            Scaffold { paddingValues ->
                when (selectedItemIndex) {
                    2 -> DrawerScreen("Urgent", drawerState, paddingValues)
                    3 -> DrawerScreen("Settings", drawerState, paddingValues)
                    else -> ChatRoute(
                        drawerState = drawerState,
                        recordFunc = recordFunc,
                        pickImageFunc = pickImageFunc,
                        pickImageUsingCamera = pickImageUsingCamera,
                        paddingValues = paddingValues,
                        active = active,
                        onActiveChange = { active = it },
                        type = items[selectedItemIndex].title,
                        selectedMessage = selectedMessage,
                        onSelectedMessageClear = { selectedMessage = null }
                    )
                }
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
    onActiveChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(
            start = if (active) 0.dp else 16.dp,
            end = if (active) 0.dp else 16.dp
        )
    ) {
        if (!active) Header(items[selectedItemIndex].title)

        SearchableHistoryList(
            listHistory = listHistory,
            onItemClicked = onSearchHistoryItemClicked,
            active = active,
            onActiveChange = onActiveChange
        )

        Spacer(modifier = Modifier.height(28.dp))
        HorizontalDivider(thickness = 2.dp)

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
fun Header(title: String) {
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
        animatedText = "",
        iconBtn2Painter = painterResource(id = R.drawable.baseline_add_comment_24),
        iconBtn2Content = "Add New Chat",
        iconBtn2Onclick = {}
    )
}
