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
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.R
import com.carepulse.app.data.model.ChatMessage
import com.carepulse.app.ui.theme.GlassFillSubtle
import com.carepulse.app.ui.theme.GlassFillSubtleDark
import com.carepulse.app.ui.theme.GlassScreen
import com.carepulse.app.ui.theme.LocalIsDarkTheme
import com.carepulse.app.ui.theme.Radii
import com.carepulse.app.ui.theme.Spacing
import com.carepulse.app.ui.theme.glassCard
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

    GlassScreen { hazeState ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(agencyName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.CardPaddingCompact),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier
                            .weight(1f)
                            .glassCard(radius = Radii.Input, hazeState = hazeState),
                        placeholder = { Text("Type a message…") },
                        shape = RoundedCornerShape(Radii.Input),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        enabled = draft.isNotBlank(),
                        onClick = {
                            vm.sendMessage(agencyId, agencyName, draft)
                            draft = ""
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.messages_send),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spacing.ScreenPaddingCompact),
                verticalArrangement = Arrangement.spacedBy(Spacing.ItemGap)
            ) {
                item { Spacer(Modifier.padding(top = 4.dp)) }
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderUid == profile?.uid
                    MessageBubble(msg, isMine)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, isMine: Boolean) {
    val dark = LocalIsDarkTheme.current
    val bubbleColor = if (isMine) {
        MaterialTheme.colorScheme.primary
    } else {
        if (dark) GlassFillSubtleDark else GlassFillSubtle
    }
    val textColor = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        Box(
            Modifier
                .background(bubbleColor, RoundedCornerShape(Radii.Card))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(msg.text, color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
