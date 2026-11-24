package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import com.example.ai.BackendProxyService
import com.example.auth.AuthManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

sealed interface PlayPurchaseState {
    data object Idle : PlayPurchaseState
    data class Processing(val message: String) : PlayPurchaseState
    data class Pending(val message: String) : PlayPurchaseState
    data class Success(val productId: String, val orderId: String, val message: String) : PlayPurchaseState
    data class Failed(val errorMessage: String, val canRetry: Boolean = true) : PlayPurchaseState
}

data class PlayProductUiInfo(
    val productId: String,
    val title: String,
    val formattedPrice: String,
    val description: String,
    val isSubscription: Boolean,
    val productDetails: ProductDetails? = null
)

/**
 * Production-ready Google Play Billing Library v8 integration.
 * Google Play Billing is the ONLY payment path for digital goods (Play Payments Policy).
 * All purchases are verified server-side against the Google Play Developer API before
 * any entitlement is applied; verification fails closed when the backend is unreachable.
 */
class PlayBillingManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onPurchaseVerified: (productId: String, orderId: String) -> Unit
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        private const val TAG = "PlayBillingManager"

        // Google Play product IDs — must match Google Play Console configuration.
        const val SUB_MONTHLY = "sub_elite_monthly"
        const val SUB_ANNUAL = "sub_elite_annual"

        const val INAPP_CHAT_QUICK = "payg_chat_quick"
        const val INAPP_CHAT_15 = "payg_chat_15"
        const val INAPP_CHAT_35 = "payg_chat_35"
        const val INAPP_CHAT_100 = "payg_chat_100"

        val SUBSCRIPTION_IDS = listOf(SUB_MONTHLY, SUB_ANNUAL)
        val CONSUMABLE_IDS = listOf(INAPP_CHAT_QUICK, INAPP_CHAT_15, INAPP_CHAT_35, INAPP_CHAT_100)
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    private val _purchaseState = MutableStateFlow<PlayPurchaseState>(PlayPurchaseState.Idle)
    val purchaseState: StateFlow<PlayPurchaseState> = _purchaseState.asStateFlow()

    private val _availableProducts = MutableStateFlow<Map<String, PlayProductUiInfo>>(emptyMap())
    val availableProducts: StateFlow<Map<String, PlayProductUiInfo>> = _availableProducts.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    fun startConnection() {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.i(TAG, "Billing client successfully connected to Google Play.")
            _isConnected.value = true
            queryAllProducts()
            restorePurchases()
        } else {
            Log.w(TAG, "Billing setup finished with code: ${billingResult.responseCode} - ${billingResult.debugMessage}")
            _isConnected.value = false
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Billing service disconnected. Retrying connection...")
        _isConnected.value = false
        coroutineScope.launch {
            delay(2000)
            startConnection()
        }
    }

    /**
     * Queries Google Play for localized prices, subscription periods, and product titles.
     */
    fun queryAllProducts() {
        if (!billingClient.isReady) return

        val subProductList = SUBSCRIPTION_IDS.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subProductList)
            .build()

        billingClient.queryProductDetailsAsync(subParams) { billingResult, result ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (details in result.productDetailsList) {
                    productDetailsMap[details.productId] = details
                }
                updateUiProductsMap()
            } else {
                Log.w(TAG, "Query subscriptions failed: ${billingResult.debugMessage}")
            }
        }

        val inAppProductList = CONSUMABLE_IDS.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProductList)
            .build()

        billingClient.queryProductDetailsAsync(inAppParams) { billingResult, result ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (details in result.productDetailsList) {
                    productDetailsMap[details.productId] = details
                }
                updateUiProductsMap()
            } else {
                Log.w(TAG, "Query in-app products failed: ${billingResult.debugMessage}")
            }
        }
    }

    private fun updateUiProductsMap() {
        val map = mutableMapOf<String, PlayProductUiInfo>()

        map[SUB_MONTHLY] = subscriptionUiInfo(
            productId = SUB_MONTHLY,
            fallbackTitle = "Elite Monthly",
            fallbackDescription = "Up to 200 AI chat answers per monthly billing period. Ad-free."
        )
        map[SUB_ANNUAL] = subscriptionUiInfo(
            productId = SUB_ANNUAL,
            fallbackTitle = "Elite Annual",
            fallbackDescription = "Up to 200 AI chat answers refreshed monthly. Ad-free."
        )

        map[INAPP_CHAT_QUICK] = oneTimeUiInfo(INAPP_CHAT_QUICK, "8 Quick Answers", "8 quick Vedic AI questions.")
        map[INAPP_CHAT_15] = oneTimeUiInfo(INAPP_CHAT_15, "18 Chat Answers", "18 Kundli-aware Vedic AI chat answers.")
        map[INAPP_CHAT_35] = oneTimeUiInfo(INAPP_CHAT_35, "45 Chat Answers", "45 Kundli-aware Vedic AI chat answers.")
        map[INAPP_CHAT_100] = oneTimeUiInfo(INAPP_CHAT_100, "110 Chat Answers", "110 Kundli-aware Vedic AI chat answers.")

        _availableProducts.value = map
    }

    private fun subscriptionUiInfo(
        productId: String,
        fallbackTitle: String,
        fallbackDescription: String
    ): PlayProductUiInfo {
        val details = productDetailsMap[productId]
        val offer = details?.subscriptionOfferDetails?.firstOrNull()
        val phase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
        val price = phase?.formattedPrice
        return PlayProductUiInfo(
            productId = productId,
            title = details?.name?.ifBlank { fallbackTitle } ?: fallbackTitle,
            formattedPrice = price ?: "Checking Google Play...",
            description = details?.description?.ifBlank { fallbackDescription } ?: fallbackDescription,
            isSubscription = true,
            productDetails = details
        )
    }

    private fun oneTimeUiInfo(
        productId: String,
        fallbackTitle: String,
        fallbackDescription: String
    ): PlayProductUiInfo {
        val details = productDetailsMap[productId]
        val price = details?.oneTimePurchaseOfferDetailsList?.firstOrNull()?.formattedPrice
        return PlayProductUiInfo(
            productId = productId,
            title = details?.name?.ifBlank { fallbackTitle } ?: fallbackTitle,
            formattedPrice = price ?: "Checking Google Play...",
            description = details?.description?.ifBlank { fallbackDescription } ?: fallbackDescription,
            isSubscription = false,
            productDetails = details
        )
    }

    /**
     * Production-ready fail-closed Google Play Purchase Sheet launcher.
     */
    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val details = productDetailsMap[productId]
        if (details == null) {
            _purchaseState.value = PlayPurchaseState.Failed(
                "Google Play product details are loading or not published in Google Play Console yet. Please check your connection or publish the product IDs ($productId) in Play Console.",
                canRetry = true
            )
            queryAllProducts()
            return
        }

        val isSubscription = SUBSCRIPTION_IDS.contains(productId)
        val productDetailsParamsList = if (isSubscription) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .setOfferToken(offerToken)
                    .build()
            )
        } else {
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _purchaseState.value = PlayPurchaseState.Processing("Launching Google Play Purchase Sheet...")
        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _purchaseState.value = PlayPurchaseState.Failed("Unable to launch Google Play: ${result.debugMessage}")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) {
                    _purchaseState.value = PlayPurchaseState.Idle
                    return
                }
                for (purchase in purchases) {
                    handleIncomingPurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PlayPurchaseState.Idle
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseState.value = PlayPurchaseState.Processing("Item already owned. Restoring access...")
                restorePurchases()
            }
            else -> {
                _purchaseState.value = PlayPurchaseState.Failed("Payment failed (${billingResult.responseCode}): ${billingResult.debugMessage}")
            }
        }
    }

    private fun handleIncomingPurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                _purchaseState.value = PlayPurchaseState.Processing("Verifying purchase with Google Play...")
                coroutineScope.launch {
                    val productId = purchase.products.firstOrNull() ?: ""
                    if (productId.isBlank()) {
                        _purchaseState.value = PlayPurchaseState.Failed("Purchase is missing a product ID.")
                        return@launch
                    }
                    val isSubscription = SUBSCRIPTION_IDS.contains(productId)

                    when (val outcome = verifyPurchaseWithBackend(purchase, productId)) {
                        is BackendVerifyOutcome.Verified,
                        is BackendVerifyOutcome.AlreadyProcessed -> {
                            // Fulfil according to Play rules: acknowledge subscriptions,
                            // consume one-time products exactly once after verification.
                            if (isSubscription) {
                                if (!purchase.isAcknowledged) {
                                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.purchaseToken)
                                        .build()
                                    billingClient.acknowledgePurchase(ackParams) { ackResult ->
                                        Log.i(TAG, "Subscription acknowledgement code: ${ackResult.responseCode}")
                                    }
                                }
                            } else {
                                val consumeParams = ConsumeParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                                billingClient.consumeAsync(consumeParams) { consumeResult, _ ->
                                    Log.i(TAG, "Consumable consumed code: ${consumeResult.responseCode}")
                                }
                            }

                            onPurchaseVerified(productId, purchase.orderId ?: "")
                            _purchaseState.value = PlayPurchaseState.Success(
                                productId = productId,
                                orderId = purchase.orderId ?: "",
                                message = if (outcome is BackendVerifyOutcome.AlreadyProcessed) {
                                    "Previous purchase restored. Your entitlements are up to date."
                                } else {
                                    "Google Play purchase verified! Your entitlements have been activated."
                                }
                            )
                        }
                        is BackendVerifyOutcome.Failed -> {
                            // Fail closed: the purchase stays un-acknowledged/un-consumed
                            // and will be re-verified on the next restore attempt.
                            _purchaseState.value = PlayPurchaseState.Failed(outcome.message)
                        }
                    }
                }
            }
            Purchase.PurchaseState.PENDING -> {
                _purchaseState.value = PlayPurchaseState.Pending("Payment is pending in Google Play. Access will unlock automatically once payment is confirmed.")
            }
            else -> {
                _purchaseState.value = PlayPurchaseState.Failed("Transaction could not be completed.")
            }
        }
    }

    private sealed interface BackendVerifyOutcome {
        data object Verified : BackendVerifyOutcome
        data object AlreadyProcessed : BackendVerifyOutcome
        data class Failed(val message: String) : BackendVerifyOutcome
    }

    /**
     * Verifies the purchase token against the backend, which checks it with the
     * Google Play Developer API. Fails closed: without a successful server
     * verification, no entitlement is ever applied.
     */
    private suspend fun verifyPurchaseWithBackend(
        purchase: Purchase,
        productId: String
    ): BackendVerifyOutcome = withContext(Dispatchers.IO) {
        val proxyBaseUrl = BackendProxyService.getBackendProxyUrl()
        if (proxyBaseUrl.isNullOrBlank()) {
            return@withContext BackendVerifyOutcome.Failed(
                "Purchase verification backend URL is unconfigured. Purchases cannot be verified without server validation."
            )
        }

        val idToken = AuthManager.getValidIdToken()
        if (idToken.isNullOrBlank()) {
            return@withContext BackendVerifyOutcome.Failed(
                "Please sign in with Google so we can verify your purchase with Google Play."
            )
        }

        try {
            val url = "$proxyBaseUrl/api/billing/verify-play-purchase"
            val json = JSONObject().apply {
                put("purchaseToken", purchase.purchaseToken)
                put("orderId", purchase.orderId ?: "")
                put("productId", productId)
                put("isSubscription", SUBSCRIPTION_IDS.contains(productId))
                put("packageName", context.packageName)
                put("purchaseTime", purchase.purchaseTime)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $idToken")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            val responseJson = try {
                if (responseBody.isBlank()) JSONObject() else JSONObject(responseBody)
            } catch (_: Exception) {
                JSONObject()
            }

            when {
                response.code == 409 && responseJson.optBoolean("verified", false) -> {
                    BackendVerifyOutcome.AlreadyProcessed
                }
                response.isSuccessful && responseJson.optBoolean("verified", false) -> {
                    BackendVerifyOutcome.Verified
                }
                response.code == 401 -> BackendVerifyOutcome.Failed(
                    "Your sign-in session expired while verifying the purchase. Please sign in again and tap \"Restore Purchases\"."
                )
                response.code == 503 -> BackendVerifyOutcome.Failed(
                    responseJson.optString(
                        "error",
                        "Purchase verification is temporarily unavailable. Your payment is safe — please retry from \"Restore Purchases\" in a moment."
                    )
                )
                else -> BackendVerifyOutcome.Failed(
                    responseJson.optString(
                        "message",
                        "Google Play could not confirm this purchase. No entitlements were applied."
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error contacting server verification API: ${e.message}")
            BackendVerifyOutcome.Failed(
                "Network error while verifying the purchase. Your payment is safe — tap \"Restore Purchases\" when you are back online."
            )
        }
    }

    /**
     * Restores purchases previously made by the user on Google Play and re-verifies
     * them with the backend so entitlements survive reinstalls and device changes.
     */
    fun restorePurchases() {
        if (!billingClient.isReady) return

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handleIncomingPurchase(purchase)
                    }
                }
            } else {
                Log.w(TAG, "Restore subscriptions failed: ${billingResult.debugMessage}")
            }
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handleIncomingPurchase(purchase)
                    }
                }
            } else {
                Log.w(TAG, "Restore in-app purchases failed: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Official Play Store subscription management link.
     */
    fun openManageSubscriptions(activity: Activity, sku: String? = null) {
        val targetSku = sku ?: SUB_MONTHLY
        val url = "https://play.google.com/store/account/subscriptions?sku=$targetSku&package=${activity.packageName}"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to launch Play Store subscription management: ${e.message}")
        }
    }

    fun resetState() {
        _purchaseState.value = PlayPurchaseState.Idle
    }

    fun destroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
