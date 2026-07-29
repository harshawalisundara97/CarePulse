@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.ChatMessage
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.CardSurface
import com.carepulse.app.ui.theme.AccentPrimary
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
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

    // Parse agencyId from chatId (format: "{agencyId}_{familyUid}")
    val agencyId = chatId.substringBefore("_")
    val agencies by vm.agencies.collectAsState()
    LaunchedEffect(Unit) { vm.loadAgencies() }
    val agencyName = agencies.firstOrNull { it.id == agencyId }?.name ?: "Agency"

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(agencyName, color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
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
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = AccentPrimary)
                }
            }
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { Spacer(Modifier.padding(top = 4.dp)) }
            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderUid == profile?.uid
                MessageBubble(msg, isMine)
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, isMine: Boolean) {
    val bubbleColor = if (isMine) AccentPrimary else CardSurface
    val textColor = if (isMine) Color.White else TextPrimary
    val alignment = if (isMine) Alignment.End else Alignment.Start

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        if (!isMine) {
            Text(
                msg.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
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
