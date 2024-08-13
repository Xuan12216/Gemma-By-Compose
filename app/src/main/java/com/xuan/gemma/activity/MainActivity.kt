package com.xuan.gemma.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xuan.gemma.ui.screen.LoadingRoute
import com.xuan.gemma.ui.screen.MainFunc
import com.xuan.gemma.ui.screen.ModelNotFoundScreen
import com.xuan.gemma.ui.theme.gemmaTheme
import com.xuan.gemma.util.AppUtils
import com.xuan.gemma.util.FileManagerHelper
import com.xuan.gemma.util.PickImageFunc
import com.xuan.gemma.util.PickImageUsingCamera
import com.xuan.gemma.util.RecordFunc

const val START_SCREEN = "start_screen"
const val CHAT_SCREEN = "chat_screen"

class MainActivity : ComponentActivity() {

    private lateinit var fileManagerHelper: FileManagerHelper
    private lateinit var pickFileLauncher: ActivityResultLauncher<Intent>
    var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pickImage = PickImageFunc(this, this)
        val pckImgUseCam = PickImageUsingCamera(this, this)

        pickFileLauncher = AppUtils.registerPickFileLauncher(this) { uri ->
            isLoading = true
            handleSelectedFile(uri) {
                isLoading = false
            }
        }
        fileManagerHelper = FileManagerHelper(this, pickFileLauncher)

        setContent {
            gemmaTheme {
                var shouldRefresh by remember { mutableStateOf(false) }

                LaunchedEffect(shouldRefresh) {
                    shouldRefresh = false
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()
                    val gemmaFileExists = fileManagerHelper.checkFileExists("model.bin")
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val recordFunc = remember { RecordFunc(this, this) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            isLoading -> LoadingScreen()
                            gemmaFileExists.not() -> ModelNotFoundScreen(
                                fileManagerHelper = fileManagerHelper,
                                onRefresh = { shouldRefresh = true }
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
    fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Saving Model to Cache...",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 70.dp, start = 4.dp, end = 4.dp)
                )
                CircularProgressIndicator()
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
                    onRetry = {}
                )
            }

            composable(CHAT_SCREEN) {
                MainFunc.MyDrawerLayout(drawerState, recordFunc, pickImage, pickImageUsingCamera)
            }
        }
    }

    private fun handleSelectedFile(uri: Uri, onComplete: () -> Unit) {
        Thread {
            val fileManagerHelper = FileManagerHelper(this, pickFileLauncher)
            fileManagerHelper.handleSelectedFile(uri)
            runOnUiThread { onComplete() }
        }.start()
    }
}
