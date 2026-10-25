package com.example.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    private val decimalFormat = DecimalFormat("#,##0.00", symbols)

    fun formatLkr(amount: Double): String {
        return "LKR ${decimalFormat.format(amount)}"
    }

    fun formatLkrCompact(amount: Double): String {
        return if (amount >= 1_000_000) {
            String.format(Locale.US, "LKR %.2fM", amount / 1_000_000.0)
        } else if (amount >= 100_000) {
            String.format(Locale.US, "LKR %.1fK", amount / 1_000.0)
        } else {
            "LKR ${decimalFormat.format(amount)}"
        }
    }
}
