package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.BackendProxyService
import com.example.astro.VedicEngine
import com.example.auth.AuthManager
import com.example.billing.PlayBillingManager
import com.example.billing.PlayProductUiInfo
import com.example.billing.PlayPurchaseState
import com.example.data.AppDatabase
import com.example.data.AstroRepository
import com.example.data.ChatMessageEntity
import com.example.data.FirestoreAstroRepository
import com.example.data.SavedReadingEntity
import com.example.data.UserProfileEntity
import com.example.model.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AstroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AstroRepository
    private val firestoreRepository: FirestoreAstroRepository
    private val kundliBirthDetailsRepository: com.example.data.KundliBirthDetailsRepository

    val birthDetailsRepository: com.example.data.KundliBirthDetailsRepository get() = kundliBirthDetailsRepository

    val currentUser: StateFlow<FirebaseUser?> = AuthManager.currentUser
    val authError: StateFlow<String?> = AuthManager.authError
    val isGuestMode: StateFlow<Boolean> = AuthManager.isGuestMode

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    fun continueAsGuest() {
        AuthManager.continueAsGuest()
    }

    val allProfiles: StateFlow<List<UserProfileEntity>>
    val primaryProfile: StateFlow<UserProfileEntity?>
    val chatMessages: StateFlow<List<ChatMessageEntity>>
    val savedReadings: StateFlow<List<SavedReadingEntity>>
    val billingState: StateFlow<UserBalance>

    private val playBillingManager: PlayBillingManager
    val playPurchaseState: StateFlow<PlayPurchaseState>
    val availablePlayProducts: StateFlow<Map<String, PlayProductUiInfo>>

    private val _purchaseRequiresSignIn = MutableStateFlow(false)
    val purchaseRequiresSignIn: StateFlow<Boolean> = _purchaseRequiresSignIn.asStateFlow()

    private val _purchaseHistory = MutableStateFlow<List<PaymentLogRecord>>(emptyList())
    val purchaseHistory: StateFlow<List<PaymentLogRecord>> = _purchaseHistory.asStateFlow()

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        // Initialize Firebase Authentication
        AuthManager.initialize(application)

        val db = AppDatabase.getDatabase(application)
        repository = AstroRepository(db.astroDao())
        firestoreRepository = FirestoreAstroRepository(application)
        kundliBirthDetailsRepository = com.example.data.KundliBirthDetailsRepository(application)

        allProfiles = repository.allProfiles
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        primaryProfile = repository.primaryProfile
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        chatMessages = repository.chatMessages
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        savedReadings = repository.savedReadings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        billingState = repository.billingState
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserBalance())

        // Initialize Google Play Billing Manager. Every verified purchase triggers a
        // server-authoritative entitlement sync (the backend credits after Play API verification).
        playBillingManager = PlayBillingManager(application, viewModelScope) { _, _ ->
            viewModelScope.launch { syncEntitlementsInternal() }
        }
        playPurchaseState = playBillingManager.purchaseState
        availablePlayProducts = playBillingManager.availableProducts
        playBillingManager.startConnection()

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }

        // Observe auth state changes to synchronize user profile and entitlements with cloud
        viewModelScope.launch {
            AuthManager.currentUser.collect { user ->
                if (user != null) {
                    syncUserProfileToCloud(user)
                    syncEntitlementsInternal()
                }
            }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _isSigningIn.value = true
            val result = AuthManager.signInWithGoogle(activity)
            _isSigningIn.value = false
            if (result.isSuccess) {
                result.getOrNull()?.let { syncUserProfileToCloud(it) }
            }
        }
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSigningIn.value = true
            val result = AuthManager.signUpWithEmail(email, password, displayName)
            _isSigningIn.value = false
            if (result.isSuccess) {
                result.getOrNull()?.let { syncUserProfileToCloud(it) }
                onComplete?.invoke(true)
            } else {
                onComplete?.invoke(false)
            }
        }
    }

    fun signInWithEmail(
        email: String,
        password: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSigningIn.value = true
            val result = AuthManager.signInWithEmail(email, password)
            _isSigningIn.value = false
            if (result.isSuccess) {
                result.getOrNull()?.let { syncUserProfileToCloud(it) }
                onComplete?.invoke(true)
            } else {
                onComplete?.invoke(false)
            }
        }
    }

    fun sendPasswordReset(email: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = AuthManager.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                onComplete(true, "Password reset link sent to your email!")
            } else {
                onComplete(false, result.exceptionOrNull()?.message ?: "Failed to send reset email.")
            }
        }
    }

    fun grantTestCredits(amount: Int = 10) {
        viewModelScope.launch {
            repository.addChatCredits(amount)
        }
    }

    fun signOut() {
        AuthManager.signOut()
    }

    private fun syncUserProfileToCloud(user: FirebaseUser) {
        viewModelScope.launch {
            firestoreRepository.saveUserProfile(
                UserProfileFirestore(
                    id = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Seeker",
                    photoUrl = user.photoUrl?.toString(),
                    preferredLanguage = _language.value,
                    aiMemoryEnabled = true
                )
            )
        }
    }

    fun saveProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            val localId = repository.saveProfile(profile)
            val uid = AuthManager.currentUserId
            if (uid != null) {
                val profileId = if (profile.id == 0L) localId.toString() else profile.id.toString()
                firestoreRepository.saveKundliProfile(
                    KundliProfileFirestore(
                        id = profileId,
                        userId = uid,
                        name = profile.name,
                        birthDate = profile.birthDate,
                        birthTime = profile.birthTime,
                        birthPlace = profile.birthPlace,
                        latitude = profile.lat,
                        longitude = profile.lng,
                        gender = profile.gender,
                        isTimeApproximate = profile.isTimeUnknown
                    )
                )
            }
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            repository.deleteProfile(id)
            val uid = AuthManager.currentUserId
            if (uid != null) {
                firestoreRepository.deleteKundliProfile(uid, id.toString())
            }
        }
    }

    fun selectProfile(id: Long) {
        viewModelScope.launch {
            repository.setPrimaryProfile(id)
        }
    }

    val astroRepository: AstroRepository get() = repository

    fun sendChatMessage(text: String) {
        viewModelScope.launch {
            if (_isSendingChat.value) return@launch
            _isSendingChat.value = true

            val currentP = primaryProfile.value
            val kundliData = if (currentP != null && currentP.birthDate.isNotBlank()) {
                VedicEngine.calculateKundli(
                    name = currentP.name,
                    birthDate = currentP.birthDate,
                    birthTime = currentP.birthTime,
                    place = currentP.birthPlace,
                    isTimeApproximate = currentP.isTimeUnknown,
                    lat = currentP.lat,
                    lng = currentP.lng
                )
            } else null

            repository.sendChatMessageToGemini(
                userMessage = text,
                kundli = kundliData,
                language = _language.value
            )

            // Save message to Firestore if authenticated
            val uid = AuthManager.currentUserId
            if (uid != null) {
                firestoreRepository.saveChatMessage(
                    ChatMessageFirestore(
                        id = "msg_${System.currentTimeMillis()}",
                        userId = uid,
                        sender = "user",
                        text = text,
                        profileId = currentP?.id?.toString() ?: ""
                    )
                )
            }

            _isSendingChat.value = false
            // Mirror the server-side credit deduction (1 answer consumed per reply).
            syncEntitlementsInternal()
        }
    }

    fun saveReading(title: String, category: String, content: String) {
        viewModelScope.launch {
            repository.saveReading(title, category, content)
        }
    }

    fun deleteSavedReading(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedReading(id)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            val uid = AuthManager.currentUserId
            if (uid != null) {
                firestoreRepository.clearChatMessages(uid)
            }
        }
    }

    fun recordCall(type: String, durationSecs: Int) {
        viewModelScope.launch {
            repository.recordCall(type, durationSecs)
        }
    }

    /**
     * Google Play Billing purchase trigger.
     * Purchases are bound to the signed-in Google account so they can be restored on any device.
     */
    fun purchasePlayProduct(activity: Activity, productId: String) {
        if (AuthManager.currentUser.value == null) {
            _purchaseRequiresSignIn.value = true
            return
        }
        playBillingManager.launchPurchaseFlow(activity, productId)
    }

    fun dismissPurchaseSignInPrompt() {
        _purchaseRequiresSignIn.value = false
    }

    fun restorePlayPurchases() {
        playBillingManager.restorePurchases()
    }

    fun openManageSubscriptions(activity: Activity, sku: String? = null) {
        playBillingManager.openManageSubscriptions(activity, sku)
    }

    fun resetPlayPurchaseState() {
        playBillingManager.resetState()
    }

    /**
     * Refreshes the verified purchase ledger from the backend for the history screen.
     */
    fun refreshPurchaseHistory() {
        viewModelScope.launch {
            val proxyUrl = BackendProxyService.getBackendProxyUrl() ?: return@launch
            if (AuthManager.currentUserId == null) return@launch
            val entries = BackendProxyService.fetchPurchaseHistory(proxyUrl) ?: return@launch
            _purchaseHistory.value = entries.map { entry ->
                PaymentLogRecord(
                    transactionId = entry.orderId.ifBlank { entry.productId },
                    packageId = entry.productId,
                    packageName = entry.title,
                    amountInr = entry.amountInr,
                    paymentMethod = entry.paymentMethod,
                    status = entry.status,
                    timestamp = entry.timestampMillis
                )
            }
        }
    }

    /**
     * Pulls the server-authoritative entitlement balances and mirrors them locally.
     * The backend is the single source of truth once a user is signed in.
     */
    private suspend fun syncEntitlementsInternal() {
        if (AuthManager.currentUserId == null) return
        val proxyUrl = BackendProxyService.getBackendProxyUrl() ?: return
        val entitlements = BackendProxyService.fetchEntitlements(proxyUrl) ?: return
        repository.applyServerEntitlements(
            planType = entitlements.planType,
            expiryMillis = entitlements.expiryMillis,
            chatCredits = entitlements.chatCredits,
            voiceMinutes = entitlements.voiceMinutes,
            isAdFree = entitlements.isAdFree
        )
    }

    fun setAiMemory(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAiMemory(enabled)
            val user = AuthManager.currentUser.value
            if (user != null) {
                firestoreRepository.saveUserProfile(
                    UserProfileFirestore(
                        id = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "Seeker",
                        photoUrl = user.photoUrl?.toString(),
                        preferredLanguage = _language.value,
                        aiMemoryEnabled = enabled
                    )
                )
            }
        }
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch {
            repository.setLanguage(lang)
            val user = AuthManager.currentUser.value
            if (user != null) {
                firestoreRepository.saveUserProfile(
                    UserProfileFirestore(
                        id = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "Seeker",
                        photoUrl = user.photoUrl?.toString(),
                        preferredLanguage = lang,
                        aiMemoryEnabled = true
                    )
                )
            }
        }
    }

    /**
     * Account and Data Deletion (Google Play Data Safety & GDPR compliance).
     * Deletion is server-authoritative: Firestore data, entitlements, audit records and the
     * Auth user are removed by the backend. The client only clears local data and signs out
     * after the server confirms, so a failed request never leaves orphaned cloud data.
     */
    fun deleteUserAccountAndData(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val proxyUrl = BackendProxyService.getBackendProxyUrl()
            if (proxyUrl.isNullOrBlank()) {
                onComplete(false)
                return@launch
            }

            if (!BackendProxyService.deleteAccountOnBackend(proxyUrl)) {
                onComplete(false)
                return@launch
            }

            runCatching {
                repository.clearChatHistory()
                repository.clearCallHistory()
                repository.resetBillingState()
                allProfiles.value.forEach { repository.deleteProfile(it.id) }
                savedReadings.value.forEach { repository.deleteSavedReading(it.id) }
            }

            AuthManager.signOut()
            onComplete(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playBillingManager.destroy()
    }
}
