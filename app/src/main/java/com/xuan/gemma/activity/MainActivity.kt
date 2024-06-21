package com.xuan.gemma.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xuan.gemma.ui.screen.LoadingRoute
import com.xuan.gemma.ui.screen.MainFunc
import com.xuan.gemma.ui.screen.ModelNotFoundScreen
import com.xuan.gemma.ui.theme.gemmaTheme
import com.xuan.gemma.util.FileManagerHelper
import com.xuan.gemma.util.PickImageFunc
import com.xuan.gemma.util.PickImageUsingCamera
import com.xuan.gemma.util.RecordFunc

const val START_SCREEN = "start_screen"
const val CHAT_SCREEN = "chat_screen"

class MainActivity : ComponentActivity() {

    private lateinit var fileManagerHelper: FileManagerHelper
    private lateinit var pickFileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pickImage = PickImageFunc(this, this)
        val pckImgUseCam = PickImageUsingCamera(this, this)

        pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    fileManagerHelper.handleSelectedFile(uri)
                }
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
                    // 檢查文件是否存在
                    val gemmaFileExists = fileManagerHelper.checkFileExists("model.bin")
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)//Drawer
                    val recordFunc = remember { RecordFunc(this, this) }//record

                    if (!gemmaFileExists) {
                        ModelNotFoundScreen(
                            fileManagerHelper = fileManagerHelper,
                            onRefresh = { shouldRefresh = true }
                        )
                    }
                    else {
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
                                    }
                                )
                            }

                            composable(CHAT_SCREEN) {
                                MainFunc.MyDrawerLayout(drawerState, recordFunc, pickImage, pckImgUseCam)
                            }
                        }
                    }
                }
            }
        }
    }
}