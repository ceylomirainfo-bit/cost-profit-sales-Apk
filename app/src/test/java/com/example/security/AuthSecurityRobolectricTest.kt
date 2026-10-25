package com.example.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.security.AuthManager
import com.example.ui.security.AuthResult
import com.example.ui.security.PinResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthSecurityRobolectricTest {

    private lateinit var context: Context
    private lateinit var authManager: AuthManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        authManager = AuthManager(context)
        authManager.logout()
    }

    @Test
    fun testSuccessfulAuthentication_establishesSession() {
        // Given fresh unauthenticated state
        assertFalse("App must start unauthenticated", authManager.isUserLoggedIn())

        // 1. Test local credential provisioning and authentication
        val (setupOk, _) = authManager.setupAccountPassword(
            "owner@ceyvana.lk",
            "Ceyvana Owner",
            "CeylonSpice#2026"
        )
        assertTrue("Account setup should succeed", setupOk)

        val authResult = authManager.authenticate("owner@ceyvana.lk", "CeylonSpice#2026")
        assertTrue("Authentication should succeed with valid credentials", authResult is AuthResult.Success)
        assertTrue("User must be marked as logged in", authManager.isUserLoggedIn())
        assertEquals("owner@ceyvana.lk", authManager.getCurrentEmail())
        assertEquals("Ceyvana Owner", authManager.getCurrentUsername())

        // 2. Test Google Sign-In integration hook
        authManager.logout()
        assertFalse(authManager.isUserLoggedIn())

        authManager.onGoogleAuthSuccess("google.owner@ceyvana.lk", "Google Owner")
        assertTrue("Session should be established after Google auth", authManager.isUserLoggedIn())
        assertEquals("google.owner@ceyvana.lk", authManager.getCurrentEmail())
        assertEquals("Google Owner", authManager.getCurrentUsername())
    }

    @Test
    fun testIncorrectPin_decrementsRemainingAttempts() {
        // Setup a secure 4-digit PIN
        val (pinOk, _) = authManager.setupPin("9283")
        assertTrue(pinOk)

        // Lock session
        authManager.lock()
        assertFalse(authManager.isUserLoggedIn())

        // Attempt with wrong PIN
        val result = authManager.unlockWithPin("0000")
        assertTrue("Result should be PinResult.Error", result is PinResult.Error)
        val errorResult = result as PinResult.Error
        assertEquals("Remaining attempts should be 4", 4, errorResult.attemptsRemaining)
        assertTrue("Message should notify remaining attempts", errorResult.message.contains("4 attempts remaining"))
        assertFalse("App must remain locked after failed PIN", authManager.isUserLoggedIn())

        // Ensure correct PIN works
        val successResult = authManager.unlockWithPin("9283")
        assertTrue("Correct PIN unlocks app", successResult is PinResult.Success)
        assertTrue(authManager.isUserLoggedIn())
    }

    @Test
    fun testLockoutEnforced_afterFiveFailedAttempts() {
        val (pinOk, _) = authManager.setupPin("7391")
        assertTrue(pinOk)
        authManager.lock()

        // 5 consecutive incorrect attempts
        for (i in 1..4) {
            val res = authManager.unlockWithPin("000$i")
            assertTrue("Attempt $i should be Error", res is PinResult.Error)
            assertEquals("Remaining attempts should decrease", 5 - i, (res as PinResult.Error).attemptsRemaining)
        }

        // 5th failed attempt triggers lockout
        val lockoutRes = authManager.unlockWithPin("0005")
        assertTrue("5th failed attempt must trigger Lockout", lockoutRes is PinResult.Lockout)
        val lockout = lockoutRes as PinResult.Lockout
        assertTrue("Seconds remaining should be positive (up to 30)", lockout.secondsRemaining in 1..30)
        assertFalse("App must remain locked during lockout", authManager.isUserLoggedIn())

        // Even entering the correct PIN during lockout must be rejected
        val attemptDuringLockout = authManager.unlockWithPin("7391")
        assertTrue("Correct PIN must still be rejected while locked out", attemptDuringLockout is PinResult.Lockout)
        assertFalse("App must not unlock during active lockout period", authManager.isUserLoggedIn())
    }

    @Test
    fun testLogout_clearsSessionAndSensitiveState() {
        authManager.setupAccountPassword("audit@ceyvana.lk", "Auditor", "SecureKey#1")
        authManager.authenticate("audit@ceyvana.lk", "SecureKey#1")
        assertTrue(authManager.isUserLoggedIn())
        assertEquals("audit@ceyvana.lk", authManager.getCurrentEmail())
        assertEquals("Auditor", authManager.getCurrentUsername())

        // Logout
        authManager.logout()

        assertFalse("User must be logged out", authManager.isUserLoggedIn())
        assertEquals("Email should be cleared", "", authManager.getCurrentEmail())
        assertEquals("Username should be cleared", "", authManager.getCurrentUsername())
    }

    @Test
    fun testProtectedScreenAccess_deniedWhenLocked_andNoPlaintextPinStored() {
        // App is locked by default
        assertFalse("App must deny access when locked", authManager.isUserLoggedIn())

        // Setup PIN and verify that NO plaintext PIN is stored in SharedPreferences
        authManager.setupPin("4829")

        val prefs = context.getSharedPreferences("ceyvana_auth_prefs", Context.MODE_PRIVATE)
        assertNull("Plaintext PIN key must never exist in storage", prefs.getString("owner_pin", null))
        assertNull("Plaintext PIN key must never exist in storage", prefs.getString("pin", null))
        assertTrue("Salted hash must exist in storage", prefs.getString("pin_hash", null) != null)
        assertTrue("Cryptographic salt must exist in storage", prefs.getString("pin_salt", null) != null)

        // Ensure legacy plaintext keys are purged
        assertNull(prefs.getString("owner_pin", null))
        assertNull(prefs.getString("owner_password", null))
    }
}
