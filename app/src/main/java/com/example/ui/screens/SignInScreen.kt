package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SignInScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignInWithGoogle: () -> Unit,
    onSignInWithEmail: (email: String, password: String) -> Unit = { _, _ -> },
    onSignUpWithEmail: (email: String, password: String, displayName: String) -> Unit = { _, _, _ -> },
    onForgotPassword: (email: String) -> Unit = {},
    onContinueAsGuest: () -> Unit,
    onViewPolicy: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // 0: Log In (Existing Account), 1: Sign Up (Create New Account)
    var selectedAuthTab by remember { mutableIntStateOf(0) }

    // Form states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localValidationMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordFeedback by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepCosmic,
                        DarkCosmicSurface,
                        Color(0xFF0F0C29)
                    )
                )
            )
            .padding(horizontal = 20.dp)
            .testTag("sign_in_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(scrollState)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sacred Jyotish Header Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SacredSaffron.copy(alpha = 0.45f),
                                MysticGold.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Vedic Astrology Icon",
                    tint = MysticGold,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Jyotish AI",
                color = MysticGold,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Vedic Kundli & AI Astrologer Devi Priya",
                color = TextSilver,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Mode Selector: Log In vs Sign Up
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCosmicSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedAuthTab,
                    containerColor = Color.Transparent,
                    contentColor = MysticGold,
                    indicator = { tabPositions ->
                        // Standard Material 3 indicator
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedAuthTab]),
                            color = SacredSaffron
                        )
                    }
                ) {
                    Tab(
                        selected = selectedAuthTab == 0,
                        onClick = {
                            selectedAuthTab = 0
                            localValidationMessage = null
                        },
                        text = {
                            Text(
                                "Log In",
                                fontSize = 14.sp,
                                fontWeight = if (selectedAuthTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAuthTab == 0) MysticGold else TextSilver
                            )
                        }
                    )
                    Tab(
                        selected = selectedAuthTab == 1,
                        onClick = {
                            selectedAuthTab = 1
                            localValidationMessage = null
                        },
                        text = {
                            Text(
                                "Create Account",
                                fontSize = 14.sp,
                                fontWeight = if (selectedAuthTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAuthTab == 1) MysticGold else TextSilver
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sign Up: Display Name field
                    if (selectedAuthTab == 1) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                localValidationMessage = null
                            },
                            label = { Text("Your Name", color = TextSilver, fontSize = 13.sp) },
                            placeholder = { Text("e.g. Rahul Sharma", color = TextMuted, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MysticGold, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MysticGold,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextGold,
                                unfocusedTextColor = TextSilver,
                                focusedContainerColor = DarkCosmicSurface,
                                unfocusedContainerColor = DarkCosmicSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localValidationMessage = null
                        },
                        label = { Text("Email Address", color = TextSilver, fontSize = 13.sp) },
                        placeholder = { Text("name@example.com", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MysticGold, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MysticGold,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextGold,
                            unfocusedTextColor = TextSilver,
                            focusedContainerColor = DarkCosmicSurface,
                            unfocusedContainerColor = DarkCosmicSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localValidationMessage = null
                        },
                        label = {
                            Text(
                                if (selectedAuthTab == 1) "Password (min. 6 characters)" else "Password",
                                color = TextSilver,
                                fontSize = 13.sp
                            )
                        },
                        placeholder = { Text("••••••••", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MysticGold, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TextSilver,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                val cleanEmail = email.trim()
                                val cleanPass = password.trim()
                                if (selectedAuthTab == 0) {
                                    if (cleanEmail.isBlank() || cleanPass.isBlank()) {
                                        localValidationMessage = "Please enter both email and password."
                                    } else {
                                        onSignInWithEmail(cleanEmail, cleanPass)
                                    }
                                } else {
                                    if (cleanEmail.isBlank() || cleanPass.length < 6) {
                                        localValidationMessage = "Please enter a valid email and a password of at least 6 characters."
                                    } else {
                                        onSignUpWithEmail(cleanEmail, cleanPass, displayName.trim())
                                    }
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MysticGold,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextGold,
                            unfocusedTextColor = TextSilver,
                            focusedContainerColor = DarkCosmicSurface,
                            unfocusedContainerColor = DarkCosmicSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Forgot Password Link for Log In mode
                    if (selectedAuthTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = MysticGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        forgotPasswordEmail = email.trim()
                                        forgotPasswordFeedback = null
                                        showForgotPasswordDialog = true
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Validation or Backend Error Notice
                    val displayError = localValidationMessage ?: errorMessage
                    if (!displayError.isNullOrBlank()) {
                        Surface(
                            color = CrimsonAstro.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CrimsonAstro.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_in_error_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CrimsonAstro, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayError,
                                    color = CrimsonAstro,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Main Submit Button (Log In or Create Account)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val cleanEmail = email.trim()
                            val cleanPass = password.trim()
                            if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                localValidationMessage = "Please enter a valid email address."
                                return@Button
                            }
                            if (cleanPass.length < 6) {
                                localValidationMessage = "Password must be at least 6 characters."
                                return@Button
                            }
                            if (selectedAuthTab == 0) {
                                onSignInWithEmail(cleanEmail, cleanPass)
                            } else {
                                onSignUpWithEmail(cleanEmail, cleanPass, displayName.trim())
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SacredSaffron,
                            contentColor = DeepCosmic,
                            disabledContainerColor = SacredSaffron.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag(if (selectedAuthTab == 0) "email_login_button" else "email_signup_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = DeepCosmic,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (selectedAuthTab == 0) "Logging In..." else "Creating Account...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = if (selectedAuthTab == 0) "Log In to Jyotish AI" else "Create Free Account",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider: OR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                Text(
                    text = "  OR  ",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Google Sign-In Button
            OutlinedButton(
                onClick = onSignInWithGoogle,
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MysticGold
                ),
                border = BorderStroke(1.dp, BorderGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("google_sign_in_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MysticGold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue with Google",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MysticGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Guest / Quick Explore Bypass
            TextButton(
                onClick = onContinueAsGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continue_as_guest_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TextSilver
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Explore First as Guest (No Password Required)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSilver
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trust & Privacy Note
            TextButton(
                onClick = onViewPolicy,
                modifier = Modifier.testTag("sign_in_privacy_policy_button")
            ) {
                Text(
                    text = "By continuing, you agree to our Terms of Service & Privacy Policy",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text("Reset Password", color = MysticGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter your account email to receive a secure password reset link.",
                        color = TextSilver,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        placeholder = { Text("name@example.com", color = TextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MysticGold,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextGold,
                            unfocusedTextColor = TextSilver
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (forgotPasswordFeedback != null) {
                        Text(
                            text = forgotPasswordFeedback ?: "",
                            color = if (forgotPasswordFeedback?.contains("sent", ignoreCase = true) == true) Color(0xFF10B981) else CrimsonAstro,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = forgotPasswordEmail.trim()
                        if (clean.isBlank()) {
                            forgotPasswordFeedback = "Please enter an email address."
                        } else {
                            onForgotPassword(clean)
                            forgotPasswordFeedback = "Password reset instructions sent to $clean."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SacredSaffron, contentColor = DeepCosmic)
                ) {
                    Text("Send Reset Link", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close", color = TextSilver)
                }
            },
            containerColor = DarkCosmicSurface
        )
    }
}
