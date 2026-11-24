package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.auth.AuthManager
import com.example.model.KundliChartData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object BackendProxyService {

    private const val TAG = "BackendProxyService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getBackendProxyUrl(customOverride: String? = null): String? {
        val custom = customOverride?.trim()
        if (!custom.isNullOrBlank()) return custom.removeSuffix("/")

        val buildUrl = try { BuildConfig.BACKEND_PROXY_URL } catch (_: Exception) { "" }
        if (buildUrl.isNotBlank() && buildUrl.startsWith("http") && !buildUrl.contains("placeholder") && !buildUrl.startsWith("MY_")) {
            return buildUrl.removeSuffix("/")
        }
        return null
    }

    /**
     * Sends consultation request to the Firebase Cloud Functions backend proxy.
     * Uses verified Firebase ID token authentication.
     */
    suspend fun consultAstrologerViaProxy(
        userMessage: String,
        kundli: KundliChartData?,
        chatHistory: List<Pair<String, String>>,
        language: String,
        isVoiceOrVideo: Boolean,
        proxyBaseUrl: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$proxyBaseUrl/api/chat"
            val idToken = AuthManager.getValidIdToken()

            val root = JSONObject().apply {
                put("message", userMessage)
                put("language", language)
                put("isVoiceOrVideo", isVoiceOrVideo)

                if (kundli != null && kundli.birthDate.isNotBlank()) {
                    val kObj = JSONObject().apply {
                        put("personName", kundli.personName)
                        put("birthDate", kundli.birthDate)
                        put("birthTime", kundli.birthTime)
                        put("isTimeApproximate", kundli.isTimeApproximate)
                        put("birthPlace", kundli.birthPlace)
                        put("lagnaSignName", kundli.lagnaSignName)
                        put("rashiSignName", kundli.rashiSignName)
                        put("nakshatra", kundli.nakshatra)
                        put("nakshatraPada", kundli.nakshatraPada)
                        put("currentMahadasha", kundli.currentMahadasha)
                        put("currentAntardasha", kundli.currentAntardasha)
                        put("manglikStatus", kundli.manglikStatus)

                        val planetsArr = JSONArray()
                        for (p in kundli.planets) {
                            planetsArr.put(JSONObject().apply {
                                put("name", p.englishName)
                                put("signName", p.signName)
                                put("house", p.house)
                                put("dignity", p.dignity)
                            })
                        }
                        put("planets", planetsArr)
                    }
                    put("kundli", kObj)
                }

                val historyArr = JSONArray()
                for ((role, text) in chatHistory.takeLast(6)) {
                    historyArr.put(JSONObject().apply {
                        put("role", role)
                        put("text", text)
                    })
                }
                put("chatHistory", historyArr)
            }

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val reqBuilder = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")

            if (!idToken.isNullOrBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer $idToken")
            }

            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string()

            if (response.isSuccessful && !body.isNullOrBlank()) {
                val json = JSONObject(body)
                return@withContext json.optString("reply").ifBlank { null }
            } else {
                Log.w(TAG, "Backend proxy chat request returned code: ${response.code} body: $body")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Backend proxy chat unavailable: ${e.message}")
        }
        null
    }

    /**
     * Checks health and configuration of backend proxy.
     */
    suspend fun checkProxyHealth(proxyBaseUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$proxyBaseUrl/api/health")
                .get()
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Fetches the server-authoritative entitlement balances for the signed-in user.
     * Returns null when the user is not signed in or the server is unreachable.
     */
    suspend fun fetchEntitlements(proxyBaseUrl: String): ServerEntitlements? = withContext(Dispatchers.IO) {
        try {
            val idToken = AuthManager.getValidIdToken() ?: return@withContext null
            val request = Request.Builder()
                .url("$proxyBaseUrl/api/billing/entitlements")
                .get()
                .addHeader("Authorization", "Bearer $idToken")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (!response.isSuccessful || body.isNullOrBlank()) {
                Log.w(TAG, "Fetch entitlements failed with code: ${response.code}")
                return@withContext null
            }
            val json = JSONObject(body)
            ServerEntitlements(
                planType = json.optString("planType", "PAY_AS_YOU_USE"),
                expiryMillis = json.optLong("expiryMillis", 0L),
                chatCredits = json.optInt("chatCredits", 0),
                voiceMinutes = json.optInt("voiceMinutes", 0),
                isAdFree = json.optBoolean("isAdFree", false)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Unable to fetch entitlements: ${e.message}")
            null
        }
    }

    /**
     * Fetches the verified Google Play purchase ledger for the signed-in user.
     */
    suspend fun fetchPurchaseHistory(proxyBaseUrl: String): List<PurchaseHistoryEntry>? = withContext(Dispatchers.IO) {
        try {
            val idToken = AuthManager.getValidIdToken() ?: return@withContext null
            val request = Request.Builder()
                .url("$proxyBaseUrl/api/billing/history")
                .get()
                .addHeader("Authorization", "Bearer $idToken")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (!response.isSuccessful || body.isNullOrBlank()) {
                Log.w(TAG, "Fetch purchase history failed with code: ${response.code}")
                return@withContext null
            }
            val orders = JSONObject(body).optJSONArray("orders") ?: return@withContext emptyList()
            val entries = mutableListOf<PurchaseHistoryEntry>()
            for (i in 0 until orders.length()) {
                val item = orders.optJSONObject(i) ?: continue
                entries.add(
                    PurchaseHistoryEntry(
                        orderId = item.optString("orderId", ""),
                        productId = item.optString("productId", ""),
                        title = item.optString("title", "Digital Purchase"),
                        amountInr = item.optInt("amountInr", 0),
                        isSubscription = item.optBoolean("isSubscription", false),
                        timestampMillis = item.optLong("timestampMillis", 0L),
                        status = item.optString("status", "VERIFIED_ACTIVE"),
                        paymentMethod = item.optString("paymentMethod", "Google Play")
                    )
                )
            }
            return@withContext entries
        } catch (e: Exception) {
            Log.w(TAG, "Unable to fetch purchase history: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Requests complete server-side account and data deletion.
     */
    suspend fun deleteAccountOnBackend(proxyBaseUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val idToken = AuthManager.getValidIdToken() ?: return@withContext false
            val request = Request.Builder()
                .url("$proxyBaseUrl/api/account/delete")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $idToken")
                .build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Account deletion on backend failed: ${e.message}")
            return@withContext false
        }
    }
}

data class ServerEntitlements(
    val planType: String,
    val expiryMillis: Long,
    val chatCredits: Int,
    val voiceMinutes: Int,
    val isAdFree: Boolean
)

data class PurchaseHistoryEntry(
    val orderId: String,
    val productId: String,
    val title: String,
    val amountInr: Int,
    val isSubscription: Boolean,
    val timestampMillis: Long,
    val status: String,
    val paymentMethod: String
)
