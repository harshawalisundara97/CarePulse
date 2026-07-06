# CarePulse Phase 2 — Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Push Notifications, Real-time Chat, Caregiver Ratings & Profile Editing, and Booking History & Status Tracking to CarePulse — sequentially, each working end-to-end before the next begins.

**Architecture:** All features extend the existing Firebase/Firestore backend using the established MVVM + StateFlow pattern. New Firestore collections (`chats`, `reviews`) follow the same `callbackFlow` snapshot-listener pattern already used for `caregivers`, `bookings`, etc. Cloud Functions (Node.js) handle server-side notification triggers.

**Tech Stack:** Kotlin/Jetpack Compose, Firebase Auth + Firestore + Messaging + Cloud Functions (Node.js 20), Material 3, StateFlow/ViewModel

---

## File Map

### New files
| File | Purpose |
|------|---------|
| `app/.../CarePulseFirebaseMessagingService.kt` | Receive FCM messages, show notifications, save token |
| `functions/index.js` | Cloud Functions: 3 notification triggers |
| `functions/package.json` | Node deps for Cloud Functions |
| `app/.../data/repository/ChatRepository.kt` | Interface for chat data |
| `app/.../data/repository/FirestoreChatRepository.kt` | Firestore-backed chat implementation |
| `app/.../ui/screens/messages/ConversationScreen.kt` | Full-screen chat conversation view |
| `app/.../ui/screens/caregiver/EditProfileScreen.kt` | Caregiver self-edits bio/skills/rate |

### Modified files
| File | Change |
|------|--------|
| `app/build.gradle.kts` | Add `firebase-messaging-ktx` |
| `AndroidManifest.xml` | Register messaging service, POST_NOTIFICATIONS permission |
| `data/model/Models.kt` | Add `Chat`, `ChatMessage`, `Review` data classes; add `agencyId` to `Booking` |
| `data/repository/CarePulseRepository.kt` | Add `saveFcmToken`, `addReview`, `reviewsFor`, `saveCaregiver`, `updateBookingStatus`, `bookingsForFamily`, `bookingsForAgency` |
| `data/repository/FirestoreCarePulseRepository.kt` | Implement above methods |
| `CarePulseApplication.kt` | Inject `chatRepository` |
| `viewmodel/CarePulseViewModel.kt` | Add chat, review, booking-status methods and StateFlows |
| `navigation/CarePulseNavGraph.kt` | Add `conversation/{chatId}` and `edit-profile` routes |
| `ui/screens/messages/MessagesScreen.kt` | Real chat list (was placeholder) |
| `ui/screens/customer/ActivityScreen.kt` | Real bookings list with tabs + rate button |
| `ui/screens/customer/CaregiverDetailScreen.kt` | Add reviews section |
| `ui/screens/agency/AgencyScreens.kt` | Real billing screen with earnings |
| `ui/screens/caregiver/CaregiverDashboardScreen.kt` | Clock-in updates booking status; upcoming shifts section |

---

## Task 1: Add FCM Dependency + Notification Permission

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Add firebase-messaging to build.gradle.kts**

In `app/build.gradle.kts`, inside the `dependencies { }` block after the existing Firebase lines, add:

```kotlin
implementation("com.google.firebase:firebase-messaging-ktx")
```

- [ ] **Step 2: Add POST_NOTIFICATIONS permission and FCM service to AndroidManifest**

Replace the entire contents of `app/src/main/AndroidManifest.xml` with:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".CarePulseApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="CarePulse"
        android:supportsRtl="true"
        android:theme="@style/Theme.CarePulse">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.CarePulse.Splash">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".CarePulseFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

    </application>
</manifest>
```

- [ ] **Step 3: Verify build succeeds**

```bash
cd /Users/ranjana/Harsha/CarePulse && ./gradlew :app:assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

---

## Task 2: Create FCM Messaging Service

**Files:**
- Create: `app/src/main/java/com/carepulse/app/CarePulseFirebaseMessagingService.kt`

- [ ] **Step 1: Create the messaging service**

```kotlin
package com.carepulse.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.carepulse.app.data.repository.FirestoreCarePulseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CarePulseFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val CHANNEL_ID = "carepulse_channel"
    private val CHANNEL_NAME = "CarePulse Alerts"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: return
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: return
        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val app = applicationContext as CarePulseApplication
        scope.launch {
            runCatching { app.repository.saveFcmToken(uid, token) }
        }
    }

    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (required on API 26+, safe to call repeatedly)
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

- [ ] **Step 2: Verify build succeeds**

```bash
cd /Users/ranjana/Harsha/CarePulse && ./gradlew :app:assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

---

## Task 3: Add saveFcmToken to Repository

**Files:**
- Modify: `data/repository/CarePulseRepository.kt`
- Modify: `data/repository/FirestoreCarePulseRepository.kt`

- [ ] **Step 1: Add saveFcmToken to the interface**

In `CarePulseRepository.kt`, add this line at the end of the interface body (after `updateCareRequest`):

```kotlin
suspend fun saveFcmToken(uid: String, token: String)
```

- [ ] **Step 2: Implement saveFcmToken in FirestoreCarePulseRepository**

In `FirestoreCarePulseRepository.kt`, add after `updateCareRequest`:

```kotlin
override suspend fun saveFcmToken(uid: String, token: String) {
    runCatching {
        usersCol.document(uid).update("fcmToken", token).await()
    }
}
```

- [ ] **Step 3: Add saveFcmToken stub to MockCarePulseRepository (if it exists)**

