package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AstroRepository
import com.example.data.ChatMessageEntity
import com.example.model.KundliChartData
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * ChatScreen composable with a TextField for user input and a LazyColumn
 * to display conversation history, using the AstroRepository to send messages to Gemini.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    repository: AstroRepository,
    kundli: KundliChartData? = null,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Observe conversation history from Room via AstroRepository
    val messages by repository.chatMessages.collectAsStateWithLifecycle(initialValue = emptyList())

    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    // Auto-scroll to the newest message whenever message count increases
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(DarkCosmicSurfaceVariant, CircleShape)
                                .border(1.dp, BorderGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Astrologer",
                                tint = MysticGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Devi Priya - Vedic AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MysticGold
                            )
                            Text(
                                text = if (isSending) "Consulting birth chart..." else "Powered by Gemini AI",
                                fontSize = 11.sp,
                                color = if (isSending) NebulaTeal else TextMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCosmicSurface,
                    titleContentColor = MysticGold
                )
            )
        },
        containerColor = DeepCosmic,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // LazyColumn to display conversation history
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !isSending) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                            shape = RoundedCornerShape(16.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(BorderGold.copy(alpha = 0.5f))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MysticGold,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Hari Om! Ask Devi Priya",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MysticGold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ask any question about career, marriage, dasha periods, or remedies. Your birth chart is factored into every response.",
                                    fontSize = 12.sp,
                                    color = TextSilver,
                                    lineHeight = 16.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                            .testTag("chat_conversation_list"),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageItem(message = msg)
                        }

                        if (isSending) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MysticGold,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Devi Priya is consulting Gemini & your planetary chart...",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Bar with TextField and Send Button
            Surface(
                color = DarkCosmicSurface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask about career, marriage, transit...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_field"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextGold,
                            unfocusedTextColor = TextSilver,
                            focusedContainerColor = DarkCosmicSurfaceVariant,
                            unfocusedContainerColor = DarkCosmicSurfaceVariant,
                            cursorColor = MysticGold,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSend = inputText.isNotBlank() && !isSending
                    IconButton(
                        onClick = {
                            val textToSend = inputText.trim()
                            if (textToSend.isNotBlank() && !isSending) {
                                inputText = ""
                                isSending = true
                                coroutineScope.launch {
                                    try {
                                        // Sends message to Gemini and persists both query & response in Room
                                        repository.sendChatMessageToGemini(
                                            userMessage = textToSend,
                                            kundli = kundli,
                                            language = language
                                        )
                                    } catch (e: Exception) {
                                        android.util.Log.e("ChatScreen", "Error sending message", e)
                                    } finally {
                                        isSending = false
                                    }
                                }
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (canSend) MysticGold else DarkCosmicSurfaceVariant,
                                shape = CircleShape
                            )
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = if (canSend) DeepCosmic else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessageEntity) {
    val isUser = message.senderRole == "user"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) DarkCosmicSurfaceVariant else DarkCosmicCard
            ),
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            border = if (!isUser) {
                CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderGold)
                )
            } else null
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isUser) Icons.Default.Person else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isUser) TextSilver else MysticGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isUser) "You" else "Devi Priya",
                        color = if (isUser) TextSilver else MysticGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = message.text,
                    color = if (isUser) TextSilver else TextGold,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
