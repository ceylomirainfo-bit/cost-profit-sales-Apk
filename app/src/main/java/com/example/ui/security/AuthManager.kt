package com.example.ui.security

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.security.MessageDigest
import java.security.SecureRandom

sealed class AuthResult {
    data class Success(val email: String, val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class Lockout(val secondsRemaining: Int) : AuthResult()
}

sealed class PinResult {
    object Success : PinResult()
    data class Error(val message: String, val attemptsRemaining: Int) : PinResult()
    data class Lockout(val secondsRemaining: Int) : PinResult()
    object NotConfigured : PinResult()
}

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ceyvana_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD_SALT = "password_salt"
        private const val KEY_PASSWORD_HASH = "password_hash"

        // Cryptographically secure salted PIN storage (NEVER store plaintext)
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_LEGACY_PLAIN_PIN = "owner_pin"

        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL_TIMESTAMP = "lockout_until"

        private const val KEY_FAILED_PIN_ATTEMPTS = "failed_pin_attempts"
        private const val KEY_PIN_LOCKOUT_UNTIL = "pin_lockout_until"

        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds progressive lockout
    }

    init {
        // Migration: Purge any legacy plaintext PIN keys from SharedPreferences
        if (prefs.contains(KEY_LEGACY_PLAIN_PIN)) {
            prefs.edit().remove(KEY_LEGACY_PLAIN_PIN).apply()
        }
    }

    fun isUserLoggedIn(): Boolean {
        val hasFirebaseSession = try {
            Firebase.auth.currentUser != null
        } catch (_: Exception) {
            false
        }
        return prefs.getBoolean(KEY_IS_AUTHENTICATED, false) || hasFirebaseSession
    }

    fun getCurrentEmail(): String {
        if (!isUserLoggedIn()) return ""
        val firebaseEmail = try {
            Firebase.auth.currentUser?.email
        } catch (_: Exception) {
            null
        }
        return firebaseEmail ?: prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun getCurrentUsername(): String {
        if (!isUserLoggedIn()) return ""
        val firebaseName = try {
            Firebase.auth.currentUser?.displayName
        } catch (_: Exception) {
            null
        }
        return firebaseName ?: prefs.getString(KEY_USERNAME, "") ?: ""
    }

    fun onGoogleAuthSuccess(email: String, displayName: String?) {
        prefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, true)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USERNAME, displayName ?: email.substringBefore("@"))
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL_TIMESTAMP, 0L)
            .putInt(KEY_FAILED_PIN_ATTEMPTS, 0)
            .putLong(KEY_PIN_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    fun hasConfiguredPassword(): Boolean {
        return prefs.contains(KEY_PASSWORD_HASH) && prefs.contains(KEY_PASSWORD_SALT)
    }

    fun setupAccountPassword(email: String, username: String, password: String): Pair<Boolean, String> {
        val trimmedEmail = email.trim()
        val trimmedUser = username.trim()
        val trimmedPass = password.trim()

        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            return Pair(false, "Please provide a valid email address.")
        }
        if (trimmedUser.isEmpty()) {
            return Pair(false, "Username cannot be blank.")
        }
        if (trimmedPass.length < 8) {
            return Pair(false, "Password must be at least 8 characters.")
        }

        val salt = generateSalt()
        val hash = hashCredential(trimmedPass, salt)
        prefs.edit()
            .putString(KEY_USER_EMAIL, trimmedEmail)
            .putString(KEY_USERNAME, trimmedUser)
            .putString(KEY_PASSWORD_SALT, salt)
            .putString(KEY_PASSWORD_HASH, hash)
            .apply()

        return Pair(true, "Account security credentials configured successfully.")
    }

    fun authenticate(identifier: String, passwordAttempt: String): AuthResult {
        val trimmedId = identifier.trim()
        val trimmedPass = passwordAttempt.trim()

        if (trimmedId.isEmpty()) {
            return AuthResult.Error("Please enter your business email or username.")
        }
        if (trimmedPass.isEmpty()) {
            return AuthResult.Error("Please enter your password.")
        }

        // Lockout protection against brute-force attacks
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL_TIMESTAMP, 0L)
        if (now < lockoutUntil) {
            val secondsRemaining = ((lockoutUntil - now) / 1000).toInt().coerceAtLeast(1)
            return AuthResult.Lockout(secondsRemaining)
        }

        if (!hasConfiguredPassword()) {
            return AuthResult.Error("No local password account configured. Please sign in with Google.")
        }

        val storedEmail = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val storedUsername = prefs.getString(KEY_USERNAME, "") ?: ""
        val storedSalt = prefs.getString(KEY_PASSWORD_SALT, "") ?: ""
        val storedHash = prefs.getString(KEY_PASSWORD_HASH, "") ?: ""

        val identifierMatches = trimmedId.equals(storedEmail, ignoreCase = true) ||
                trimmedId.equals(storedUsername, ignoreCase = true)

        val computedHash = hashCredential(trimmedPass, storedSalt)
        val passwordMatches = computedHash == storedHash

        if (identifierMatches && passwordMatches) {
            prefs.edit()
                .putBoolean(KEY_IS_AUTHENTICATED, true)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL_TIMESTAMP, 0L)
                .apply()
            return AuthResult.Success(storedEmail, storedUsername)
        } else {
            val failedCount = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failedCount)
            if (failedCount >= MAX_FAILED_ATTEMPTS) {
                val newLockoutUntil = now + LOCKOUT_DURATION_MS
                editor.putLong(KEY_LOCKOUT_UNTIL_TIMESTAMP, newLockoutUntil).apply()
                return AuthResult.Lockout((LOCKOUT_DURATION_MS / 1000).toInt())
            }
            editor.apply()
            val remaining = MAX_FAILED_ATTEMPTS - failedCount
            return AuthResult.Error("Invalid credentials. $remaining attempt${if (remaining == 1) "" else "s"} remaining.")
        }
    }

    // --- SECURE 4-DIGIT APP-LOCK PIN ---

    fun hasConfiguredPin(): Boolean {
        return prefs.contains(KEY_PIN_HASH) && prefs.contains(KEY_PIN_SALT)
    }

    fun setupPin(newPin: String): Pair<Boolean, String> {
        val trimmed = newPin.trim()
        if (trimmed.length != 4 || !trimmed.all { it.isDigit() }) {
            return Pair(false, "PIN must consist of exactly 4 digits.")
        }

        val salt = generateSalt()
        val hash = hashCredential(trimmed, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .putInt(KEY_FAILED_PIN_ATTEMPTS, 0)
            .putLong(KEY_PIN_LOCKOUT_UNTIL, 0L)
            .apply()

        return Pair(true, "Security PIN created successfully.")
    }

    fun unlockWithPin(pinAttempt: String): PinResult {
        if (!hasConfiguredPin()) {
            return PinResult.NotConfigured
        }

        val trimmed = pinAttempt.trim()
        if (trimmed.length != 4 || !trimmed.all { it.isDigit() }) {
            val failed = prefs.getInt(KEY_FAILED_PIN_ATTEMPTS, 0)
            val remaining = (MAX_FAILED_ATTEMPTS - failed).coerceAtLeast(1)
            return PinResult.Error("PIN must be exactly 4 digits.", remaining)
        }

        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_PIN_LOCKOUT_UNTIL, 0L)
        if (now < lockoutUntil) {
            val secondsRemaining = ((lockoutUntil - now) / 1000).toInt().coerceAtLeast(1)
            return PinResult.Lockout(secondsRemaining)
        }

        val storedSalt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val storedHash = prefs.getString(KEY_PIN_HASH, "") ?: ""

        val computedHash = hashCredential(trimmed, storedSalt)
        return if (computedHash == storedHash) {
            prefs.edit()
                .putBoolean(KEY_IS_AUTHENTICATED, true)
                .putInt(KEY_FAILED_PIN_ATTEMPTS, 0)
                .putLong(KEY_PIN_LOCKOUT_UNTIL, 0L)
                .apply()
            PinResult.Success
        } else {
            val failedCount = prefs.getInt(KEY_FAILED_PIN_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_PIN_ATTEMPTS, failedCount)
            if (failedCount >= MAX_FAILED_ATTEMPTS) {
                val newLockoutUntil = now + LOCKOUT_DURATION_MS
                editor.putLong(KEY_PIN_LOCKOUT_UNTIL, newLockoutUntil).apply()
                PinResult.Lockout((LOCKOUT_DURATION_MS / 1000).toInt())
            } else {
                editor.apply()
                val remaining = MAX_FAILED_ATTEMPTS - failedCount
                PinResult.Error("Incorrect PIN. $remaining attempt${if (remaining == 1) "" else "s"} remaining.", remaining)
            }
        }
    }

    fun changePin(currentPin: String, newPin: String): Pair<Boolean, String> {
        if (!hasConfiguredPin()) {
            return Pair(false, "No existing PIN configured.")
        }
        val storedSalt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val storedHash = prefs.getString(KEY_PIN_HASH, "") ?: ""

        if (hashCredential(currentPin.trim(), storedSalt) != storedHash) {
            return Pair(false, "Current PIN is incorrect.")
        }
        if (newPin.trim().length != 4 || !newPin.trim().all { it.isDigit() }) {
            return Pair(false, "New PIN must be exactly 4 digits.")
        }

        val newSalt = generateSalt()
        val newHash = hashCredential(newPin.trim(), newSalt)
        prefs.edit()
            .putString(KEY_PIN_SALT, newSalt)
            .putString(KEY_PIN_HASH, newHash)
            .putInt(KEY_FAILED_PIN_ATTEMPTS, 0)
            .apply()

        return Pair(true, "Security PIN updated successfully.")
    }

    fun lock() {
        prefs.edit().putBoolean(KEY_IS_AUTHENTICATED, false).apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, false)
            .putString(KEY_USER_EMAIL, "")
            .putString(KEY_USERNAME, "")
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putInt(KEY_FAILED_PIN_ATTEMPTS, 0)
            .apply()
        try {
            Firebase.auth.signOut()
        } catch (_: Exception) {}
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashCredential(secret: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val payload = "$salt:$secret:CEYVANA_AUTHENTICATION_SALT_2026".toByteArray(Charsets.UTF_8)
        val digest = md.digest(payload)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