Check if `MockCarePulseRepository.kt` exists:
```bash
find /Users/ranjana/Harsha/CarePulse -name "MockCarePulseRepository.kt"
```
If it exists, open it and add: `override suspend fun saveFcmToken(uid: String, token: String) { }` (no-op for mock).

- [ ] **Step 4: Call saveFcmToken after sign-in in ViewModel**

In `CarePulseViewModel.kt`, after the `init { }` block, add:

```kotlin
/** Called once FCM has a token — persists it so Cloud Functions can address this device. */
fun onFcmTokenReceived(token: String) {
    val uid = (_authState.value as? AuthState.SignedIn)?.uid ?: return
    viewModelScope.launch { runCatching { repo.saveFcmToken(uid, token) } }
}
```

- [ ] **Step 5: Request notification permission in MainActivity**

Open `app/src/main/java/com/carepulse/app/MainActivity.kt`. Add the import and permission request inside `onCreate`, after `installSplashScreen()`. The full relevant section should look like:

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

// Inside the class, before onCreate:
private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or not */ }

// Inside onCreate, after installSplashScreen() call:
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
// Refresh FCM token on launch
FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
    // ViewModel isn't accessible here directly; store in a shared place
    // For now, the service's onNewToken handles registration.
}
```

- [ ] **Step 6: Build and verify**

```bash
cd /Users/ranjana/Harsha/CarePulse && ./gradlew :app:assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

---

## Task 4: Cloud Functions for Notification Triggers

**Files:**
- Create: `functions/package.json`
- Create: `functions/index.js`

These run server-side on Firebase. You need Node.js installed and the Firebase CLI.

- [ ] **Step 1: Check Firebase CLI is installed**

```bash
firebase --version
```
If not installed: `npm install -g firebase-tools`

- [ ] **Step 2: Initialize Cloud Functions in the project root**

```bash
cd /Users/ranjana/Harsha/CarePulse
firebase login --no-localhost   # if not already logged in
firebase init functions         # choose: JavaScript, project=carepules, do NOT overwrite existing files
```
Select `JavaScript` (not TypeScript) when prompted. Say **No** to ESLint. Say **Yes** to install dependencies.

- [ ] **Step 3: Replace functions/index.js with the 3 notification triggers**

```javascript
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// Helper: send FCM to a uid
async function notifyUser(uid, title, body) {
  if (!uid) return;
  const userDoc = await db.collection("users").doc(uid).get();
  const token = userDoc.data()?.fcmToken;
  if (!token) return;
  await admin.messaging().send({
    token,
    notification: { title, body },
    android: { priority: "high" },
  });
}

// Trigger 1: New care request → notify the agency admin
exports.onCareRequestCreated = functions.firestore
  .document("careRequests/{requestId}")
  .onCreate(async (snap) => {
    const req = snap.data();
    const agencyId = req.agencyId;
    if (!agencyId) return;
    // Find the agency admin uid (user whose agencyId == this agencyId and role == AGENCY)
    const usersSnap = await db.collection("users")
      .where("agencyId", "==", agencyId)
      .where("role", "==", "AGENCY")
      .limit(1)
      .get();
    if (usersSnap.empty) return;
    const adminUid = usersSnap.docs[0].id;
    await notifyUser(
      adminUid,
      "New care request",
      `${req.familyName} needs a caregiver for ${req.patientName}`
    );
  });

// Trigger 2: Care request assigned → notify the family
exports.onCareRequestAssigned = functions.firestore
  .document("careRequests/{requestId}")
  .onUpdate(async (change) => {
    const before = change.before.data();
    const after = change.after.data();
    if (before.status === after.status) return; // no status change
    if (after.status !== "ASSIGNED") return;
    const familyUid = after.familyUid;
    await notifyUser(
      familyUid,
      "Caregiver assigned",
      `${after.assignedCaregiverName} has been assigned to care for ${after.patientName}`
    );
  });

// Trigger 3: Shift report filed → notify the family (via booking lookup)
exports.onShiftReportCreated = functions.firestore
  .document("reports/{reportId}")
  .onCreate(async (snap) => {
    const report = snap.data();
    // Find a booking associated with this caregiver to get the customerUid
    const bookingsSnap = await db.collection("bookings")
      .where("caregiverUid", "==", report.caregiverId ?? "")
      .where("status", "in", ["IN_PROGRESS", "CONFIRMED"])
      .limit(1)
      .get();
    if (bookingsSnap.empty) return;
    const booking = bookingsSnap.docs[0].data();
    const customerUid = booking.customerUid;
    await notifyUser(
      customerUid,
      "Shift report filed",
      `${report.caregiverName} has submitted today's care report`
    );
  });
```

- [ ] **Step 4: Deploy the Cloud Functions**

```bash
cd /Users/ranjana/Harsha/CarePulse
firebase deploy --only functions --project carepules
```
Expected output ends with: `✔  Deploy complete!`

- [ ] **Step 5: Commit notifications feature**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/build.gradle.kts app/src/main/AndroidManifest.xml \
  app/src/main/java/com/carepulse/app/CarePulseFirebaseMessagingService.kt \
  app/src/main/java/com/carepulse/app/MainActivity.kt \
  app/src/main/java/com/carepulse/app/data/repository/CarePulseRepository.kt \
  app/src/main/java/com/carepulse/app/data/repository/FirestoreCarePulseRepository.kt \
  app/src/main/java/com/carepulse/app/viewmodel/CarePulseViewModel.kt \
  functions/
git commit -m "feat: add push notifications via FCM + Cloud Functions (3 triggers)

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 5: Add Chat Data Models

**Files:**
- Modify: `data/model/Models.kt`

- [ ] **Step 1: Append Chat and ChatMessage models to Models.kt**

At the end of `Models.kt`, add:

```kotlin
/**
 * A chat thread between a family and an agency.
 * chatId = "{agencyId}_{familyUid}" — deterministic so both sides find the same doc.
 */
