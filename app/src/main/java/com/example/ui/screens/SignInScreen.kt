package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SignInScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignInWithGoogle: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onViewPolicy: () -> Unit
) {
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
            .padding(24.dp)
            .testTag("sign_in_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sacred Jyotish Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SacredSaffron.copy(alpha = 0.35f),
                                MysticGold.copy(alpha = 0.15f),
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
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Jyotish AI",
                color = MysticGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Vedic Kundli & AI Astrologer Devi Priya",
                color = TextSilver,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Value Proposition Highlights
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureHighlightRow(
                        icon = Icons.Default.AutoFixHigh,
                        title = "Personalized Kundli & Horoscopes",
                        description = "Vedic planetary positions, Mahadasha, and Kundli Milan"
                    )
                    FeatureHighlightRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Devi Priya Consultations",
                        description = "Personalized astrological guidance via Vedic AI chat consultation"
                    )
                    FeatureHighlightRow(
                        icon = Icons.Default.CloudDone,
                        title = "Secure Cloud Sync",
                        description = "Your birth charts and consultation history backed up securely"
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Error banner if any
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrimsonAstro.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CrimsonAstro.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("sign_in_error_card")
                ) {
                    Text(
                        text = errorMessage,
                        color = CrimsonAstro,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }

            // Interactive Google Sign-In Button
            Button(
                onClick = onSignInWithGoogle,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MysticGold,
                    contentColor = DeepCosmic,
                    disabledContainerColor = MysticGold.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DeepCosmic,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Connecting to Google...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCosmic
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = DeepCosmic
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCosmic
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Guest / Emulator Test Bypass
            OutlinedButton(
                onClick = onContinueAsGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("continue_as_guest_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MysticGold
                ),
                border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MysticGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continue as Guest (Explore Features)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MysticGold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Testing in emulator or didn't receive code? Tap 'Continue as Guest' to bypass 2FA and explore Kundli & Devi Priya immediately.",
                color = TextSilver.copy(alpha = 0.85f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Trust & Privacy Note
            TextButton(
                onClick = onViewPolicy,
                modifier = Modifier.testTag("sign_in_privacy_policy_button")
            ) {
                Text(
                    text = "By signing in, you agree to our Terms of Service & Privacy Policy",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SacredSaffron.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MysticGold,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = TextGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = TextSilver,
                fontSize = 11.sp
            )
        }
    }
}
