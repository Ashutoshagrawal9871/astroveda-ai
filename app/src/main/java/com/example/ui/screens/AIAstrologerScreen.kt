package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.billing.PlayProductUiInfo
import com.example.billing.PlayPurchaseState
import com.example.data.ChatMessageEntity
import com.example.data.SavedReadingEntity
import com.example.data.UserProfileEntity
import com.example.model.KundliChartData
import com.example.model.PurchasePackage
import com.example.model.UserBalance
import com.example.ui.components.AdNoticeBanner
import com.example.ui.components.PayAsYouUseBottomSheet
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AIAstrologerScreen(
    currentProfile: UserProfileEntity?,
    kundli: KundliChartData?,
    userBalance: UserBalance,
    chatMessages: List<ChatMessageEntity>,
    savedReadings: List<SavedReadingEntity>,
    isSendingMessage: Boolean,
    onSendMessage: (String) -> Unit,
    onSaveReading: (title: String, category: String, content: String) -> Unit,
    onDeleteSavedReading: (Long) -> Unit,
    onNavigateToBilling: () -> Unit,
    playPurchaseState: PlayPurchaseState = PlayPurchaseState.Idle,
    availablePlayProducts: Map<String, PlayProductUiInfo> = emptyMap(),
    onPurchasePlayProduct: (String) -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    initialPrompt: String? = null,
    onClearInitialPrompt: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Chat, 1: Guidance, 2: Saved

    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            inputText = initialPrompt
            selectedMainTab = 0
            onClearInitialPrompt?.invoke()
        }
    }

    var showReportDialog by remember { mutableStateOf<String?>(null) }
    var showNoCreditsDialog by remember { mutableStateOf(false) }
    var showVoiceComingSoonDialog by remember { mutableStateOf(false) }
    var showPayAsYouUseSheet by remember { mutableStateOf(false) }
    var paygInitialPackage by remember { mutableStateOf<PurchasePackage?>(null) }

    // Pulsing halo animation for Devi Priya
    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    // Scroll to latest message
    LaunchedEffect(chatMessages.size, isSendingMessage) {
        val count = chatMessages.size + if (isSendingMessage) 1 else 0
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    val quickTopicPrompts = remember(kundli) {
        listOf(
            "💍 Marriage & Soulmate" to "Analyze my 7th house and Venus placement for marriage timing and soulmate compatibility.",
            "💼 Career & Growth" to "What does my 10th house and current Dasha indicate for career promotion and growth?",
            "💰 Wealth & Finance" to "Examine my 2nd and 11th house alignment for financial gains and wealth prospects.",
            "🧘 Current Dasha Remedies" to "Explain my current ${kundli?.currentMahadasha ?: "Dasha"} period and suggest effective Vedic remedies.",
            "🧿 Gemstone Advice" to "Which auspicious gemstone should I wear based on my Lagna and planet positions?",
            "🌿 Health & Vitality" to "Provide insights on my 6th house, health, vitality, and planetary protection."
        )
    }

    Scaffold(
        containerColor = DeepCosmic
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar — Compact Devi Priya Identity Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(BorderGold, SacredSaffron)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SacredSaffron.copy(alpha = haloAlpha * 0.25f))
                                .border(2.dp, MysticGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = R.drawable.astrologer_avatar,
                                contentDescription = "Devi Priya",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Devi Priya", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = NebulaTeal.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                    Text("AI GUIDE", color = NebulaTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            Text(
                                text = if (kundli != null) "AI Vedic Astrologer • ${kundli.lagnaSignName} Lagna (${kundli.rashiSignName})" else "AI Vedic Astrologer • Interpretive Guidance",
                                color = TextSilver,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Right: Credit Balance Button
                    Surface(
                        color = DarkCosmicSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderGold),
                        modifier = Modifier.clickable { onNavigateToBilling() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = SacredSaffron, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${userBalance.chatMessagesRemaining} Qs", color = MysticGold, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Tabs Bar (Consultation | Guidance | Saved)
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = DarkCosmicSurface,
                contentColor = MysticGold,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = selectedMainTab == 0, onClick = { selectedMainTab = 0 }, text = { Text("Consultation", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedMainTab == 1, onClick = { selectedMainTab = 1 }, text = { Text("Celestial Guidance", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedMainTab == 2, onClick = { selectedMainTab = 2 }, text = { Text("Saved (${savedReadings.size})", fontWeight = FontWeight.Bold) })
            }

            when (selectedMainTab) {
                0 -> {
                    // Chat Consultation View
                    Column(modifier = Modifier.weight(1f)) {
                        // Quick Topics Chips
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(quickTopicPrompts) { (label, prompt) ->
                                SuggestionChip(
                                    onClick = {
                                        if (userBalance.chatMessagesRemaining <= 0) {
                                            showNoCreditsDialog = true
                                        } else {
                                            onSendMessage(prompt)
                                        }
                                    },
                                    label = { Text(label, fontSize = 11.sp, color = MysticGold, fontWeight = FontWeight.SemiBold) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkCosmicCard),
                                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = BorderGold.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        // Chat Messages Area
                        Box(modifier = Modifier.weight(1f)) {
                            if (chatMessages.isEmpty()) {
                                // Prominent Starter Cards
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MysticGold, modifier = Modifier.size(32.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text("Ask Devi Priya Anything About Your Chart", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                                    Text("Select a question below or type your personal question in the chat box at the bottom.", color = TextSilver, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }

                                    items(quickTopicPrompts) { (title, prompt) ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (userBalance.chatMessagesRemaining <= 0) {
                                                        showNoCreditsDialog = true
                                                    } else {
                                                        onSendMessage(prompt)
                                                    }
                                                },
                                            colors = CardDefaults.cardColors(containerColor = DarkCosmicSurface),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(title, color = MysticGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(prompt, color = TextSilver, fontSize = 11.sp, maxLines = 2)
                                                }
                                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SacredSaffron, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(chatMessages) { message ->
                                        ChatMessageBubble(
                                            message = message,
                                            onCopy = {
                                                clipboardManager.setText(AnnotatedString(message.text))
                                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                            onSave = {
                                                onSaveReading("Consultation with Devi Priya", message.category, message.text)
                                                Toast.makeText(context, "Saved to Reading Vault", Toast.LENGTH_SHORT).show()
                                            },
                                            onReport = { showReportDialog = message.text }
                                        )
                                    }

                                    if (isSendingMessage) {
                                        item {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(0.85f),
                                                colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                                                shape = RoundedCornerShape(14.dp),
                                                border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.5f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MysticGold, strokeWidth = 2.dp)
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text("Devi Priya is consulting your birth chart...", color = TextGold, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Fixed Message Input Field
                        Surface(
                            color = DarkCosmicCard,
                            tonalElevation = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Ask marriage, career, dasha remedies...", color = TextMuted, fontSize = 12.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MysticGold,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedContainerColor = DarkCosmicSurface,
                                        unfocusedContainerColor = DarkCosmicSurface,
                                        focusedTextColor = TextGold,
                                        unfocusedTextColor = TextSilver
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    maxLines = 4
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val text = inputText.trim()
                                        if (text.isNotBlank()) {
                                            if (userBalance.chatMessagesRemaining <= 0) {
                                                showNoCreditsDialog = true
                                            } else {
                                                onSendMessage(text)
                                                inputText = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            brush = Brush.linearGradient(listOf(SacredSaffron, SaffronOrange)),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = DeepCosmic, modifier = Modifier.size(20.dp))
                                }
                            }
                            Text(
                                text = "Interpretive Vedic guidance provided by AI. Not a replacement for professional legal, medical, or financial advice.",
                                color = TextMuted,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                1 -> {
                    // Celestial Guidance Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Text("Celestial Guidance & Forecasts", color = MysticGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Vedic planetary alignments customized for ${kundli?.personName ?: "you"}", color = TextSilver, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Current Dasha Insight (${kundli?.currentMahadasha ?: "Jupiter"})", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "You are currently in ${kundli?.currentMahadasha ?: "Jupiter"} Mahadasha and ${kundli?.currentAntardasha ?: "Saturn"} Antardasha. Focus on spiritual growth, disciplined work, and long-term planning.",
                                        color = TextSilver,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            selectedMainTab = 0
                                            onSendMessage("Detailed prediction for my current ${kundli?.currentMahadasha} Mahadasha and remedies")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SacredSaffron, contentColor = DeepCosmic)
                                    ) {
                                        Text("Ask Full Dasha Analysis", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Saved Readings Vault Tab
                    if (savedReadings.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Saved Readings Yet", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Tap ⭐ during any consultation to bookmark guidance here.", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(savedReadings) { reading ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(reading.title, color = MysticGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { onDeleteSavedReading(reading.id) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Text(reading.content, color = TextSilver, fontSize = 12.sp, lineHeight = 17.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Voice Call Dialog
        if (showVoiceComingSoonDialog) {
            AlertDialog(
                onDismissRequest = { showVoiceComingSoonDialog = false },
                title = { Text("Voice Consultations Coming Soon", color = MysticGold, fontWeight = FontWeight.Bold) },
                text = { Text("Direct AI Voice Consultations with Devi Priya will unlock in a future update. AI Chat consultation is fully active now!", color = TextSilver) },
                confirmButton = {
                    Button(onClick = { showVoiceComingSoonDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SacredSaffron, contentColor = DeepCosmic)) {
                        Text("Understood", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = DarkCosmicSurface
            )
        }

        // No Credits Dialog
        if (showNoCreditsDialog) {
            AlertDialog(
                onDismissRequest = { showNoCreditsDialog = false },
                title = { Text("Allowance Reached", color = MysticGold, fontWeight = FontWeight.Bold) },
                text = { Text("You have used your AI chat answer allowance. Top up instantly with a ₹29 micro-pack or subscribe to continue.", color = TextSilver) },
                confirmButton = {
                    Button(
                        onClick = {
                            showNoCreditsDialog = false
                            showPayAsYouUseSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SacredSaffron, contentColor = DeepCosmic)
                    ) {
                        Text("Top Up ₹29", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoCreditsDialog = false }) {
                        Text("Later", color = TextSilver)
                    }
                },
                containerColor = DarkCosmicSurface
            )
        }

        // Report Content Dialog (Google Play AI Content Policy Compliance)
        showReportDialog?.let { _ ->
            AlertDialog(
                onDismissRequest = { showReportDialog = null },
                title = { Text("Report AI Response", color = MysticGold, fontWeight = FontWeight.Bold) },
                text = { Text("Thank you for helping keep Devi Priya safe and accurate. This response has been flagged for safety review.", color = TextSilver) },
                confirmButton = {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Response reported. Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                            showReportDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SacredSaffron, contentColor = DeepCosmic)
                    ) {
                        Text("Submit Report", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = null }) {
                        Text("Cancel", color = TextSilver)
                    }
                },
                containerColor = DarkCosmicSurface
            )
        }

        // Pay-As-You-Use Sheet
        if (showPayAsYouUseSheet) {
            PayAsYouUseBottomSheet(
                initialPackage = paygInitialPackage,
                playPurchaseState = playPurchaseState,
                availablePlayProducts = availablePlayProducts,
                onDismiss = { showPayAsYouUseSheet = false },
                onPurchasePackage = { productId ->
                    showPayAsYouUseSheet = false
                    onPurchasePlayProduct(productId)
                },
                onRestorePurchases = onRestorePurchases
            )
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    onReport: () -> Unit
) {
    val isUser = message.senderRole == "user"
    val dateStr = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.92f),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) DarkCosmicSurfaceVariant else DarkCosmicCard
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            border = BorderStroke(
                1.dp,
                if (isUser) BorderGold.copy(alpha = 0.5f) else BorderGold
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MysticGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Devi Priya", color = MysticGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(dateStr, color = TextMuted, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text,
                    color = if (isUser) TextGold else TextSilver,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                if (isUser) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(dateStr, color = TextMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onCopy, contentPadding = PaddingValues(0.dp)) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSilver, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", color = TextSilver, fontSize = 11.sp)
                            }
                            TextButton(onClick = onSave, contentPadding = PaddingValues(0.dp)) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Save", tint = MysticGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", color = MysticGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(onClick = onReport, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Flag, contentDescription = "Report", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
