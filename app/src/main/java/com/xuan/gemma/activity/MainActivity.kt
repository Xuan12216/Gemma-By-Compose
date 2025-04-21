package com.xuan.gemma.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

    private val mainViewModel: MainViewModel by viewModels { MainViewModel.getFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        mainViewModel.init(this, this)

        setContent {
            GemmaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var isFileExists by remember { mutableStateOf(mainViewModel.fileManagerHelper.checkModelFileExits()) }

                    CompositionLocalProvider(
                        LocalMainViewModel provides mainViewModel
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                mainViewModel.fileManagerHelper.isLoading.value -> {
                                    LoadingIndicator(message = "Saving Model to Device...")
                                }
                                !isFileExists -> {
                                    isFileExists = mainViewModel.fileManagerHelper.checkModelFileExits()
                                    ModelNotFoundScreen(
                                        fileManagerHelper = mainViewModel.fileManagerHelper,
                                        onRefresh = { isFileExists = mainViewModel.fileManagerHelper.checkModelFileExits() },
                                        context = this@MainActivity
                                    )
                                }
                                else -> AppNavHost(
                                    navController = navController,
                                    onRetry = {
                                        mainViewModel.fileManagerHelper.deleteModelFiles(this@MainActivity.cacheDir)
                                        isFileExists = false
                                    }
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
        onRetry: () -> Unit
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
                    onRetry = { onRetry() }
                )
            }

            composable(CHAT_SCREEN) {
                MyDrawerLayout()
            }
        }
    }
}
