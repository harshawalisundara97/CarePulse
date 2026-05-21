package com.carepulse.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    role: String,
    onLogin: (name: String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val title = if (role == "CAREGIVER") "Caregiver Sign-in" else "Family Sign-in"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = InkPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = InkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = CreamBackground
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Welcome back", style = MaterialTheme.typography.headlineLarge, color = InkPrimary)
            Text(
                if (role == "CAREGIVER")
                    "Sign in to manage your shifts and patients."
                else
                    "Sign in to find a caregiver and check in on your loved ones.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary
            )
            Spacer(Modifier.height(8.dp))
            CarePulseTextField(value = name, onValueChange = { name = it }, label = "Full name")
            CarePulseTextField(value = email, onValueChange = { email = it }, label = "Email")
            CarePulseTextField(value = password, onValueChange = { password = it }, label = "Password")
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = if (role == "CAREGIVER") "Continue to profile" else "Sign in",
                onClick = { onLogin(name.ifBlank { "Alex Chen" }) },
                enabled = email.isNotBlank() && password.isNotBlank()
            )
        }
    }
}
