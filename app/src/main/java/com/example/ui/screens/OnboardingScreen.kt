package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import com.example.ui.components.ChakraEmblem
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CulturalGreeting
import com.example.data.model.LauncherLanguage
import com.example.ui.components.GlassCard
import com.example.ui.viewmodel.LauncherUiState
import com.example.ui.viewmodel.LauncherViewModel
import com.example.util.Localization

@Composable
fun OnboardingScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onRequestDefaultLauncher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = uiState.settings.language
    val isDefault = uiState.isDefaultLauncher
    var currentStep by remember { mutableIntStateOf(0) } // 0: Welcome, 1: Language & Greeting, 2: Default Launcher, 3: Finish

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0D253F)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    IconButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) { idx ->
                        Box(
                            modifier = Modifier
                                .size(width = if (currentStep == idx) 22.dp else 8.dp, height = 6.dp)
                                .clip(CircleShape)
                                .background(if (currentStep >= idx) Color(0xFFE65100) else Color.White.copy(0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.size(36.dp))
            }

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboarding_step"
                ) { step ->
                    when (step) {
                        0 -> OnboardingWelcomeStep(language = language)
                        1 -> OnboardingLanguageGreetingStep(
                            language = language,
                            greeting = uiState.settings.greetingChoice,
                            onSelectLanguage = { viewModel.updateLanguage(it) },
                            onSelectGreeting = { viewModel.updateCulturalGreeting(it) }
                        )
                        2 -> OnboardingDefaultLauncherStep(
                            language = language,
                            isDefault = isDefault,
                            onRequestDefault = onRequestDefaultLauncher
                        )
                        3 -> OnboardingSummaryStep(
                            language = language,
                            greeting = uiState.settings.greetingChoice
                        )
                    }
                }
            }

            // Bottom Navigation Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            viewModel.completeOnboarding()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_primary_button")
                ) {
                    Text(
                        text = if (currentStep == 3) "Finish Setup & Enter Bharat OS" else "Continue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (currentStep == 3) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (currentStep == 0 || currentStep == 2) {
                    Button(
                        onClick = { viewModel.completeOnboarding() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip to Home",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color.White.copy(0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingWelcomeStep(language: LauncherLanguage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        ChakraEmblem(modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = Localization.getText("app_title", language),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Welcome to Bharat OS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFFB74D)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "A functional Android launcher engineered with Indian cultural dignity and modern glassmorphism aesthetic.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun OnboardingLanguageGreetingStep(
    language: LauncherLanguage,
    greeting: CulturalGreeting,
    onSelectLanguage: (LauncherLanguage) -> Unit,
    onSelectGreeting: (CulturalGreeting) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Language & Greeting",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Choose your regional tongue and auspicious home screen greeting.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(0.75f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "REGIONAL LANGUAGE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFB74D)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LauncherLanguage.values().forEach { lang ->
                val isSelected = language == lang
                GlassCard(
                    theme = com.example.data.model.LauncherTheme.DARK,
                    cornerRadius = 14.dp,
                    onClick = { onSelectLanguage(lang) },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = lang.nativeName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFFFFB74D) else Color.White
                        )
                        Text(
                            text = lang.displayName,
                            fontSize = 11.sp,
                            color = Color.White.copy(0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "CULTURAL GREETING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFB74D)
        )
        Spacer(modifier = Modifier.height(8.dp))

        CulturalGreeting.values().forEach { item ->
            val isSelected = greeting == item
            GlassCard(
                theme = com.example.data.model.LauncherTheme.DARK,
                cornerRadius = 14.dp,
                onClick = { onSelectGreeting(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectGreeting(item) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE65100))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.englishText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = item.hindiText,
                            fontSize = 12.sp,
                            color = Color(0xFFFFB74D)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingDefaultLauncherStep(
    language: LauncherLanguage,
    isDefault: Boolean,
    onRequestDefault: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = if (isDefault) Icons.Default.CheckCircle else Icons.Default.Home,
            contentDescription = null,
            tint = if (isDefault) Color(0xFF34D399) else Color(0xFFFFB74D),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isDefault) "Default Launcher Active" else "Set as Default Launcher",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bharat OS replaces your home-screen experience with an Indian-inspired glassmorphism interface. Your operating system, apps, and data remain unchanged and secure.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (!isDefault) {
            Button(
                onClick = onRequestDefault,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("onboarding_role_button")
            ) {
                Text(
                    text = "Authorize Home Role",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        } else {
            GlassCard(
                theme = com.example.data.model.LauncherTheme.DARK,
                cornerRadius = 14.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Bharat OS is ready as your primary home screen.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingSummaryStep(
    language: LauncherLanguage,
    greeting: CulturalGreeting
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        ChakraEmblem(modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tip: Long-press any empty area on your home screen to open the Glass Customization Mode. Swipe up anytime to access all installed apps.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(0.85f),
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
    }
}
