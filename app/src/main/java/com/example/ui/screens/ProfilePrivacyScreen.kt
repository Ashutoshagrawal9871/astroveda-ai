package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfileEntity
import com.example.ui.theme.*

@Composable
fun ProfilePrivacyScreen(
    profiles: List<UserProfileEntity>,
    currentProfile: UserProfileEntity?,
    onSelectProfile: (Long) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onAddNewProfile: () -> Unit,
    onClearChatHistory: () -> Unit,
    onToggleAiMemory: (Boolean) -> Unit,
    onSetLanguage: (String) -> Unit,
    onViewPolicy: () -> Unit,
    onNavigateToPlans: (() -> Unit)? = null,
    userEmail: String? = null,
    userDisplayName: String? = null,
    isGuestMode: Boolean = false,
    onSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDeleteAccount: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val reminderPrefs = remember { com.example.util.ReminderPreferences.getInstance(context) }
    var isDailyReminderOn by remember { mutableStateOf(reminderPrefs.isDailyReminderEnabled) }
    var isFestivalReminderOn by remember { mutableStateOf(reminderPrefs.isFestivalReminderEnabled) }
    var isQuietHoursOn by remember { mutableStateOf(reminderPrefs.isQuietHoursEnabled) }

    var aiMemoryEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("en") }

    var showDeleteConfirmDialog by remember { mutableStateOf<Long?>(null) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
    var showTransactionHistory by remember { mutableStateOf(false) }

    if (showTransactionHistory) {
        TransactionHistoryScreen(
            onBack = { showTransactionHistory = false },
            onNavigateToPlans = { showTransactionHistory = false }
        )
        return
    }

    Scaffold(
        containerColor = DeepCosmic
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Header Card ─────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(HeroGradient))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Profile & Settings",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Account · Birth Charts · Privacy · Language",
                                color = TextSilver.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ── Account Card ────────────────────────────────────────────
            item {
                // Hero Authenticated Account Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_account_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(GoldGradient))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(
                                            Brush.radialGradient(listOf(SaffronOrange.copy(alpha = 0.4f), Color.Transparent)),
                                            CircleShape
                                        )
                                        .border(1.5.dp, MysticGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (if (isGuestMode) "?" else userDisplayName?.firstOrNull()?.uppercaseChar()?.toString()) ?: "✦",
                                        color = MysticGold,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isGuestMode) "Guest Seeker (Local Mode)" else (userDisplayName ?: "Seeker"),
                                        color = MysticGold,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isGuestMode) "Local Device Storage" else (userEmail ?: "Google Account Connected"),
                                        color = TextSilver,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (isGuestMode) {
                                Button(
                                    onClick = onSignIn,
                                    colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = DeepCosmic),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("guest_connect_google_button")
                                ) {
                                    Text("Connect Google", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                FilledTonalButton(
                                    onClick = { showSignOutConfirmDialog = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkCosmicSurface, contentColor = SacredSaffron),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("sign_out_button")
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isGuestMode) Icons.Default.CloudQueue else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isGuestMode) TextMuted else NebulaTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isGuestMode) "Local Storage Active" else "Google Cloud Sync Verified",
                                    color = if (isGuestMode) TextMuted else NebulaTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (!isGuestMode) {
                                TextButton(
                                    onClick = { showDeleteAccountConfirmDialog = true },
                                    modifier = Modifier.testTag("delete_account_button")
                                ) {
                                    Text("Delete Account", color = CrimsonAstro, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))


                // Saved Birth Charts Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saved Birth Charts (Profiles)", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onAddNewProfile) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MysticGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Profile", color = MysticGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Profiles List
            items(profiles) { profile ->
                val isSelected = profile.id == currentProfile?.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectProfile(profile.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) DarkCosmicSurfaceVariant else DarkCosmicCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) MysticGold else BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.name,
                                    color = if (isSelected) MysticGold else TextGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = MysticGold.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text("ACTIVE", color = MysticGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(
                                text = "${profile.birthDate} at ${profile.birthTime} • ${profile.birthPlace}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        if (profiles.size > 1) {
                            IconButton(onClick = { showDeleteConfirmDialog = profile.id }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Language Preference Selection
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text("App & Astrologer Language", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedLanguage == "en",
                        onClick = {
                            selectedLanguage = "en"
                            onSetLanguage("en")
                        },
                        label = { Text("English (English)", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkCosmicSurfaceVariant,
                            selectedLabelColor = MysticGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedLanguage == "hi",
                        onClick = {
                            selectedLanguage = "hi"
                            onSetLanguage("hi")
                        },
                        label = { Text("हिंदी (Hindi)", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkCosmicSurfaceVariant,
                            selectedLabelColor = MysticGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Privacy & Memory Controls
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Privacy & Safety Controls", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AI Astrologer Memory", color = TextGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Allow Devi Priya to recall past consultations across sessions for continuous guidance.", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = aiMemoryEnabled,
                                onCheckedChange = {
                                    aiMemoryEnabled = it
                                    onToggleAiMemory(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MysticGold, checkedTrackColor = DarkCosmicSurfaceVariant)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showClearChatDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Clear Consultation Chat History", color = CrimsonAstro, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Permanently delete conversation logs from this device.", color = TextMuted, fontSize = 11.sp)
                            }
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = CrimsonAstro)
                        }
                    }
                }
            }

            // Vedic Reminders & Quiet Hours (Opt-in)
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Daily Reminders & Notifications", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Opt-in, non-intrusive reminders with quiet hours protection.", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("reminder_preferences_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Daily Cosmic Horoscope Reminder", color = TextGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Morning notification for your personalized insight (${reminderPrefs.dailyReminderTime}).", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isDailyReminderOn,
                                onCheckedChange = {
                                    isDailyReminderOn = it
                                    reminderPrefs.isDailyReminderEnabled = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MysticGold, checkedTrackColor = DarkCosmicSurfaceVariant)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Major Festivals & Muhurat Alerts", color = TextGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Alerts for Ekadashi, Purnima, Amavasya, and seasonal festivals.", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isFestivalReminderOn,
                                onCheckedChange = {
                                    isFestivalReminderOn = it
                                    reminderPrefs.isFestivalReminderEnabled = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MysticGold, checkedTrackColor = DarkCosmicSurfaceVariant)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Quiet Hours Protection (10 PM – 7 AM)", color = TextGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Ensures peaceful rest by disabling notifications at night.", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isQuietHoursOn,
                                onCheckedChange = {
                                    isQuietHoursOn = it
                                    reminderPrefs.isQuietHoursEnabled = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MysticGold, checkedTrackColor = DarkCosmicSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // Trust, Billing & Support Hub
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Plans, Receipts & Policies", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (onNavigateToPlans != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPlans() }
                            .testTag("open_plans_from_profile_card"),
                        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(BorderGold, SacredSaffron)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MysticGold, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Plans, Credits & Subscriptions", color = MysticGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("View active balance, buy consult credits, and manage plans.", color = TextSilver, fontSize = 11.sp)
                                }
                            }
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MysticGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTransactionHistory = true }
                        .testTag("open_transaction_history_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = SacredSaffron, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Transaction History", color = MysticGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("View Google Play orders, subscriptions, and receipts.", color = TextSilver, fontSize = 11.sp)
                            }
                        }
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MysticGold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewPolicy() },
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = MysticGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("App Guidelines & Refund Policy", color = TextGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Google Play refund policy, usage terms, and disclosures.", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MysticGold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Dialogs
        showDeleteConfirmDialog?.let { profileId ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                title = { Text("Delete Birth Chart Profile?", color = MysticGold, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete this birth chart profile?", color = TextSilver) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteProfile(profileId)
                            showDeleteConfirmDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAstro)
                    ) {
                        Text("Delete Profile", color = TextGold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text("Cancel", color = TextSilver)
                    }
                },
                containerColor = DarkCosmicSurface
            )
        }

        if (showClearChatDialog) {
            AlertDialog(
                onDismissRequest = { showClearChatDialog = false },
                title = { Text("Clear All Chat Messages?", color = MysticGold, fontWeight = FontWeight.Bold) },
                text = { Text("This will delete all previous consultation messages with Devi Priya.", color = TextSilver) },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearChatHistory()
                            showClearChatDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAstro)
                    ) {
                        Text("Clear All", color = TextGold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearChatDialog = false }) {
                        Text("Cancel", color = TextSilver)
                    }
                },
                containerColor = DarkCosmicSurface
            )
        }

        if (showSignOutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutConfirmDialog = false },
                title = { Text("Sign Out of Google?", color = MysticGold, fontWeight = FontWeight.Bold) },
                text = { Text("Your saved charts remain securely backed up in your Google Account.", color = TextSilver) },
                confirmButton = {
                    Button(
                        onClick = {
                            showSignOutConfirmDialog = false
                            onSignOut()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SacredSaffron)
                    ) {
                        Text("Sign Out", color = DeepCosmic, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutConfirmDialog = false }) {
                        Text("Cancel", color = TextSilver)
                    }
                },
                containerColor = DarkCosmicSurface
            )
        }

        if (showDeleteAccountConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountConfirmDialog = false },
                title = { Text("Permanently Delete Account?", color = CrimsonAstro, fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently delete your Google session, saved Kundli profiles, and cloud consultation records. This action cannot be undone.", color = TextSilver) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteAccountConfirmDialog = false
                            onDeleteAccount()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAstro)
                    ) {
                        Text("Delete Everything", color = TextGold, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountConfirmDialog = false }) {
                        Text("Cancel", color = TextSilver)
                    }
                },
                containerColor = DarkCosmicSurface
            )
        }
    }
}
