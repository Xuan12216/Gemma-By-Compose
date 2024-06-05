package com.xuan.gemini.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.xuan.gemini.ui.screen.MainFunc
import com.xuan.gemini.ui.theme.GeminiTheme
import com.xuan.gemini.util.PickImageFunc
import com.xuan.gemini.util.PickImageUsingCamera
import com.xuan.gemini.util.RecordFunc

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pickImage = PickImageFunc(this, this)
        val pckImgUseCam = PickImageUsingCamera(this, this)

        setContent {

            GeminiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)//Drawer
                    val recordFunc = remember { RecordFunc(this, this) }//record

                    MainFunc.MyDrawerLayout(drawerState, recordFunc, pickImage, pckImgUseCam)
                }
            }
        }
    }
}