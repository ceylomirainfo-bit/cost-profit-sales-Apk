package com.example.ui.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateUtils {
    // Official Business Timezone: Asia/Colombo (Sri Lanka Standard Time UTC+05:30)
    val COLOMBO_ZONE: ZoneId = ZoneId.of("Asia/Colombo")

    // Historical reference date for initial seed/catalog data
    const val INITIAL_BUSINESS_DATE = "2026-10-24"

    private val isoFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val displayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
    private val monthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    private val reportTimestampFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a z", Locale.US)

    /**
     * Returns today's actual calendar date in Asia/Colombo timezone as LocalDate
     */
    fun getTodayColombo(): LocalDate {
        return LocalDate.now(COLOMBO_ZONE)
    }

    /**
     * Returns today's actual calendar date in Asia/Colombo timezone as YYYY-MM-DD
     */
    fun getActualTodayIso(): String {
        return getTodayColombo().format(isoFormat)
    }

    /**
     * Returns yesterday's calendar date in Asia/Colombo timezone as YYYY-MM-DD
     */
    fun getYesterdayIso(): String {
        return getTodayColombo().minusDays(1).format(isoFormat)
    }

    /**
     * Returns the date range for the current week (Monday to Sunday) in Asia/Colombo
     */
    fun getThisWeekRangeIso(): Pair<String, String> {
        val today = getTodayColombo()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return Pair(monday.format(isoFormat), sunday.format(isoFormat))
    }

    /**
     * Returns the date range for the current month (1st of month to end of month) in Asia/Colombo
     */
    fun getThisMonthRangeIso(): Pair<String, String> {
        val today = getTodayColombo()
        val firstDay = today.withDayOfMonth(1)
        val lastDay = today.with(TemporalAdjusters.lastDayOfMonth())
        return Pair(firstDay.format(isoFormat), lastDay.format(isoFormat))
    }

    /**
     * Returns the date range for the previous month (1st of prev month to end of prev month) in Asia/Colombo
     */
    fun getPreviousMonthRangeIso(): Pair<String, String> {
        val today = getTodayColombo()
        val prevMonth = today.minusMonths(1)
        val firstDay = prevMonth.withDayOfMonth(1)
        val lastDay = prevMonth.with(TemporalAdjusters.lastDayOfMonth())
        return Pair(firstDay.format(isoFormat), lastDay.format(isoFormat))
    }

    /**
     * Returns current date and time formatted in Asia/Colombo timezone
     */
    fun getCurrentTimestampDisplay(): String {
        return ZonedDateTime.now(COLOMBO_ZONE).format(reportTimestampFormat)
    }

    fun formatIsoToDisplay(isoDate: String): String {
        return try {
            val parsed = LocalDate.parse(isoDate, isoFormat)
            parsed.format(displayFormat)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatPeriodRangeDisplay(startDateIso: String, endDateIso: String): String {
        val startDisplay = formatIsoToDisplay(startDateIso)
        val endDisplay = formatIsoToDisplay(endDateIso)
        return if (startDateIso == endDateIso) {
            startDisplay
        } else {
            "$startDisplay – $endDisplay"
        }
    }

    fun formatIsoToMonthYear(isoDate: String): String {
        return try {
            val parsed = LocalDate.parse(isoDate, isoFormat)
            parsed.format(monthYearFormat)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun parseLocalDate(isoDate: String): LocalDate {
        return try {
            LocalDate.parse(isoDate, isoFormat)
        } catch (_: Exception) {
            getTodayColombo()
        }
    }

    fun toIsoString(date: LocalDate): String {
        return date.format(isoFormat)
    }

    /**
     * Validates that end date is on or after start date
     */
    fun isRangeValid(startDateIso: String, endDateIso: String): Boolean {
        return try {
            val start = LocalDate.parse(startDateIso, isoFormat)
            val end = LocalDate.parse(endDateIso, isoFormat)
            !end.isBefore(start)
        } catch (_: Exception) {
            false
        }
    }
}
