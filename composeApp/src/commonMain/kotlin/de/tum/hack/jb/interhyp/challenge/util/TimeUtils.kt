package de.tum.hack.jb.interhyp.challenge.util

/**
 * Multiplatform utility for time operations
 */

/**
 * Get current time in milliseconds since epoch
 */
expect fun currentTimeMillis(): Long

/**
 * Get day of month from timestamp (1-31)
 */
expect fun getDayOfMonth(timestamp: Long): Int

/**
 * Get month from timestamp (0-11, where 0 is January)
 */
expect fun getMonth(timestamp: Long): Int

/**
 * Get year from timestamp
 */
expect fun getYear(timestamp: Long): Int

/**
 * Get start of month timestamp for a given timestamp
 */
expect fun getStartOfMonth(timestamp: Long): Long

/**
 * Format timestamp to date string (e.g., "MMM dd, yyyy")
 */
expect fun formatDate(timestamp: Long): String

/**
 * Format currency amount (e.g., "€123.45")
 */
fun formatCurrency(amount: Double): String {
    // Use simple string interpolation that works on all platforms
    val isNegative = amount < 0
    val absAmount = if (isNegative) -amount else amount
    val rounded = (absAmount * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val centsStr = cents.toString().padStart(2, '0')
    val sign = if (isNegative) "-" else ""
    return "$sign€$euros.$centsStr"
}