data class Chat(
    val id: String,
    val agencyId: String,
    val familyUid: String,
    val agencyName: String = "",
    val familyName: String = "",
    val caregiverId: String? = null,     // set when agency loops in a caregiver
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L         // epoch millis
)

data class ChatMessage(
    val id: String,
    val senderUid: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Build to confirm no syntax errors**

```bash
cd /Users/ranjana/Harsha/CarePulse && ./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

---

## Task 6: Chat Repository

**Files:**
- Create: `app/src/main/java/com/carepulse/app/data/repository/ChatRepository.kt`
- Create: `app/src/main/java/com/carepulse/app/data/repository/FirestoreChatRepository.kt`

- [ ] **Step 1: Create ChatRepository interface**

```kotlin
package com.carepulse.app.data.repository

import com.carepulse.app.data.model.Chat
import com.carepulse.app.data.model.ChatMessage
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    /** All chat threads the current user participates in. */
    fun chatsFor(uid: String): StateFlow<List<Chat>>
    /** Messages in a single thread, real-time. */
    fun messagesIn(chatId: String): StateFlow<List<ChatMessage>>
    suspend fun sendMessage(chatId: String, message: ChatMessage, chat: Chat)
    suspend fun loopInCaregiver(chatId: String, caregiverId: String)
}
```

- [ ] **Step 2: Create FirestoreChatRepository**

```kotlin
package com.carepulse.app.data.repository

