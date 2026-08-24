package com.carepulse.app

import android.app.Application
import com.carepulse.app.data.auth.AuthRepository
import com.carepulse.app.data.repository.FirestoreCarePulseRepository
import com.carepulse.app.data.repository.FirestoreChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry + tiny manual service locator. Holds the Firebase-backed
 * singletons (auth, repository) and an application-scoped coroutine scope
 * used by the repository's real-time flows.
 */
class CarePulseApplication : Application() {

    val applicationScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    val authRepository: AuthRepository by lazy { AuthRepository() }

    val repository: FirestoreCarePulseRepository by lazy {
        FirestoreCarePulseRepository(applicationScope, appContext = this)
    }

    val chatRepository: FirestoreChatRepository by lazy {
        FirestoreChatRepository(applicationScope)
    }

    override fun onCreate() {
        super.onCreate()
        // Populate the caregiver roster on first launch so the dashboard isn't empty.
        applicationScope.launch { runCatching { repository.seedCaregiversIfEmpty() } }
    }
}
