package com.carepulse.app.data.repository

import com.carepulse.app.data.model.Chat
import com.carepulse.app.data.model.ChatMessage
import com.google.firebase.firestore.DocumentSnapshot
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

    override fun chatsFor(uid: String): StateFlow<List<Chat>> = callbackFlow<List<Chat>> {
        val reg = chatsCol
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toChat() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override fun messagesIn(chatId: String): StateFlow<List<ChatMessage>> = callbackFlow<List<ChatMessage>> {
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
            // Upsert the chat thread header (creates it on first message)
            chatRef.set(chat.toMap()).await()
            chatRef.collection("messages").document(message.id).set(message.toMap()).await()
        }
    }

    override suspend fun loopInCaregiver(chatId: String, caregiverId: String) {
        runCatching {
            chatsCol.document(chatId).update("caregiverId", caregiverId).await()
        }
    }

    // --- Mappers ------------------------------------------------------------

    private fun DocumentSnapshot.toChat(): Chat? = runCatching {
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

    private fun DocumentSnapshot.toMessage(): ChatMessage? = runCatching {
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
