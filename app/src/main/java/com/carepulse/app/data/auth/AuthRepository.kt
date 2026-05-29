package com.carepulse.app.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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

    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> =
        runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user ?: error("Google sign-in returned no user")
        }

    fun signOut() = auth.signOut()
}
