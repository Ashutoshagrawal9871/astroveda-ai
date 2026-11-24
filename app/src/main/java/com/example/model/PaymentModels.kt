package com.example.model

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

/**
 * Verified Google Play transaction and purchase record for display in Transaction History.
 * Backed by Google Play Billing and server-side verification ledger.
 */
data class PaymentLogRecord(
    val transactionId: String,
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val packageId: String,
    val packageName: String,
    val amountInr: Int = 0,
    val paymentMethod: String = "Google Play",
    val utrNumber: String = "",
    val status: String = "VERIFIED_ACTIVE",
    val timestamp: Long = System.currentTimeMillis()
)

data class PaymentVerificationResult(
    val success: Boolean,
    val status: String, // "ACTIVATED", "PENDING_VERIFICATION", "REJECTED", "ERROR"
    val isActivated: Boolean,
    val transactionId: String = "",
    val message: String = ""
)

object PaymentUiUtils {
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
