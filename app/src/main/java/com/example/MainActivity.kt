package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.astro.VedicEngine
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AstroViewModel

enum class MainDestination(val title: String, val icon: ImageVector, val testTag: String) {
    TODAY("Today", Icons.Default.WbSunny, "nav_today"),
    KUNDLI("Kundli", Icons.Default.AutoFixHigh, "nav_kundli"),
    EXPLORE("Explore", Icons.Default.Explore, "nav_explore"),
    AI_ASTROLOGER("Ask AI", Icons.Default.AutoAwesome, "nav_ai_astrologer"),
    PROFILE("Profile", Icons.Default.Person, "nav_profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: AstroViewModel = viewModel()) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(MainDestination.TODAY) }

    // Sub-screen states
    var isPolicyScreenActive by remember { mutableStateOf(false) }
    var isPlansScreenActive by remember { mutableStateOf(false) }
    var showNewProfileDialog by remember { mutableStateOf(false) }
    var pendingAskAiPrompt by remember { mutableStateOf<String?>(null) }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isGuestMode by viewModel.isGuestMode.collectAsStateWithLifecycle()
    val isSigningIn by viewModel.isSigningIn.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val currentProfile by viewModel.primaryProfile.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val savedReadings by viewModel.savedReadings.collectAsStateWithLifecycle()
    val userBalance by viewModel.billingState.collectAsStateWithLifecycle()
    val isSendingChat by viewModel.isSendingChat.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val playPurchaseState by viewModel.playPurchaseState.collectAsStateWithLifecycle()
    val availablePlayProducts by viewModel.availablePlayProducts.collectAsStateWithLifecycle()
    val purchaseHistory by viewModel.purchaseHistory.collectAsStateWithLifecycle()
    val requiresSignInForPurchase by viewModel.purchaseRequiresSignIn.collectAsStateWithLifecycle()

    // Refresh the verified purchase ledger whenever Plans screen is opened.
    LaunchedEffect(isPlansScreenActive) {
        if (isPlansScreenActive) {
            viewModel.refreshPurchaseHistory()
        }
    }

    // Composable Auth Gate: Unauthenticated users are shown SignInScreen unless exploring as Guest
    if (currentUser == null && !isGuestMode) {
        if (isPolicyScreenActive) {
            PolicyViewerScreen(onBack = { isPolicyScreenActive = false })
        } else {
            SignInScreen(
                isLoading = isSigningIn,
                errorMessage = authError,
                onSignInWithGoogle = {
                    (context as? Activity)?.let { act ->
                        viewModel.signInWithGoogle(act)
                    }
                },
                onContinueAsGuest = {
                    viewModel.continueAsGuest()
                },
                onViewPolicy = { isPolicyScreenActive = true }
            )
        }
        return
    }

    val currentKundli = remember(currentProfile) {
        currentProfile?.let { p ->
            VedicEngine.calculateKundli(
                name = p.name,
                birthDate = p.birthDate,
                birthTime = p.birthTime,
                place = p.birthPlace,
                isTimeApproximate = p.isTimeUnknown,
                lat = p.lat,
                lng = p.lng
            )
        }
    }

    // Back handler for secondary screens
    BackHandler(enabled = isPolicyScreenActive || isPlansScreenActive) {
        if (isPolicyScreenActive) isPolicyScreenActive = false
        else if (isPlansScreenActive) isPlansScreenActive = false
    }

    if (isPolicyScreenActive) {
        PolicyViewerScreen(
            onBack = { isPolicyScreenActive = false }
        )
        return
    }

    if (isPlansScreenActive) {
        PlansBillingScreen(
            userBalance = userBalance,
            playPurchaseState = playPurchaseState,
            availablePlayProducts = availablePlayProducts,
            purchaseHistory = purchaseHistory,
            onPurchaseProduct = { productId ->
                (context as? Activity)?.let { act ->
                    viewModel.purchasePlayProduct(act, productId)
                }
            },
            onRestorePurchases = { viewModel.restorePlayPurchases() },
            onRefreshHistory = { viewModel.refreshPurchaseHistory() },
            onManageSubscriptions = { sku ->
                (context as? Activity)?.let { act ->
                    viewModel.openManageSubscriptions(act, sku)
                }
            },
            onViewPolicy = { isPolicyScreenActive = true },
            onBack = { isPlansScreenActive = false }
        )
        return
    }

    // Main Scaffold with Bottom Navigation
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkCosmicSurface,
                contentColor = MysticGold,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                MainDestination.entries.forEach { destination ->
                    val selected = currentDestination == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                tint = if (selected) DeepCosmic else TextSilver
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MysticGold else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepCosmic,
                            indicatorColor = MysticGold,
                            unselectedIconColor = TextSilver,
                            unselectedTextColor = TextMuted,
                            selectedTextColor = MysticGold
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        },
        containerColor = DeepCosmic
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                MainDestination.TODAY -> {
                    TodayScreen(
                        currentProfile = currentProfile,
                        allProfiles = allProfiles,
                        repository = viewModel.astroRepository,
                        language = language,
                        chatCredits = userBalance.chatMessagesRemaining,
                        onSelectProfile = { viewModel.selectProfile(it) },
                        onAddNewProfile = { showNewProfileDialog = true },
                        onNavigateToExplore = { currentDestination = MainDestination.EXPLORE },
                        onNavigateToAskAi = { prompt ->
                            pendingAskAiPrompt = prompt
                            currentDestination = MainDestination.AI_ASTROLOGER
                        },
                        onNavigateToPlans = { isPlansScreenActive = true },
                        onSaveReading = { title, cat, content -> viewModel.saveReading(title, cat, content) }
                    )
                }

                MainDestination.KUNDLI -> {
                    KundliScreen(
                        currentProfile = currentProfile,
                        onSaveProfile = { viewModel.saveProfile(it) },
                        onNavigateToChat = { currentDestination = MainDestination.AI_ASTROLOGER }
                    )
                }

                MainDestination.EXPLORE -> {
                    AstrologyScreen(
                        repository = viewModel.astroRepository,
                        language = language
                    )
                }

                MainDestination.AI_ASTROLOGER -> {
                    AIAstrologerScreen(
                        currentProfile = currentProfile,
                        kundli = currentKundli,
                        userBalance = userBalance,
                        chatMessages = chatMessages,
                        savedReadings = savedReadings,
                        isSendingMessage = isSendingChat,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onSaveReading = { title, cat, content -> viewModel.saveReading(title, cat, content) },
                        onDeleteSavedReading = { viewModel.deleteSavedReading(it) },
                        onNavigateToBilling = { isPlansScreenActive = true },
                        playPurchaseState = playPurchaseState,
                        availablePlayProducts = availablePlayProducts,
                        onPurchasePlayProduct = { productId ->
                            (context as? Activity)?.let { act ->
                                viewModel.purchasePlayProduct(act, productId)
                            }
                        },
                        onRestorePurchases = { viewModel.restorePlayPurchases() },
                        initialPrompt = pendingAskAiPrompt,
                        onClearInitialPrompt = { pendingAskAiPrompt = null }
                    )
                }

                MainDestination.PROFILE -> {
                    ProfilePrivacyScreen(
                        profiles = allProfiles,
                        currentProfile = currentProfile,
                        onSelectProfile = { viewModel.selectProfile(it) },
                        onDeleteProfile = { viewModel.deleteProfile(it) },
                        onAddNewProfile = { showNewProfileDialog = true },
                        onClearChatHistory = { viewModel.clearChatHistory() },
                        onToggleAiMemory = { viewModel.setAiMemory(it) },
                        onSetLanguage = { viewModel.setLanguage(it) },
                        onViewPolicy = { isPolicyScreenActive = true },
                        onNavigateToPlans = { isPlansScreenActive = true },
                        userEmail = currentUser?.email,
                        userDisplayName = currentUser?.displayName,
                        isGuestMode = isGuestMode,
                        onSignIn = {
                            (context as? Activity)?.let { act ->
                                viewModel.signInWithGoogle(act)
                            }
                        },
                        onSignOut = { viewModel.signOut() },
                        onDeleteAccount = {
                            viewModel.deleteUserAccountAndData { success ->
                                Toast.makeText(
                                    context,
                                    if (success) {
                                        "Account and all associated data have been permanently deleted."
                                    } else {
                                        "Could not delete your account. Check your connection and try again, or email support@jyotishai.com."
                                    },
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
            }

            if (showNewProfileDialog) {
                BirthDetailsDialog(
                    initial = null,
                    onDismiss = { showNewProfileDialog = false },
                    onSave = { newProfile ->
                        viewModel.saveProfile(newProfile)
                        showNewProfileDialog = false
                    }
                )
            }

            // Purchases are bound to the signed-in Google account, so guests sign in first.
            if (requiresSignInForPurchase) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissPurchaseSignInPrompt() },
                    title = {
                        Text("Sign In to Purchase", color = MysticGold, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            "Purchases are securely linked to your Google account so they can be restored on any device. Please sign in, then tap Buy again.",
                            color = TextSilver,
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.dismissPurchaseSignInPrompt()
                                (context as? Activity)?.let { act ->
                                    viewModel.signInWithGoogle(act)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SacredSaffron,
                                contentColor = DeepCosmic
                            ),
                            modifier = Modifier.testTag("sign_in_to_purchase_button")
                        ) {
                            Text("Sign In with Google", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissPurchaseSignInPrompt() }) {
                            Text("Not Now", color = TextSilver)
                        }
                    },
                    containerColor = DarkCosmicSurface
                )
            }
        }
    }
}