import com.carepulse.app.data.model.Chat
import com.carepulse.app.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(
    private val scope: CoroutineScope,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ChatRepository {

    private val chatsCol = db.collection("chats")

    override fun chatsFor(uid: String): StateFlow<List<Chat>> = callbackFlow {
        val reg = chatsCol
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toChat() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override fun messagesIn(chatId: String): StateFlow<List<ChatMessage>> = callbackFlow {
        val reg = chatsCol.document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toMessage() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override suspend fun sendMessage(chatId: String, message: ChatMessage, chat: Chat) {
        runCatching {
            val chatRef = chatsCol.document(chatId)
            // Upsert the chat thread header (creates it if first message)
            chatRef.set(chat.toMap()).await()
            chatRef.collection("messages").document(message.id).set(message.toMap()).await()
        }
    }

    override suspend fun loopInCaregiver(chatId: String, caregiverId: String) {
        runCatching {
            chatsCol.document(chatId).update("caregiverId", caregiverId).await()
        }
    }

    // --- Mappers ---

    private fun com.google.firebase.firestore.DocumentSnapshot.toChat(): Chat? = runCatching {
        Chat(
            id = id,
            agencyId = getString("agencyId") ?: return@runCatching null,
            familyUid = getString("familyUid") ?: return@runCatching null,
            agencyName = getString("agencyName") ?: "",
            familyName = getString("familyName") ?: "",
            caregiverId = getString("caregiverId"),
            lastMessage = getString("lastMessage") ?: "",
            lastMessageAt = getLong("lastMessageAt") ?: 0L
        )
    }.getOrNull()

    private fun com.google.firebase.firestore.DocumentSnapshot.toMessage(): ChatMessage? = runCatching {
        ChatMessage(
            id = id,
            senderUid = getString("senderUid") ?: return@runCatching null,
            senderName = getString("senderName") ?: "",
            text = getString("text") ?: "",
            timestamp = getLong("timestamp") ?: 0L
        )
    }.getOrNull()

    private fun Chat.toMap() = mapOf(
        "agencyId" to agencyId,
        "familyUid" to familyUid,
        "agencyName" to agencyName,
        "familyName" to familyName,
        "caregiverId" to caregiverId,
        "lastMessage" to lastMessage,
        "lastMessageAt" to lastMessageAt,
        "participants" to listOfNotNull(familyUid, agencyId, caregiverId).distinct()
    )

    private fun ChatMessage.toMap() = mapOf(
        "senderUid" to senderUid,
        "senderName" to senderName,
        "text" to text,
        "timestamp" to timestamp
    )
}
```

- [ ] **Step 3: Register chatRepository in CarePulseApplication**

In `CarePulseApplication.kt`, add after the `repository` lazy property:

```kotlin
val chatRepository: FirestoreChatRepository by lazy {
    FirestoreChatRepository(applicationScope)
}
```

Add import at top: `import com.carepulse.app.data.repository.FirestoreChatRepository`

- [ ] **Step 4: Build to confirm**

```bash
cd /Users/ranjana/Harsha/CarePulse && ./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

---

## Task 7: Chat ViewModel Methods

**Files:**
- Modify: `viewmodel/CarePulseViewModel.kt`

- [ ] **Step 1: Inject chatRepository into CarePulseViewModel**

Change the class signature:

```kotlin
class CarePulseViewModel(
    private val auth: AuthRepository,
    private val repo: CarePulseRepository,
    private val googleHelper: GoogleSignInHelper,
    private val chatRepo: com.carepulse.app.data.repository.FirestoreChatRepository
) : ViewModel() {
```

- [ ] **Step 2: Add chat StateFlows and methods**

Add after the `matchesFor` function:

```kotlin
// --- Chat -------------------------------------------------------------------

/** All chat threads for the signed-in user. */
val myChats: StateFlow<List<com.carepulse.app.data.model.Chat>> =
    _profile.map { it?.uid }
        .combine(_profile) { uid, _ -> uid }
        .map { uid -> if (uid != null) chatRepo.chatsFor(uid).value else emptyList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

private val _activeChatId = MutableStateFlow<String?>(null)

fun openChat(chatId: String) { _activeChatId.value = chatId }

fun messagesIn(chatId: String) = chatRepo.messagesIn(chatId)

fun sendMessage(agencyId: String, agencyName: String, text: String) {
    val profile = _profile.value ?: return
    val familyUid = profile.uid
    val chatId = "${agencyId}_${familyUid}"
    viewModelScope.launch {
        val msg = com.carepulse.app.data.model.ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            senderUid = familyUid,
            senderName = profile.displayName,
            text = text.trim(),
            timestamp = System.currentTimeMillis()
        )
        val chat = com.carepulse.app.data.model.Chat(
            id = chatId,
            agencyId = agencyId,
            familyUid = familyUid,
            agencyName = agencyName,
            familyName = profile.displayName,
            lastMessage = text.trim(),
            lastMessageAt = System.currentTimeMillis()
        )
        chatRepo.sendMessage(chatId, msg, chat)
    }
}

fun loopInCaregiver(chatId: String, caregiverId: String) {
    viewModelScope.launch { chatRepo.loopInCaregiver(chatId, caregiverId) }
}
```

- [ ] **Step 3: Update the Factory to inject chatRepository**

In `CarePulseViewModel.Factory`:

```kotlin
val Factory = viewModelFactory {
    initializer {
        val app = this[APPLICATION_KEY] as CarePulseApplication
        CarePulseViewModel(
            app.authRepository,
            app.repository,
            app.googleSignInHelper,
            app.chatRepository
        )
    }
}
```

- [ ] **Step 4: Build to confirm**

```bash
cd /Users/ranjana/Harsha/CarePulse && ./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

---

## Task 8: Chat UI — MessagesScreen + ConversationScreen

**Files:**
- Modify: `ui/screens/messages/MessagesScreen.kt`
- Create: `ui/screens/messages/ConversationScreen.kt`
- Modify: `navigation/CarePulseNavGraph.kt`

- [ ] **Step 1: Rewrite MessagesScreen with real chat threads**

Replace the entire content of `MessagesScreen.kt`:

```kotlin
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.Chat
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.theme.*
import com.carepulse.app.viewmodel.CarePulseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessagesScreen(vm: CarePulseViewModel, onOpenChat: (String) -> Unit) {
    // For families: load agencies so they can start a chat
    val agencies by vm.agencies.collectAsState()
    LaunchedEffect(Unit) { vm.loadAgencies() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", style = MaterialTheme.typography.headlineMedium,
                    color = InkPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = CreamBackground
    ) { padding ->
        val profile by vm.profile.collectAsState()
        if (agencies.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No agencies available to chat with.", color = InkSecondary,
                    style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(agencies) { agency ->
                    val chatId = "${agency.id}_${profile?.uid ?: ""}"
                    PastelCard(modifier = Modifier.clickable { onOpenChat(chatId) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ChatBubbleOutline, null, tint = PastelMintDeep,
                                modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(agency.name, style = MaterialTheme.typography.titleMedium,
                                    color = InkPrimary, fontWeight = FontWeight.SemiBold)
                                if (agency.nearHospital.isNotBlank()) {
                                    Text(agency.nearHospital, style = MaterialTheme.typography.bodySmall,
                                        color = InkSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create ConversationScreen**

Create new file `app/src/main/java/com/carepulse/app/ui/screens/messages/ConversationScreen.kt`:

```kotlin
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.ChatMessage
import com.carepulse.app.ui.theme.*
import com.carepulse.app.viewmodel.CarePulseViewModel

@Composable
fun ConversationScreen(
    chatId: String,
    vm: CarePulseViewModel,
    onBack: () -> Unit
) {
    val profile by vm.profile.collectAsState()
    val messages by vm.messagesIn(chatId).collectAsState()
    val listState = rememberLazyListState()
    var draft by remember { mutableStateOf("") }

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    // Parse agencyId from chatId (format: "{agencyId}_{familyUid}")
    val agencyId = chatId.substringBefore("_")
    val agencies by vm.agencies.collectAsState()
    LaunchedEffect(Unit) { vm.loadAgencies() }
    val agencyName = agencies.firstOrNull { it.id == agencyId }?.name ?: "Agency"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(agencyName, color = InkPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = InkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().background(Color.White).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message…") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    enabled = draft.isNotBlank(),
                    onClick = {
                        vm.sendMessage(agencyId, agencyName, draft)
                        draft = ""
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = PastelMintDeep)
                }
            }
        },
        containerColor = CreamBackground
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderUid == profile?.uid
                MessageBubble(msg, isMine)
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, isMine: Boolean) {
    val bubbleColor = if (isMine) PastelMintDeep else Color.White
    val textColor = if (isMine) Color.White else InkPrimary
    val alignment = if (isMine) Alignment.End else Alignment.Start

    Column(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        if (!isMine) {
            Text(msg.senderName, style = MaterialTheme.typography.labelSmall, color = InkSecondary,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp))
        }
        Box(
            Modifier
                .background(bubbleColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(msg.text, color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
```

- [ ] **Step 3: Add conversation route to NavGraph**

In `CarePulseNavGraph.kt`:

1. Add to the `Routes` object:
```kotlin
const val Conversation = "conversation/{chatId}"
fun conversation(chatId: String) = "conversation/$chatId"
```

2. In `CarePulseNavGraph()`, pass `onOpenChat` into `MessagesScreen`. Find the `composable(Routes.Messages)` block and replace it:
```kotlin
composable(Routes.Messages) {
    if (role == UserRole.AGENCY) AgencyRequestsScreen(vm = vm)
    else MessagesScreen(
        vm = vm,
        onOpenChat = { chatId -> navController.navigate(Routes.conversation(chatId)) }
    )
}
```

3. Add the conversation composable (after the `CareRequest` composable):
```kotlin
composable(
    Routes.Conversation,
    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
) { entry ->
    val chatId = entry.arguments?.getString("chatId") ?: return@composable
    ConversationScreen(
        chatId = chatId,
        vm = vm,
        onBack = { navController.popBackStack() }
    )
}
```

- [ ] **Step 4: Build and verify**

```bash
cd /Users/ranjana/Harsha/CarePulse && ./gradlew :app:assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit chat feature**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/data/model/Models.kt \
  app/src/main/java/com/carepulse/app/data/repository/ChatRepository.kt \
  app/src/main/java/com/carepulse/app/data/repository/FirestoreChatRepository.kt \
  app/src/main/java/com/carepulse/app/CarePulseApplication.kt \
  app/src/main/java/com/carepulse/app/viewmodel/CarePulseViewModel.kt \
  app/src/main/java/com/carepulse/app/ui/screens/messages/ \
  app/src/main/java/com/carepulse/app/navigation/CarePulseNavGraph.kt
git commit -m "feat: real-time chat between family and agency via Firestore

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 9: Add Review Data Model + Repository Methods

**Files:**
- Modify: `data/model/Models.kt`
- Modify: `data/repository/CarePulseRepository.kt`
- Modify: `data/repository/FirestoreCarePulseRepository.kt`

- [ ] **Step 1: Add Review data class to Models.kt**

At the end of `Models.kt`, add:

```kotlin
/** A family's star rating for a caregiver after a completed booking. */
data class Review(
    val id: String,
    val caregiverId: String,
    val familyUid: String,
    val reviewerName: String,
    val bookingId: String,
    val rating: Float,          // 1.0–5.0
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Add review + saveCaregiver methods to CarePulseRepository interface**

In `CarePulseRepository.kt`, add after `updateCareRequest`:

```kotlin
// --- Reviews ---------------------------------------------------------------
suspend fun addReview(review: Review)
suspend fun reviewsFor(caregiverId: String): List<Review>

// --- Caregiver self-update -------------------------------------------------
suspend fun saveCaregiver(caregiver: Caregiver)
```

Add import at top: `import com.carepulse.app.data.model.Review`

- [ ] **Step 3: Implement in FirestoreCarePulseRepository**

In `FirestoreCarePulseRepository.kt`, add a new collection and implementations. After `careRequestsCol` declaration:

```kotlin
private val reviewsCol = db.collection("reviews")
```

After `saveFcmToken`:

```kotlin
override suspend fun addReview(review: Review) {
    runCatching {
        reviewsCol.document(review.id).set(mapOf(
            "caregiverId" to review.caregiverId,
            "familyUid" to review.familyUid,
            "reviewerName" to review.reviewerName,
            "bookingId" to review.bookingId,
            "rating" to review.rating,
            "text" to review.text,
            "createdAt" to review.createdAt
        )).await()
        // Recalculate average rating on the caregiver doc
        val existing = reviewsCol
            .whereEqualTo("caregiverId", review.caregiverId)
            .get().await()
        val allRatings = existing.documents.mapNotNull { it.getDouble("rating")?.toFloat() }
        if (allRatings.isNotEmpty()) {
            val avg = allRatings.average().toFloat()
            caregiversCol.document(review.caregiverId).update(
                "rating", avg,
                "ratingCount", allRatings.size
            ).await()
        }
    }
}

override suspend fun reviewsFor(caregiverId: String): List<Review> = runCatching {
    reviewsCol.whereEqualTo("caregiverId", caregiverId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .get().await()
        .documents.mapNotNull { doc ->
            Review(
                id = doc.id,
                caregiverId = doc.getString("caregiverId") ?: return@mapNotNull null,
                familyUid = doc.getString("familyUid") ?: "",
                reviewerName = doc.getString("reviewerName") ?: "",
                bookingId = doc.getString("bookingId") ?: "",
                rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                text = doc.getString("text") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        }
}.getOrDefault(emptyList())

override suspend fun saveCaregiver(caregiver: Caregiver) {
    runCatching { caregiversCol.document(caregiver.id).set(caregiver.toMap()).await() }
}
```

- [ ] **Step 4: Build to confirm**

```bash
cd /Users/ranjana/Harsha/CarePulse && ./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

---

## Task 10: Ratings ViewModel + UI

**Files:**
- Modify: `viewmodel/CarePulseViewModel.kt`
- Modify: `ui/screens/customer/CaregiverDetailScreen.kt`
- Create: `ui/screens/caregiver/EditProfileScreen.kt`
- Modify: `navigation/CarePulseNavGraph.kt`

- [ ] **Step 1: Add review methods to CarePulseViewModel**

Add after `loopInCaregiver`:

```kotlin
// --- Reviews ---------------------------------------------------------------

fun submitReview(caregiverId: String, bookingId: String, rating: Float, text: String) {
    val p = _profile.value ?: return
    viewModelScope.launch {
        repo.addReview(
            com.carepulse.app.data.model.Review(
                id = UUID.randomUUID().toString(),
                caregiverId = caregiverId,
                familyUid = p.uid,
                reviewerName = p.displayName,
                bookingId = bookingId,
                rating = rating,
                text = text.trim()
            )
        )
    }
}

suspend fun reviewsFor(caregiverId: String) = repo.reviewsFor(caregiverId)

// --- Caregiver self-edit ---------------------------------------------------

fun updateCaregiverProfile(
    bio: String,
    specializations: List<String>,
    hourlyRate: Int,
    availability: List<String>
) {
    val uid = currentUid ?: return
    viewModelScope.launch {
        val existing = repo.caregivers.value.firstOrNull { it.id == uid } ?: return@launch
        repo.saveCaregiver(existing.copy(
            bio = bio,
            specializations = specializations,
            hourlyRate = hourlyRate,
            availability = availability
        ))
    }
}
```

- [ ] **Step 2: Add reviews to CaregiverDetailScreen**

In `CaregiverDetailScreen.kt`, add at the bottom of the main scrollable column (after the bio / availability section), a `ReviewsSection` composable call:

```kotlin
// Add this import at top of file
import androidx.compose.runtime.LaunchedEffect

// Inside the @Composable screen, add state:
var reviews by remember { mutableStateOf<List<com.carepulse.app.data.model.Review>>(emptyList()) }
LaunchedEffect(caregiverId) { reviews = vm.reviewsFor(caregiverId) }

// At the bottom of the scrollable column, add:
ReviewsSection(reviews)
```

Add the `ReviewsSection` composable at the bottom of the file:

```kotlin
@Composable
private fun ReviewsSection(reviews: List<com.carepulse.app.data.model.Review>) {
    if (reviews.isEmpty()) return
    Spacer(Modifier.height(16.dp))
    Text("Reviews", style = MaterialTheme.typography.titleMedium,
        color = com.carepulse.app.ui.theme.InkPrimary,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    reviews.forEach { review ->
        com.carepulse.app.ui.components.PastelCard {
            Column {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    com.carepulse.app.ui.components.RatingRow(review.rating, 0)
                    Spacer(Modifier.width(8.dp))
                    Text(review.reviewerName,
                        style = MaterialTheme.typography.labelMedium,
                        color = com.carepulse.app.ui.theme.InkSecondary)
                }
                if (review.text.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(review.text, style = MaterialTheme.typography.bodyMedium,
                        color = com.carepulse.app.ui.theme.InkPrimary)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}
```

- [ ] **Step 3: Create EditProfileScreen for caregivers**

Create `app/src/main/java/com/carepulse/app/ui/screens/caregiver/EditProfileScreen.kt`:

```kotlin
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.*
import com.carepulse.app.viewmodel.CarePulseViewModel

@Composable
fun EditProfileScreen(vm: CarePulseViewModel, onDone: () -> Unit) {
    val caregivers by vm.caregivers.collectAsState()
    val profile by vm.profile.collectAsState()
    val myCg = caregivers.firstOrNull { it.id == profile?.uid }

    var bio by remember(myCg) { mutableStateOf(myCg?.bio ?: "") }
    var specializations by remember(myCg) { mutableStateOf(myCg?.specializations?.joinToString(", ") ?: "") }
    var hourlyRate by remember(myCg) { mutableStateOf(myCg?.hourlyRate?.toString() ?: "") }
    var availability by remember(myCg) { mutableStateOf(myCg?.availability?.joinToString(", ") ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = InkPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CarePulseTextField(value = bio, onValueChange = { bio = it },
                label = "Bio", singleLine = false)
            CarePulseTextField(value = specializations, onValueChange = { specializations = it },
                label = "Skills (comma-separated, e.g. Elderly Care, Post-Op)")
            CarePulseTextField(value = hourlyRate, onValueChange = { hourlyRate = it.filter { c -> c.isDigit() } },
                label = "Hourly rate (LKR)")
            CarePulseTextField(value = availability, onValueChange = { availability = it },
                label = "Availability (e.g. Mon AM, Tue PM)")
            Spacer(Modifier.height(8.dp))
            PrimaryButton(text = "Save changes", onClick = {
                vm.updateCaregiverProfile(
                    bio = bio,
                    specializations = specializations.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    hourlyRate = hourlyRate.toIntOrNull() ?: 0,
                    availability = availability.split(",").map { it.trim() }.filter { it.isNotBlank() }
                )
                onDone()
            })
        }
    }
}
```

- [ ] **Step 4: Add Edit Profile button to CaregiverDashboard and nav route**

In `CaregiverDashboardScreen.kt`, add `onEditProfile: () -> Unit = {}` to the function signature, and add an action icon to the `TopAppBar`:

```kotlin
// Add to TopAppBar actions:
IconButton(onClick = onEditProfile) {
    Icon(Icons.Filled.Edit, contentDescription = "Edit profile", tint = InkSecondary)
}
```

Add `Icons.Filled.Edit` import.

In `CarePulseNavGraph.kt`:

1. Add to Routes: `const val EditProfile = "edit-profile"`
2. In `composable(Routes.Home)` for `UserRole.CAREGIVER`, add `onEditProfile`:
```kotlin
UserRole.CAREGIVER -> CaregiverDashboardScreen(
    vm = vm,
    onClockOut = { navController.navigate(Routes.ShiftSummary) },
    onSignOut = signOut,
    onEditProfile = { navController.navigate(Routes.EditProfile) }
)
```
3. Add the composable:
```kotlin
composable(Routes.EditProfile) {
    EditProfileScreen(vm = vm, onDone = { navController.popBackStack() })
}
```

- [ ] **Step 5: Build and verify**

```bash
cd /Users/ranjana/Harsha/CarePulse && ./gradlew :app:assembleDebug 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit ratings + profile edit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/data/model/Models.kt \
  app/src/main/java/com/carepulse/app/data/repository/ \
  app/src/main/java/com/carepulse/app/viewmodel/CarePulseViewModel.kt \
  app/src/main/java/com/carepulse/app/ui/screens/
git commit -m "feat: caregiver ratings, reviews on detail screen, caregiver self-edit profile

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 11: Booking Status Tracking

**Files:**
- Modify: `data/model/Models.kt` (add `agencyId` to `Booking`)
- Modify: `data/repository/CarePulseRepository.kt`
- Modify: `data/repository/FirestoreCarePulseRepository.kt`
- Modify: `viewmodel/CarePulseViewModel.kt`
- Modify: `ui/screens/customer/ActivityScreen.kt`
- Modify: `ui/screens/agency/AgencyScreens.kt` (AgencyBillingScreen)
- Modify: `ui/screens/caregiver/CaregiverDashboardScreen.kt`

- [ ] **Step 1: Add agencyId to Booking model**

In `Models.kt`, change the `Booking` data class to add `agencyId`:

```kotlin
data class Booking(
    val id: String,
    val caregiverId: String,
    val customerName: String,
    val patientName: String,
    val dateLabel: String,
    val timeSlot: String,
    val totalCost: Int,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val customerUid: String? = null,
    val caregiverUid: String? = null,
    val agencyId: String? = null          // NEW: for agency billing queries
)
```

- [ ] **Step 2: Add updateBookingStatus to repository interface**

In `CarePulseRepository.kt`, add:

```kotlin
suspend fun updateBookingStatus(bookingId: String, status: BookingStatus)
```

Add import: `import com.carepulse.app.data.model.BookingStatus`

- [ ] **Step 3: Implement in FirestoreCarePulseRepository**

After `saveCaregiver`, add:

```kotlin
override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
    runCatching {
        bookingsCol.document(bookingId).update("status", status.name).await()
    }
}
```

Also update `toBooking()` mapper to read `agencyId` and `status`:

Find the `toBooking()` extension function and make sure it reads:
```kotlin
status = try { BookingStatus.valueOf(getString("status") ?: "CONFIRMED") }
         catch (e: Exception) { BookingStatus.CONFIRMED },
agencyId = getString("agencyId")
```

- [ ] **Step 4: Add clockIn to CarePulseViewModel**

In `CarePulseViewModel.kt`, after `updateCaregiverProfile`, add:

```kotlin
// --- Booking status ---------------------------------------------------------

/** Caregiver clocks in — moves booking from CONFIRMED to IN_PROGRESS. */
fun clockIn(bookingId: String) {
    viewModelScope.launch {
        repo.updateBookingStatus(bookingId, BookingStatus.IN_PROGRESS)
    }
}

/** Called when shift summary is submitted — mark booking COMPLETED. */
fun completeBooking(bookingId: String) {
    viewModelScope.launch {
        repo.updateBookingStatus(bookingId, BookingStatus.COMPLETED)
    }
}

/** Bookings for the signed-in family. */
val familyBookings: StateFlow<List<Booking>> =
    combine(repo.bookings, _profile) { all, profile ->
        val uid = profile?.uid ?: return@combine emptyList()
        all.filter { it.customerUid == uid }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

/** Bookings for the signed-in agency. */
val agencyBookings: StateFlow<List<Booking>> =
    combine(repo.bookings, _profile) { all, profile ->
        val agencyId = profile?.agencyId ?: return@combine emptyList()
        all.filter { it.agencyId == agencyId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

Add import: `import com.carepulse.app.data.model.BookingStatus`

- [ ] **Step 5: Rewrite ActivityScreen with real bookings**

Replace `ActivityScreen.kt` content:

```kotlin
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.Booking
import com.carepulse.app.data.model.BookingStatus
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.*
import com.carepulse.app.viewmodel.CarePulseViewModel

@Composable
fun ActivityScreen(vm: CarePulseViewModel, onRateCaregiver: (String, String) -> Unit = { _, _ -> }) {
    val bookings by vm.familyBookings.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Past")

    val upcoming = bookings.filter { it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.IN_PROGRESS }
    val past = bookings.filter { it.status == BookingStatus.COMPLETED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", style = MaterialTheme.typography.headlineMedium,
                    color = InkPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = CreamBackground
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent,
                contentColor = PastelMintDeep) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i },
                        text = { Text(title) })
                }
            }
            val list = if (selectedTab == 0) upcoming else past
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No ${tabs[selectedTab].lowercase()} bookings.",
                        color = InkSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(list) { booking ->
                        BookingCard(booking, onRateCaregiver)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking, onRate: (String, String) -> Unit) {
    val statusColor = when (booking.status) {
        BookingStatus.CONFIRMED -> InkSecondary
        BookingStatus.IN_PROGRESS -> PastelMintDeep
        BookingStatus.COMPLETED -> InkPrimary
    }
    PastelCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(booking.patientName.ifBlank { "Patient" },
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary, fontWeight = FontWeight.SemiBold)
                    Text("${booking.dateLabel} · ${booking.timeSlot}",
                        style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
                PastelChip(booking.status.name.lowercase().replace("_", " ")
                    .replaceFirstChar { it.uppercase() })
            }
            Spacer(Modifier.height(6.dp))
            Text("LKR ${booking.totalCost}", style = MaterialTheme.typography.bodyMedium,
                color = statusColor, fontWeight = FontWeight.Medium)
            if (booking.status == BookingStatus.COMPLETED) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { onRate(booking.caregiverId, booking.id) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Rate caregiver")
                }
            }
        }
    }
}
```

- [ ] **Step 6: Rewrite AgencyBillingScreen with real data**

In `AgencyScreens.kt`, replace `AgencyBillingScreen`:

```kotlin
@Composable
fun AgencyBillingScreen(vm: CarePulseViewModel) {
    val bookings by vm.agencyBookings.collectAsState()
    val grouped = bookings.groupBy { it.caregiverId }
    val totalEarnings = bookings.sumOf { it.totalCost }

    AgencyScaffold("Billing") { mod ->
        if (bookings.isEmpty()) {
            EmptyState(Icons.Filled.Payments, "No bookings yet",
                "Booking costs and payments will be tracked here.")
        } else {
            LazyColumn(mod, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    PastelCard {
                        Column {
                            Text("Total earnings", style = MaterialTheme.typography.bodyMedium,
                                color = InkSecondary)
                            Text("LKR $totalEarnings",
                                style = MaterialTheme.typography.headlineMedium,
                                color = PastelMintDeep, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                grouped.forEach { (caregiverId, cgBookings) ->
                    val cgName = cgBookings.firstOrNull()?.caregiverUid ?: caregiverId
                    item {
                        Text(cgName, style = MaterialTheme.typography.titleSmall,
                            color = InkSecondary, modifier = androidx.compose.ui.Modifier.padding(top = 8.dp))
                    }
                    items(cgBookings) { b ->
                        PastelCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(b.patientName, style = MaterialTheme.typography.bodyLarge,
                                        color = InkPrimary)
                                    Text("${b.dateLabel} · ${b.timeSlot}",
                                        style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                }
                                PastelChip("LKR ${b.totalCost}")
                            }
                        }
                    }
                }
            }
        }
    }
}
```

Note: you'll need to add `import androidx.compose.foundation.lazy.items` at the top of `AgencyScreens.kt` if it's missing.

- [ ] **Step 7: Add upcoming shifts to CaregiverDashboardScreen**

In `CaregiverDashboardScreen.kt`, collect `vm.bookings` and filter by `caregiverUid == profile?.uid`. Add an "Upcoming shifts" section below the clock-in card. The clock-in button should call `vm.clockIn(booking.id)`:

```kotlin
// Add at top of the composable:
val allBookings by vm.bookings.collectAsState()
val profile by vm.profile.collectAsState()
val myShifts = allBookings.filter {
    it.caregiverUid == profile?.uid &&
    (it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.IN_PROGRESS)
}

// Add after the clock-in card:
if (myShifts.isNotEmpty()) {
    Spacer(Modifier.height(16.dp))
    Text("Upcoming shifts", style = MaterialTheme.typography.titleMedium,
        color = InkPrimary, fontWeight = FontWeight.SemiBold)
    myShifts.forEach { shift ->
        Spacer(Modifier.height(8.dp))
        PastelCard {
            Column {
                Text(shift.patientName, style = MaterialTheme.typography.bodyLarge, color = InkPrimary)
                Text("${shift.dateLabel} · ${shift.timeSlot}",
                    style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                if (shift.status == BookingStatus.CONFIRMED) {
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton(text = "Clock in", onClick = { vm.clockIn(shift.id) })
                }
            }
        }
    }
}
```

- [ ] **Step 8: Final build + install on tablet**

```bash
cd /Users/ranjana/Harsha/CarePulse
./gradlew :app:assembleDebug 2>&1 | tail -5
# Then check tablet is connected:
adb devices
# If device shown, install:
./gradlew :app:installDebug
adb shell monkey -p com.carepulse.app -c android.intent.category.LAUNCHER 1
```

- [ ] **Step 9: Commit booking history**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add -A
git commit -m "feat: booking status tracking, family booking history, agency earnings, caregiver shifts

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Verification Checklist

### Notifications
- [ ] Submit a care request as Family → agency device gets a push notification within ~5 seconds
- [ ] Assign a caregiver (as Agency) → family device gets push "Caregiver assigned"
- [ ] Submit shift summary (as Caregiver) → family device gets push "Shift report filed"
- [ ] Firebase console → Functions → Logs: no unhandled errors

### Chat
- [ ] Family opens Messages tab → sees list of agencies
- [ ] Tap an agency → conversation opens
- [ ] Send a message → appears instantly (real-time Firestore)
- [ ] Open same chat on agency's Messages tab → message appears
- [ ] Kill and reopen app → messages persist

### Ratings
- [ ] As Family, go to CaregiverDetailScreen → see reviews section (empty at first)
- [ ] After a completed booking, ActivityScreen shows "Rate caregiver" button
- [ ] Submit a 4-star review → appears on CaregiverDetailScreen
- [ ] Caregiver's overall rating updates in the caregiver list

### Booking History
- [ ] Family ActivityScreen shows upcoming bookings with status chips
- [ ] Caregiver clocks in → family's booking status changes to "In Progress" in real time
- [ ] After shift summary, status changes to "Completed"
- [ ] Agency Billing tab shows real LKR totals per caregiver
