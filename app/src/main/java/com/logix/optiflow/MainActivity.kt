package com.logix.optiflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.logix.optiflow.di.SearchBookingModule
import com.logix.optiflow.presentation.navigation.OptiFlowNavHost
import com.logix.optiflow.presentation.theme.OptiFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SearchBookingModule.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            OptiFlowTheme {
                OptiFlowNavHost()
            }
        }
    }
}
