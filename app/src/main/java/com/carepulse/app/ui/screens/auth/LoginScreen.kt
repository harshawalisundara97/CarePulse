package com.carepulse.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.viewmodel.CarePulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    role: String,
    vm: CarePulseViewModel,
    onAuthSuccess: (isNewCaregiver: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val userRole = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.CUSTOMER)
    val isCaregiver = userRole == UserRole.CAREGIVER

    var isSignUp by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val loading by vm.authLoading.collectAsState()
    val error by vm.authError.collectAsState()
    val profile by vm.profile.collectAsState()
    val context = LocalContext.current

    // Navigate once authentication has produced a profile.
    LaunchedEffect(profile, loading) {
        if (submitted && !loading && profile != null) {
            submitted = false
            onAuthSuccess(isCaregiver && isSignUp)
        }
    }

    val title = if (isCaregiver) "Caregiver" else "Family"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$title sign-in", color = InkPrimary) },
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
            Text(
                if (isSignUp) "Create your account" else "Welcome back",
                style = MaterialTheme.typography.headlineLarge, color = InkPrimary
            )
            Text(
                if (isCaregiver)
                    "Manage your shifts and patients."
                else
                    "Find a caregiver and check in on your loved ones.",
                style = MaterialTheme.typography.bodyMedium, color = InkSecondary
            )
            Spacer(Modifier.height(8.dp))

            if (isSignUp) {
                CarePulseTextField(value = name, onValueChange = { name = it }, label = "Full name")
            }
            CarePulseTextField(value = email, onValueChange = { email = it }, label = "Email")
            CarePulseTextField(value = password, onValueChange = { password = it }, label = "Password")

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            PrimaryButton(
                text = when {
                    loading -> "Please wait…"
                    isSignUp -> "Create account"
                    else -> "Sign in"
                },
                onClick = {
                    submitted = true
                    if (isSignUp) vm.signUp(email, password, name, userRole)
                    else vm.signIn(email, password)
                },
                enabled = !loading && email.isNotBlank() && password.isNotBlank() &&
                    (!isSignUp || name.isNotBlank())
            )

            OutlinedButton(
                onClick = {
                    submitted = true
                    vm.signInWithGoogle(context, userRole)
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue with Google", color = InkPrimary, fontWeight = FontWeight.SemiBold)
            }

            if (loading) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isSignUp) "Already have an account?" else "New to CarePulse?",
                    style = MaterialTheme.typography.bodyMedium, color = InkSecondary
                )
                TextButton(onClick = { isSignUp = !isSignUp; vm.clearAuthError() }) {
                    Text(if (isSignUp) "Sign in" else "Create account")
                }
            }
        }
    }
}
