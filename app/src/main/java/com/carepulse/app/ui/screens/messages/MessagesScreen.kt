@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.Caregiver
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMintDeep
import com.carepulse.app.viewmodel.CarePulseViewModel

/**
 * Conversation list — the core of the family <-> caregiver "connect" idea.
 * For now it lists the caregivers as contacts; tapping a row is wired up for a
 * future chat detail screen.
 */
@Composable
fun MessagesScreen(vm: CarePulseViewModel) {
    val caregivers by vm.caregivers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Messages", style = MaterialTheme.typography.headlineMedium,
                        color = InkPrimary, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = CreamBackground
    ) { padding ->
        if (caregivers.isEmpty()) {
            EmptyMessages(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(caregivers) { cg -> ConversationRow(cg) }
            }
        }
    }
}

@Composable
private fun ConversationRow(caregiver: Caregiver) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable { /* TODO: open chat detail */ }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GeneratedAvatar(
            seed = caregiver.avatarSeed,
            initials = caregiver.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }
                .take(2).joinToString(""),
            size = 52
        )
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(caregiver.name, style = MaterialTheme.typography.titleMedium,
                color = InkPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                "Tap to start a conversation",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyMessages(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(72.dp).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ChatBubbleOutline, null,
                    tint = PastelMintDeep, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("No conversations yet", style = MaterialTheme.typography.titleMedium,
                color = InkPrimary, fontWeight = FontWeight.SemiBold)
            Text("Your chats with caregivers will appear here.",
                style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        }
    }
}
