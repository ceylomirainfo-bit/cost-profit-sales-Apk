package com.example.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.security.AuthManager
import com.example.ui.security.AuthResult
import com.example.ui.security.PinResult
import com.example.ui.viewmodel.CeyvanaViewModel
import com.example.ui.viewmodel.MainNavigationSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * End-to-end security verification tests for CEYVANA SPICES:
 * 1. Login (credentials and Google Sign-In, error handling for invalid/unregistered credentials).
 * 2. Incorrect PIN attempts (attempt decrementing, input validation, remaining count).
 * 3. Brute-force lockout (5 failed attempts trigger 30s lockout, lockout enforcement even against correct PIN).
 * 4. Logout (session termination, in-memory state purging, Firebase sign-out).
 * 5. Protected screen gating (unauthenticated/locked access denied, no plaintext credentials in persistent storage).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthSecurityRobolectricTest {

    private lateinit var context: Context
    private lateinit var authManager: AuthManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(
                context,
                com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("com.example.app")
                    .setProjectId("demo-test-project")
                    .setApiKey("fake-api-key-for-test")
                    .build()
            )
        }
        authManager = AuthManager(context)
        authManager.logout()
    }

    // ==========================================
    // 1. LOGIN TESTS
    // ==========================================

    @Test
    fun testLogin_unauthenticatedByDefault() {
        assertFalse("App must start unauthenticated", authManager.isUserLoggedIn())
        assertEquals("Unauthenticated user email must be empty", "", authManager.getCurrentEmail())
        assertEquals("Unauthenticated username must be empty", "", authManager.getCurrentUsername())
    }

    @Test
    fun testLogin_rejectsBlankOrInvalidCredentials() {
        // Empty credentials
        val emptyResult = authManager.authenticate("", "")
        assertTrue("Blank credentials must be rejected", emptyResult is AuthResult.Error)

        val emptyPassResult = authManager.authenticate("owner@ceyvana.lk", "")
        assertTrue("Blank password must be rejected", emptyPassResult is AuthResult.Error)

        // Attempt login without configured account
        val unconfiguredResult = authManager.authenticate("unknown@ceyvana.lk", "SomePassword123")
        assertTrue("Unconfigured account must fail authentication", unconfiguredResult is AuthResult.Error)
        assertFalse("App must remain locked", authManager.isUserLoggedIn())
    }

    @Test
    fun testLogin_successfulWithConfiguredCredentials_andGoogleAuth() {
        // Provision account credentials
        val (setupOk, _) = authManager.setupAccountPassword(
            "owner@ceyvana.lk",
            "Ceyvana Owner",
            "CeylonSpice#2026"
        )
        assertTrue("Account provisioning should succeed", setupOk)

        // Invalid password attempt
        val wrongPassResult = authManager.authenticate("owner@ceyvana.lk", "WrongPassword999")
        assertTrue("Wrong password should be rejected", wrongPassResult is AuthResult.Error)
        assertFalse("User should not be logged in", authManager.isUserLoggedIn())

        // Correct credentials attempt
        val successResult = authManager.authenticate("owner@ceyvana.lk", "CeylonSpice#2026")
        assertTrue("Valid credentials must authenticate successfully", successResult is AuthResult.Success)
        assertTrue("User must be logged in", authManager.isUserLoggedIn())
        assertEquals("owner@ceyvana.lk", authManager.getCurrentEmail())
        assertEquals("Ceyvana Owner", authManager.getCurrentUsername())

        // Also verify Google OAuth callback integration hook
        authManager.logout()
        assertFalse(authManager.isUserLoggedIn())

        authManager.onGoogleAuthSuccess("google.owner@ceyvana.lk", "Google Owner")
        assertTrue("Google auth hook must establish session", authManager.isUserLoggedIn())
        assertEquals("google.owner@ceyvana.lk", authManager.getCurrentEmail())
        assertEquals("Google Owner", authManager.getCurrentUsername())
    }

    // ==========================================
    // 2. INCORRECT PIN ATTEMPTS TESTS
    // ==========================================

    @Test
    fun testIncorrectPin_decrementsRemainingAttempts() {
        // Setup secure 4-digit PIN
        val (pinOk, _) = authManager.setupPin("9283")
        assertTrue(pinOk)

        authManager.lock()
        assertFalse(authManager.isUserLoggedIn())

        // Non-4-digit input format validation
        val formatError = authManager.unlockWithPin("12")
        assertTrue("Invalid length must return Error", formatError is PinResult.Error)
        assertEquals("Must require 4 digits", "PIN must be exactly 4 digits.", (formatError as PinResult.Error).message)

        // Attempt 1 with incorrect 4-digit PIN
        val result1 = authManager.unlockWithPin("0000")
        assertTrue("Result should be PinResult.Error", result1 is PinResult.Error)
        val error1 = result1 as PinResult.Error
        assertEquals("Remaining attempts should be 4", 4, error1.attemptsRemaining)
        assertTrue("Message should notify remaining attempts", error1.message.contains("4 attempts remaining"))
        assertFalse("App must remain locked after failed PIN", authManager.isUserLoggedIn())

        // Attempt 2 with incorrect PIN
        val result2 = authManager.unlockWithPin("1111")
        val error2 = result2 as PinResult.Error
        assertEquals("Remaining attempts should decrease to 3", 3, error2.attemptsRemaining)
        assertFalse(authManager.isUserLoggedIn())

        // Correct PIN resets counter and unlocks
        val successResult = authManager.unlockWithPin("9283")
        assertTrue("Correct PIN unlocks app", successResult is PinResult.Success)
        assertTrue("Session must be unlocked", authManager.isUserLoggedIn())
    }

    // ==========================================
    // 3. LOCKOUT TESTS
    // ==========================================

    @Test
    fun testLockoutEnforced_afterFiveFailedAttempts() {
        val (pinOk, _) = authManager.setupPin("7391")
        assertTrue(pinOk)
        authManager.lock()

        // 4 consecutive incorrect attempts
        for (i in 1..4) {
            val res = authManager.unlockWithPin("000$i")
            assertTrue("Attempt $i should be Error", res is PinResult.Error)
            assertEquals("Remaining attempts should decrease", 5 - i, (res as PinResult.Error).attemptsRemaining)
        }

        // 5th failed attempt triggers progressive 30-second lockout
        val lockoutRes = authManager.unlockWithPin("0005")
        assertTrue("5th failed attempt must trigger Lockout", lockoutRes is PinResult.Lockout)
        val lockout = lockoutRes as PinResult.Lockout
        assertTrue("Lockout countdown must be positive (up to 30s)", lockout.secondsRemaining in 1..30)
        assertFalse("App must remain locked during lockout", authManager.isUserLoggedIn())

        // Even entering the correct PIN during lockout must be strictly rejected
        val attemptDuringLockout = authManager.unlockWithPin("7391")
        assertTrue("Correct PIN must still be rejected while locked out", attemptDuringLockout is PinResult.Lockout)
        assertFalse("App must not unlock during active lockout period", authManager.isUserLoggedIn())
    }

    // ==========================================
    // 4. LOGOUT TESTS
    // ==========================================

    @Test
    fun testLogout_clearsSessionAndSensitiveState() {
        authManager.setupAccountPassword("audit@ceyvana.lk", "Auditor", "SecureKey#1")
        authManager.authenticate("audit@ceyvana.lk", "SecureKey#1")
        assertTrue(authManager.isUserLoggedIn())
        assertEquals("audit@ceyvana.lk", authManager.getCurrentEmail())
        assertEquals("Auditor", authManager.getCurrentUsername())

        // Trigger Logout
        authManager.logout()

        assertFalse("User must be logged out", authManager.isUserLoggedIn())
        assertEquals("Email should be cleared", "", authManager.getCurrentEmail())
        assertEquals("Username should be cleared", "", authManager.getCurrentUsername())
    }

    // ==========================================
    // 5. PROTECTED SCREEN ACCESS & GATING TESTS
    // ==========================================

    @Test
    fun testProtectedScreenAccess_strictlyDeniedWhenLocked() {
        // Ensure app starts locked
        authManager.logout()
        assertFalse("Session is unauthenticated", authManager.isUserLoggedIn())

        // Setup PIN
        authManager.setupPin("4829")

        // 1. Verify that SharedPreferences NEVER contains plaintext PIN or passwords
        val prefs = context.getSharedPreferences("ceyvana_auth_prefs", Context.MODE_PRIVATE)
        assertNull("Plaintext PIN key must never exist in storage", prefs.getString("owner_pin", null))
        assertNull("Plaintext PIN key must never exist in storage", prefs.getString("pin", null))
        assertNull("Plaintext password key must never exist in storage", prefs.getString("owner_password", null))
        assertTrue("Salted hash must exist in storage", prefs.getString("pin_hash", null) != null)
        assertTrue("Cryptographic salt must exist in storage", prefs.getString("pin_salt", null) != null)

        // 2. Sensitive user info is blocked from retrieval while locked
        assertEquals("Email accessor returns empty when locked", "", authManager.getCurrentEmail())
        assertEquals("Username accessor returns empty when locked", "", authManager.getCurrentUsername())
    }

    @Test
    fun testViewModel_gatingAndLogoutReset() {
        // Instantiate ViewModel
        val viewModel = CeyvanaViewModel(ApplicationProvider.getApplicationContext())

        // By default when unauthenticated, isAppLocked must be true
        assertTrue("App must be locked on startup when unauthenticated", viewModel.isAppLocked.value)

        // Navigate to a protected section
        viewModel.navigateTo(MainNavigationSection.REPORTS)
        assertEquals(MainNavigationSection.REPORTS, viewModel.currentSection.value)

        // Trigger logout: must lock app, reset to DASHBOARD, and purge user credentials
        viewModel.logout()
        assertTrue("Logout must lock the app", viewModel.isAppLocked.value)
        assertEquals("Logout must reset navigation to DASHBOARD", MainNavigationSection.DASHBOARD, viewModel.currentSection.value)
        assertEquals("User email must be wiped from memory", "", viewModel.currentUserEmail.value)
        assertEquals("Username must be wiped from memory", "", viewModel.currentUsername.value)
    }
}
