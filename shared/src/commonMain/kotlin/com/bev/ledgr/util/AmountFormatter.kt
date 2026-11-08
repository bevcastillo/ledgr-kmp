package com.bev.ledgr.util

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Formats money amounts for display.
 *
 * Pure Kotlin (no java.text), so it works in commonMain for both Android and iOS.
 *
 * By default the full amount is shown. Pass compact = true (e.g. on the home screen)
 * to shorten large amounts.
 *
 * Default (compact = false):
 *   1,234.5           -> ₱1,234.50
 *   1,234,666,777.00  -> ₱1,234,666,777.00
 *
 * With compact = true:
 *   99,999.99         -> ₱99,999.99   (below the threshold, shown in full)
 *   500,000           -> ₱500K
 *   1,000,000         -> ₱1M
 *   1,500,000         -> ₱1.5M
 *   1,234,666,777.00  -> ₱1.23B
 *   -2,750,000        -> -₱2.75M
 */

object AmountFormatter {
    private data class Scale(val value: Double, val suffix: String)

    private val scales = listOf(
        Scale(1_000_000_000_000.0, "T"),
        Scale(1_000_000_000.0, "B"),
        Scale(1_000_000.0, "M"),
        Scale(1_000.0, "K"),
    )

    /**
     * @param amount the amount in pesos (e.g. 1234.50)
     * @param currencySymbol prefix shown before the number
     * @param compact set to true to shorten large amounts (e.g. on the home screen)
     * @param compactThreshold when compact, amounts at or above this use K/M/B/T; below it show in full
     */
    fun format(
        amount: Double,
        currencySymbol: String = "₱",
        compact: Boolean = false,
        compactThreshold: Double = 100_000.0,
    ): String {
        val absolute = abs(amount)
        val isNegative = amount < 0 && (absolute * 100).roundToLong() > 0
        val sign = if (isNegative) "-" else ""

        val body = if (compact && absolute >= compactThreshold) {
            compactBody(absolute)
        } else {
            fullBody(absolute)
        }

        return "$sign$currencySymbol$body"
    }

    /**
     * Same as [format], for amounts stored as integer centavos (e.g. 123450 = ₱1,234.50).
     */
    fun formatCentavos(
        centavos: Long,
        currencySymbol: String = "₱",
        compact: Boolean = false,
        compactThreshold: Double = 100_000.0,
    ): String = format(
        amount = centavos / 100.0,
        currencySymbol = currencySymbol,
        compact = compact,
        compactThreshold = compactThreshold,
    )

    private fun compactBody(absolute: Double): String {
        var index = scales.indexOfFirst { absolute >= it.value }
        if (index == -1) return fullBody(absolute)

        // Work in hundredths of the unit so we keep up to 2 decimals (1.23B).
        var scaledHundredths = (absolute / scales[index].value * 100).roundToLong()

        // Rounding can push a value up to the next unit, e.g. 999,999 -> "1000K".
        // In that case, move up to the next unit so it reads "1M" instead.
        if (scaledHundredths >= 100_000 && index > 0) {
            index -= 1
            scaledHundredths = (absolute / scales[index].value * 100).roundToLong()
        }

        return trimmedDecimal(scaledHundredths) + scales[index].suffix
    }

    private fun fullBody(absolute: Double): String {
        val cents = (absolute * 100).roundToLong()
        val whole = cents / 100
        val fraction = cents % 100
        return "${groupThousands(whole)}.${fraction.toString().padStart(2, '0')}"
    }

    // 150 -> "1.5", 123 -> "1.23", 100 -> "1", 50000 -> "500"
    private fun trimmedDecimal(hundredths: Long): String {
        val whole = hundredths / 100
        val fraction = hundredths % 100
        val wholeText = groupThousands(whole)

        return when {
            fraction == 0L -> wholeText
            fraction % 10 == 0L -> "$wholeText.${fraction / 10}"
            else -> "$wholeText.${fraction.toString().padStart(2, '0')}"
        }
    }

    // 1234666777 -> "1,234,666,777"
    private fun groupThousands(value: Long): String =
        value.toString()
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
}
