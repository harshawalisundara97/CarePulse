package com.carepulse.app.data.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Snapshot of the current authentication state. */
sealed interface AuthState {
    /** Initial state before the first auth callback arrives. */
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(
        val uid: String,
        val email: String?,
        val displayName: String?
    ) : AuthState
}

/**
 * Thin wrapper over [FirebaseAuth]. Exposes auth changes as a [Flow] and
 * suspend functions for the email/password and Google sign-in paths. All
 * Firebase [com.google.android.gms.tasks.Task]s are awaited via
 * kotlinx-coroutines-play-services.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser: FirebaseUser? get() = auth.currentUser

    /** Emits Loading first, then SignedIn/SignedOut on every auth change. */
    val authState: Flow<AuthState> = callbackFlow {
        trySend(AuthState.Loading)
        val listener = FirebaseAuth.AuthStateListener { fb ->
            val user = fb.currentUser
            trySend(
                if (user != null)
                    AuthState.SignedIn(user.uid, user.email, user.displayName)
                else
                    AuthState.SignedOut
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUpEmail(email: String, password: String, name: String): Result<FirebaseUser> =
        runCatching {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: error("Sign-up returned no user")
            if (name.isNotBlank()) {
                user.updateProfile(userProfileChangeRequest { displayName = name }).await()
            }
            user
        }

    suspend fun signInEmail(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            result.user ?: error("Sign-in returned no user")
        }

    /**
     * Browser-based Google sign-in: opens the system's default browser (Chrome
     * Custom Tab) to Google's account chooser, then returns control to the app
     * once the user picks an account and grants access. Uses Firebase's generic
     * OAuth provider flow instead of the Credential Manager / One Tap picker,
     * since it only requires "Google" to be enabled as a sign-in provider in the
     * Firebase console (no separate Google Cloud OAuth consent screen setup).
     *
     * If the activity was recreated mid-flow (e.g. rotation) the pending result
     * is picked up from [FirebaseAuth.getPendingAuthResult] instead of
     * relaunching the browser.
     */
    suspend fun signInWithGoogleBrowser(activity: Activity): Result<FirebaseUser> =
        runCatching {
            val provider = OAuthProvider.newBuilder("google.com").apply {
                addCustomParameter("prompt", "select_account")
            }.build()

            val pending = auth.pendingAuthResult
            val result = if (pending != null) {
                pending.await()
            } else {
                auth.startActivityForSignInWithProvider(activity, provider).await()
            }
            result.user ?: error("Google sign-in returned no user")
        }

    /** Sends Firebase's password-reset email to the given address. */
    suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching {
            auth.sendPasswordResetEmail(email.trim()).await()
        }

    fun signOut() = auth.signOut()
}
