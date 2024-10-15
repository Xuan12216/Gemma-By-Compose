package com.xuan.gemma.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.xuan.gemma.ui.screen.MainFunc
import com.xuan.gemma.ui.screen.ModelNotFoundScreen
import com.xuan.gemma.ui.theme.GemmaTheme
import com.xuan.gemma.util.FileManagerHelper
import com.xuan.gemma.util.PickImageFunc
import com.xuan.gemma.util.PickImageUsingCamera
import com.xuan.gemma.util.RecordFunc

const val START_SCREEN = "start_screen"
const val CHAT_SCREEN = "chat_screen"

class MainActivity : ComponentActivity() {

    private lateinit var fileManagerHelper: FileManagerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileManagerHelper = FileManagerHelper(this).apply { initPickFileLauncher() }
        val pickImage = PickImageFunc(this, this)
        val pckImgUseCam = PickImageUsingCamera(this, this)

        setContent {
            GemmaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val recordFunc = remember { RecordFunc(this, this) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            fileManagerHelper.isLoading.value -> LoadingIndicator(message = "Saving Model to Device...")
                            !fileManagerHelper.checkFileExists("model") -> ModelNotFoundScreen(
                                fileManagerHelper = fileManagerHelper,
                                onRefresh = { fileManagerHelper.openFileManager() }
                            )
                            else -> AppNavHost(
                                navController = navController,
                                drawerState = drawerState,
                                recordFunc = recordFunc,
                                pickImage = pickImage,
                                pickImageUsingCamera = pckImgUseCam
                            )
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
        recordFunc: RecordFunc,
        pickImage: PickImageFunc,
        pickImageUsingCamera: PickImageUsingCamera
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
                    onRetry = {fileManagerHelper.openFileManager()}
                )
            }

            composable(CHAT_SCREEN) {
                MainFunc.MyDrawerLayout(drawerState, recordFunc, pickImage, pickImageUsingCamera)
            }
        }
    }
}
