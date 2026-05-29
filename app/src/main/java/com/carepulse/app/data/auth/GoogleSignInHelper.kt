package com.carepulse.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL

/**
 * Obtains a Google ID token using the modern Credential Manager API.
 *
 * The server client id is the *web* OAuth client id that the google-services
 * plugin writes into `R.string.default_web_client_id` from
 * `google-services.json`. The returned token is handed to
 * [AuthRepository.signInWithGoogleIdToken].
 */
class GoogleSignInHelper(
    private val serverClientId: String
) {
    /**
     * Launches the Google credential picker and returns the ID token.
     * Must be called with an Activity [Context] so the system UI can show.
     */
    suspend fun getGoogleIdToken(context: Context): Result<String> = runCatching {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val response = CredentialManager.create(context).getCredential(context, request)
        val credential = response.credential

        if (credential is CustomCredential &&
            credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            error("Unexpected credential type: ${credential.type}")
        }
    }
}
