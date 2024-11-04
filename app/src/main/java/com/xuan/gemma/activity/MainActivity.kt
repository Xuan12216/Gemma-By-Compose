package com.xuan.gemma.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xuan.gemma.ui.screen.LoadingIndicator
import com.xuan.gemma.ui.screen.LoadingRoute
import com.xuan.gemma.ui.screen.ModelNotFoundScreen
import com.xuan.gemma.ui.screen.MyDrawerLayout
import com.xuan.gemma.ui.theme.GemmaTheme
import com.xuan.gemma.viewmodel.MainViewModel

const val START_SCREEN = "start_screen"
const val CHAT_SCREEN = "chat_screen"

val LocalMainViewModel = compositionLocalOf<MainViewModel> { error("MainViewModel not provided") }

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.getFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel.init(this, this)

        setContent {
            GemmaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                    CompositionLocalProvider(
                        LocalMainViewModel provides viewModel
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                viewModel.fileManagerHelper.isLoading.value -> LoadingIndicator(message = "Saving Model to Device...")
                                !viewModel.fileManagerHelper.checkFileExists("model") -> ModelNotFoundScreen(
                                    fileManagerHelper = viewModel.fileManagerHelper,
                                    onRefresh = { viewModel.fileManagerHelper.openFileManager() }
                                )
                                else -> AppNavHost(
                                    navController = navController,
                                    drawerState = drawerState,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AppNavHost(
        navController: NavHostController,
        drawerState: DrawerState,
    ) {
        NavHost(
            navController = navController,
            startDestination = START_SCREEN
        ) {
            composable(START_SCREEN) {
                LoadingRoute(
                    onModelLoaded = {
                        navController.navigate(CHAT_SCREEN) {
                            popUpTo(START_SCREEN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onRetry = {viewModel.fileManagerHelper.openFileManager()}
                )
            }

            composable(CHAT_SCREEN) {
                MyDrawerLayout(drawerState)
            }
        }
    }
}
