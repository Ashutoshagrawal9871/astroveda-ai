package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Production-grade Firebase Authentication Manager with Google Sign-In.
 * Authenticates users via Jetpack Credential Manager and secures access
 * to personalized Kundli and astrology features.
 */
object AuthManager {
    private const val TAG = "AuthManager"
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    fun continueAsGuest() {
        _isGuestMode.value = true
    }

    fun exitGuestMode() {
        _isGuestMode.value = false
    }

    fun initialize(context: Context) {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
        _currentUser.value = auth.currentUser
    }

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun requireUserId(): String = auth.currentUser?.uid ?: error("User not authenticated")

    val isAuthenticated: Boolean
        get() = auth.currentUser != null

    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> = withContext(Dispatchers.Main) {
        try {
            _authError.value = null
            val serverClientId = activity.getString(R.string.default_web_client_id)

            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                _currentUser.value = user
                if (user != null) {
                    Log.i(TAG, "Google Sign-In successful. User: ${user.email} (${user.uid})")
                    Result.success(user)
                } else {
                    val errorMsg = "Firebase authentication returned null user"
                    _authError.value = errorMsg
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Unexpected credential type returned"
                _authError.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In: ${e.message}")
            _authError.value = null
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            _authError.value = e.localizedMessage ?: "Sign-in failed. Please try again."
            Result.failure(e)
        }
    }

    suspend fun getValidIdToken(forceRefresh: Boolean = false): String? = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext null
            val tokenResult = user.getIdToken(forceRefresh).await()
            tokenResult.token
        } catch (e: Exception) {
            Log.w(TAG, "Unable to retrieve Firebase ID token: ${e.message}")
            null
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _isGuestMode.value = false
    }

    fun clearError() {
        _authError.value = null
    }
}
