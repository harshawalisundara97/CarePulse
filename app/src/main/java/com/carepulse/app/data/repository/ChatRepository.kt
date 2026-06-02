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
