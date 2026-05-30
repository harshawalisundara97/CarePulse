package com.carepulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.carepulse.app.navigation.CarePulseNavGraph
import com.carepulse.app.ui.theme.CarePulseTheme
import com.carepulse.app.ui.theme.CreamBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate() so the system splash is installed.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarePulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().background(CreamBackground),
                    color = CreamBackground
                ) {
                    CarePulseNavGraph()
                }
            }
        }
    }
}
