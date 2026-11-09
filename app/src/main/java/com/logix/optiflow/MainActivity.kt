package com.logix.optiflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.logix.optiflow.presentation.navigation.OptiFlowNavHost
import com.logix.optiflow.presentation.theme.OptiFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OptiFlowTheme {
                OptiFlowNavHost()
            }
        }
    }
}
