package de.tum.hack.jb.interhyp.challenge.util

actual fun currentTimeMillis(): Long {
    return kotlin.js.Date().getTime().toLong()
}

actual fun getDayOfMonth(timestamp: Long): Int {
    val date = kotlin.js.Date(timestamp.toDouble())
    return date.getDate()
}

actual fun getMonth(timestamp: Long): Int {
    val date = kotlin.js.Date(timestamp.toDouble())
    return date.getMonth()
}

actual fun getYear(timestamp: Long): Int {
    val date = kotlin.js.Date(timestamp.toDouble())
    return date.getFullYear()
}

actual fun getStartOfMonth(timestamp: Long): Long {
    val date = kotlin.js.Date(timestamp.toDouble())
    val year = date.getFullYear()
    val month = date.getMonth()
    val newDate = kotlin.js.Date(year, month, 1, 0, 0, 0, 0)
    return newDate.getTime().toLong()
}

actual fun formatDate(timestamp: Long): String {
    val date = kotlin.js.Date(timestamp.toDouble())
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val month = months[date.getMonth()]
    val day = date.getDate()
    val year = date.getFullYear()
    return "$month ${day.toString().padStart(2, '0')}, $year"
}

