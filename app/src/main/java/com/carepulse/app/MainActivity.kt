package com.carepulse.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.carepulse.app.navigation.CarePulseNavGraph
import com.carepulse.app.ui.theme.CarePulseTheme
import com.carepulse.app.ui.theme.CreamBackground

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or not — FCM works anyway */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate() so the system splash is installed.
        installSplashScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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
