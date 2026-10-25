package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GoogleSignInButton
import com.example.ui.components.attemptAutoSignIn
import com.example.ui.security.AuthResult
import com.example.ui.security.PinResult
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DarkCharcoalTextSecondary
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LockDarkGreenAccent
import com.example.ui.theme.LockForestGreen
import com.example.ui.theme.LockMutedGold
import com.example.ui.theme.LockSoftGray
import com.example.ui.theme.LockWarmIvory
import com.example.ui.theme.LockWhite
import com.example.ui.theme.MutedGoldSecondary
import com.example.ui.viewmodel.CeyvanaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun LockScreen(
    viewModel: CeyvanaViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }

    // Silent background auto-sign-in attempt on startup if Google session exists
    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { email, name ->
                viewModel.onGoogleSignInSuccess(email, name)
            },
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    // Form inputs & states (empty by default - never hardcode production credentials)
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }

    // Error states
    var identifierError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    // Auth Mode: Password form vs Quick PIN Keypad
    var isPinMode by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }

    // Dialogs
    var showSecurityInfoDialog by remember { mutableStateOf(false) }
    var showChangeCredentialsDialog by remember { mutableStateOf(false) }

    fun triggerShake() {
        coroutineScope.launch {
            repeat(3) {
                shakeOffset.animateTo(-22f, tween(50))
                shakeOffset.animateTo(22f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    fun handleAuthenticate() {
        focusManager.clearFocus()
        if (isAuthenticating) return

        identifierError = null
        passwordError = null
        authErrorMessage = null

        val trimmedId = identifier.trim()
        val trimmedPass = password.trim()

        var hasError = false
        if (trimmedId.isEmpty()) {
            identifierError = "Email or username is required."
            hasError = true
        } else if (trimmedId.contains("@") && !trimmedId.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"))) {
            identifierError = "Please enter a valid email address."
            hasError = true
        }

        if (trimmedPass.isEmpty()) {
            passwordError = "Password is required."
            hasError = true
        } else if (trimmedPass.length < 6) {
            passwordError = "Password must be at least 6 characters."
            hasError = true
        }

        if (hasError) {
            triggerShake()
            return
        }

        isAuthenticating = true
        coroutineScope.launch {
            // High-security verification with realistic feedback
            delay(400)
            val result = viewModel.authenticateWithCredentials(trimmedId, trimmedPass)
            isAuthenticating = false
            when (result) {
                is AuthResult.Success -> {
                    authErrorMessage = null
                }
                is AuthResult.Error -> {
                    authErrorMessage = result.message
                    triggerShake()
                }
                is AuthResult.Lockout -> {
                    authErrorMessage = "Security lockout: too many failed attempts. Try again in ${result.secondsRemaining}s."
                    triggerShake()
                }
            }
        }
    }

    fun handlePinKeyPress(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            isPinError = false

            if (newPin.length == 4) {
                coroutineScope.launch {
                    delay(150)
                    if (!viewModel.hasConfiguredPin()) {
                        val (ok, msg) = viewModel.setupPin(newPin)
                        if (ok) {
                            authErrorMessage = null
                            viewModel.unlockWithPin(newPin)
                            enteredPin = ""
                        } else {
                            isPinError = true
                            authErrorMessage = msg
                            triggerShake()
                            delay(600)
                            enteredPin = ""
                            isPinError = false
                        }
                    } else {
                        val result = viewModel.unlockWithPin(newPin)
                        when (result) {
                            is PinResult.Success -> {
                                authErrorMessage = null
                                enteredPin = ""
                            }
                            is PinResult.Error -> {
                                isPinError = true
                                authErrorMessage = "${result.message} (${result.attemptsRemaining} attempts remaining)"
                                triggerShake()
                                delay(600)
                                enteredPin = ""
                                isPinError = false
                            }
                            is PinResult.Lockout -> {
                                isPinError = true
                                authErrorMessage = "Security lockout: too many failed PIN attempts. Try again in ${result.secondsRemaining}s or sign in with Google."
                                triggerShake()
                                delay(600)
                                enteredPin = ""
                                isPinError = false
                            }
                            is PinResult.NotConfigured -> {
                                authErrorMessage = "Security PIN has not been set yet."
                                enteredPin = ""
                            }
                        }
                    }
                }
            }
        }
    }

    fun handlePinBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isPinError = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LockForestGreen)
    ) {
        val isDesktopOrTablet = maxWidth >= 760.dp

        if (isDesktopOrTablet) {
            // ==========================================
            // DESKTOP / TABLET TWO-PANEL BALANCED LAYOUT
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // LEFT PANEL - BRANDING & BOTANICAL ILLUSTRATION
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    LockForestGreen,
                                    LockDarkGreenAccent
                                )
                            )
                        )
                        .padding(horizontal = 48.dp, vertical = 40.dp)
                ) {
                    // Subtle background decorative border
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 1.dp,
                                color = LockMutedGold.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top: Ceyvana Spices Wordmark & Star Anise Crest
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ceyvana_brand_logo),
                                        contentDescription = "Ceyvana Brand Logo",
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, LockMutedGold, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "CEYVANA SPICES",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 2.5.sp,
                                            color = LockWhite,
                                            fontFamily = FontFamily.Serif
                                        )
                                        Text(
                                            text = "CEYLON HERITAGE • SINCE 2026",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 1.8.sp,
                                            color = LockMutedGold
                                        )
                                    }
                                }
                            }

                            // Center: Tasteful botanical spice illustration
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.5.dp, LockMutedGold.copy(alpha = 0.6f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = LockDarkGreenAccent),
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(280.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_spice_botanical),
                                            contentDescription = "Botanical illustration of Ceylon spices, cinnamon, tea leaves, and peppercorns",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        // Subtle golden vignette overlay
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            LockDarkGreenAccent.copy(alpha = 0.5f)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                }
                            }

                            // Bottom: Tagline and supporting line
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "The Essence of Ceylon, Managed with Care.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = LockWarmIvory,
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily.Serif
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Your daily business, in one place.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = LockWarmIvory.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = LockMutedGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Executive Access Security • Protected Data",
                                        fontSize = 11.sp,
                                        color = LockMutedGold
                                    )
                                }
                            }
                        }
                    }
                }

                // RIGHT PANEL - WARM IVORY AUTHENTICATION CARD
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(LockForestGreen)
                        .padding(horizontal = 48.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = LockWarmIvory),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                            .shadow(20.dp, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 36.dp, vertical = 32.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Header
                            Text(
                                text = "Welcome Back",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = LockForestGreen,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sign in to access your Ceyvana Spices dashboard.",
                                fontSize = 13.sp,
                                color = DarkCharcoalTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Authentication Form or PIN View
                            if (!isPinMode) {
                                // EMAIL / USERNAME INPUT
                                OutlinedTextField(
                                    value = identifier,
                                    onValueChange = {
                                        identifier = it
                                        identifierError = null
                                        authErrorMessage = null
                                    },
                                    label = { Text("Business Email or Username") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = if (identifierError != null) Color(0xFFC53030) else LockMutedGold
                                        )
                                    },
                                    isError = identifierError != null,
                                    supportingText = {
                                        if (identifierError != null) {
                                            Text(
                                                text = identifierError ?: "",
                                                color = Color(0xFFC53030),
                                                fontSize = 11.sp
                                            )
                                        }
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
                                        focusedBorderColor = LockForestGreen,
                                        unfocusedBorderColor = LockSoftGray.copy(alpha = 0.4f),
                                        errorBorderColor = Color(0xFFC53030),
                                        focusedLabelColor = LockForestGreen,
                                        unfocusedLabelColor = DarkCharcoalTextSecondary,
                                        focusedTextColor = DarkCharcoalText,
                                        unfocusedTextColor = DarkCharcoalText
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_identifier_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // PASSWORD INPUT WITH SHOW/HIDE TOGGLE
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        passwordError = null
                                        authErrorMessage = null
                                    },
                                    label = { Text("Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (passwordError != null) Color(0xFFC53030) else LockMutedGold
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { passwordVisible = !passwordVisible },
                                            modifier = Modifier.testTag("toggle_password_visibility")
                                        ) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                                tint = DarkCharcoalTextSecondary
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    isError = passwordError != null,
                                    supportingText = {
                                        if (passwordError != null) {
                                            Text(
                                                text = passwordError ?: "",
                                                color = Color(0xFFC53030),
                                                fontSize = 11.sp
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { handleAuthenticate() }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LockForestGreen,
                                        unfocusedBorderColor = LockSoftGray.copy(alpha = 0.4f),
                                        errorBorderColor = Color(0xFFC53030),
                                        focusedLabelColor = LockForestGreen,
                                        unfocusedLabelColor = DarkCharcoalTextSecondary,
                                        focusedTextColor = DarkCharcoalText,
                                        unfocusedTextColor = DarkCharcoalText
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_password_input")
                                )

                                // Global Authentication Error Banner
                                if (authErrorMessage != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFFF5F5),
                                        border = BorderStroke(1.dp, Color(0xFFFEB2B2)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = Color(0xFFC53030),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = authErrorMessage ?: "",
                                                color = Color(0xFF9B2C2C),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // PRIMARY BUTTON: "Unlock Dashboard"
                                Button(
                                    onClick = { handleAuthenticate() },
                                    enabled = !isAuthenticating,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LockForestGreen,
                                        contentColor = LockWhite
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("unlock_dashboard_button")
                                ) {
                                    if (isAuthenticating) {
                                        CircularProgressIndicator(
                                            color = LockMutedGold,
                                            strokeWidth = 2.5.dp,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Authenticating...",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.LockOpen,
                                            contentDescription = null,
                                            tint = LockMutedGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Unlock Dashboard",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = LockSoftGray.copy(alpha = 0.35f))
                                    Text(
                                        text = "   OR CLOUD SIGN-IN   ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = DarkCharcoalTextSecondary
                                    )
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = LockSoftGray.copy(alpha = 0.35f))
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                GoogleSignInButton(
                                    onAuthSuccess = { email, name ->
                                        viewModel.onGoogleSignInSuccess(email, name)
                                    },
                                    onAuthError = { err ->
                                        authErrorMessage = err
                                    }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Security Mode Switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            isPinMode = true
                                            authErrorMessage = null
                                        },
                                        modifier = Modifier.testTag("switch_to_pin_mode_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Pin,
                                            contentDescription = null,
                                            tint = LockMutedGold,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (viewModel.hasConfiguredPin()) "Use 4-Digit Security PIN" else "Setup 4-Digit Security PIN",
                                            fontSize = 12.sp,
                                            color = LockForestGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            } else {
                                // 4-DIGIT PIN AUTHENTICATION ALTERNATIVE
                                PinAuthenticationCard(
                                    enteredPin = enteredPin,
                                    isPinError = isPinError,
                                    isConfigured = viewModel.hasConfiguredPin(),
                                    authErrorMessage = authErrorMessage,
                                    onKeyPress = { handlePinKeyPress(it) },
                                    onBackspace = { handlePinBackspace() },
                                    onClear = {
                                        enteredPin = ""
                                        isPinError = false
                                        authErrorMessage = null
                                    },
                                    onBackToPassword = {
                                        isPinMode = false
                                        enteredPin = ""
                                        isPinError = false
                                        authErrorMessage = null
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = LockSoftGray.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Bottom Security Architecture & Cloud Notice
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSecurityInfoDialog = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = LockMutedGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Security Architecture & Cloud Status",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkCharcoalTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // MOBILE COMPACT SINGLE-COLUMN LAYOUT
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header with Ceyvana Logo and Wordmark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ceyvana_brand_logo),
                        contentDescription = "Ceyvana Brand Logo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, LockMutedGold, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CEYVANA SPICES",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = LockWhite,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "BUSINESS MANAGEMENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = LockMutedGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tagline Banner
                Text(
                    text = "The Essence of Ceylon, Managed with Care.",
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = LockWarmIvory,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Authentication Card in Warm Ivory
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LockWarmIvory),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome Back",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = LockForestGreen,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sign in to access your Ceyvana Spices dashboard.",
                            fontSize = 12.sp,
                            color = DarkCharcoalTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        if (!isPinMode) {
                            // Email/Username Input
                            OutlinedTextField(
                                value = identifier,
                                onValueChange = {
                                    identifier = it
                                    identifierError = null
                                    authErrorMessage = null
                                },
                                label = { Text("Email or Username", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = if (identifierError != null) Color(0xFFC53030) else LockMutedGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                isError = identifierError != null,
                                supportingText = {
                                    if (identifierError != null) {
                                        Text(text = identifierError ?: "", color = Color(0xFFC53030), fontSize = 11.sp)
                                    }
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
                                    focusedBorderColor = LockForestGreen,
                                    unfocusedBorderColor = LockSoftGray.copy(alpha = 0.4f),
                                    errorBorderColor = Color(0xFFC53030),
                                    focusedLabelColor = LockForestGreen,
                                    unfocusedLabelColor = DarkCharcoalTextSecondary,
                                    focusedTextColor = DarkCharcoalText,
                                    unfocusedTextColor = DarkCharcoalText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_identifier_input")
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Password Input
                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                    authErrorMessage = null
                                },
                                label = { Text("Password", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (passwordError != null) Color(0xFFC53030) else LockMutedGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        modifier = Modifier.testTag("toggle_password_visibility")
                                    ) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = DarkCharcoalTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = passwordError != null,
                                supportingText = {
                                    if (passwordError != null) {
                                        Text(text = passwordError ?: "", color = Color(0xFFC53030), fontSize = 11.sp)
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { handleAuthenticate() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LockForestGreen,
                                    unfocusedBorderColor = LockSoftGray.copy(alpha = 0.4f),
                                    errorBorderColor = Color(0xFFC53030),
                                    focusedLabelColor = LockForestGreen,
                                    unfocusedLabelColor = DarkCharcoalTextSecondary,
                                    focusedTextColor = DarkCharcoalText,
                                    unfocusedTextColor = DarkCharcoalText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_password_input")
                            )

                            if (authErrorMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF5F5),
                                    border = BorderStroke(1.dp, Color(0xFFFEB2B2)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFC53030),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = authErrorMessage ?: "",
                                            color = Color(0xFF9B2C2C),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // "Unlock Dashboard" Button
                            Button(
                                onClick = { handleAuthenticate() },
                                enabled = !isAuthenticating,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LockForestGreen,
                                    contentColor = LockWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("unlock_dashboard_button")
                            ) {
                                if (isAuthenticating) {
                                    CircularProgressIndicator(
                                        color = LockMutedGold,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Authenticating...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = LockMutedGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Unlock Dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = LockSoftGray.copy(alpha = 0.35f))
                                Text(
                                    text = "   OR CLOUD SIGN-IN   ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = DarkCharcoalTextSecondary
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = LockSoftGray.copy(alpha = 0.35f))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            GoogleSignInButton(
                                onAuthSuccess = { email, name ->
                                    viewModel.onGoogleSignInSuccess(email, name)
                                },
                                onAuthError = { err ->
                                    authErrorMessage = err
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        isPinMode = true
                                        authErrorMessage = null
                                    },
                                    modifier = Modifier.testTag("switch_to_pin_mode_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pin,
                                        contentDescription = null,
                                        tint = LockMutedGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (viewModel.hasConfiguredPin()) "Use 4-Digit Security PIN" else "Setup 4-Digit Security PIN",
                                        fontSize = 11.sp,
                                        color = LockForestGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // PIN Mode on Mobile
                            PinAuthenticationCard(
                                enteredPin = enteredPin,
                                isPinError = isPinError,
                                isConfigured = viewModel.hasConfiguredPin(),
                                authErrorMessage = authErrorMessage,
                                onKeyPress = { handlePinKeyPress(it) },
                                onBackspace = { handlePinBackspace() },
                                onClear = {
                                    enteredPin = ""
                                    isPinError = false
                                    authErrorMessage = null
                                },
                                onBackToPassword = {
                                    isPinMode = false
                                    enteredPin = ""
                                    isPinError = false
                                    authErrorMessage = null
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = LockSoftGray.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = { showSecurityInfoDialog = true },
                            modifier = Modifier.testTag("security_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = LockMutedGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Security & Storage Notice",
                                fontSize = 11.sp,
                                color = DarkCharcoalTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // SECURITY ARCHITECTURE & CLOUD STATUS DIALOG
    // ==========================================
    if (showSecurityInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = LockMutedGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Security & Cloud Architecture",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LockForestGreen,
                        fontFamily = FontFamily.Serif
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Android OS Sandboxed Storage",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Business records, catalog, and sales are persisted in a local Room SQLite database sandboxed to this Android application. Authentication prevents unauthorized terminal operation.",
                                fontSize = 12.sp,
                                color = Color(0xFF14532D)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEFCE8),
                        border = BorderStroke(1.dp, Color(0xFFFEF08A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFFCA8A04),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Multi-User & Cloud Sync Readiness",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF854D0E)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Authentication alone on a shared local device profile does not substitute for cloud-backed multi-device access. The application is structured and pre-configured for seamless connection with Firebase Authentication and Firestore Cloud Database.",
                                fontSize = 12.sp,
                                color = Color(0xFF713F12)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LockWarmIvory,
                        border = BorderStroke(1.dp, LockMutedGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Security & Protection Standards:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LockForestGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Primary Access: Firebase Authentication via Google Identity (OAuth 2.0)\n• Local Terminal Lock: Cryptographically salted SHA-256 hash\n• Plaintext Protection: No passwords or PINs stored in plaintext\n• Anti-Brute Force: 5 failed attempts trigger a progressive lockout",
                                fontSize = 11.sp,
                                color = DarkCharcoalTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSecurityInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LockForestGreen)
                ) {
                    Text("Understood", color = LockWhite)
                }
            }
        )
    }
}

// ==========================================
// PIN AUTHENTICATION COMPONENT (Keypad)
// ==========================================
@Composable
private fun PinAuthenticationCard(
    enteredPin: String,
    isPinError: Boolean,
    isConfigured: Boolean,
    authErrorMessage: String?,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onBackToPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isConfigured) "Enter 4-Digit Security PIN" else "Setup 4-Digit Security PIN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LockForestGreen
            )
            TextButton(onClick = onBackToPassword) {
                Text(
                    text = "Use Password",
                    fontSize = 12.sp,
                    color = LockMutedGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 PIN DOTS
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val filled = i < enteredPin.length
                val dotColor = when {
                    isPinError -> Color(0xFFC53030)
                    filled -> LockForestGreen
                    else -> LockSoftGray.copy(alpha = 0.3f)
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(
                            width = 1.5.dp,
                            color = if (filled) LockMutedGold else Color.Transparent,
                            shape = CircleShape
                        )
                )
            }
        }

        if (authErrorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = authErrorMessage,
                color = if (isPinError) Color(0xFFC53030) else LockForestGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // NUMERIC KEYPAD (1-9, Clear, 0, Backspace)
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                row.forEach { digit ->
                    KeypadCircleButton(
                        text = digit,
                        onClick = { onKeyPress(digit) }
                    )
                }
            }
        }

        // Bottom Row: Clear, 0, Backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            // Clear Key
            Surface(
                shape = CircleShape,
                color = LockSoftGray.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { onClear() }
                    .testTag("pin_clear_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "C",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LockForestGreen
                    )
                }
            }

            KeypadCircleButton(
                text = "0",
                onClick = { onKeyPress("0") }
            )

            // Backspace
            Surface(
                shape = CircleShape,
                color = LockForestGreen.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { onBackspace() }
                    .testTag("pin_backspace_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = LockForestGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (isConfigured) "Salted cryptographic hash verification" else "Create a 4-digit code to lock your local terminal",
            fontSize = 11.sp,
            color = DarkCharcoalTextSecondary
        )
    }
}

@Composable
private fun KeypadCircleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = LockWhite,
        border = BorderStroke(1.dp, LockMutedGold.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("keypad_digit_$text")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LockForestGreen
            )
        }
    }
}
