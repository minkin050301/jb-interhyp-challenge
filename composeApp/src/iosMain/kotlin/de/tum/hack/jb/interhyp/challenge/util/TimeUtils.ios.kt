package de.tum.hack.jb.interhyp.challenge.util

import platform.Foundation.*

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun getDayOfMonth(timestamp: Long): Int {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val calendar = NSCalendar.currentCalendar
    val components = calendar.components(
        NSCalendarUnitDay,
        fromDate = date
    )
    return components.day.toInt()
}

actual fun getMonth(timestamp: Long): Int {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val calendar = NSCalendar.currentCalendar
    val components = calendar.components(
        NSCalendarUnitMonth,
        fromDate = date
    )
    return (components.month.toInt() - 1) // iOS months are 1-12, convert to 0-11
}

actual fun getYear(timestamp: Long): Int {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val calendar = NSCalendar.currentCalendar
    val components = calendar.components(
        NSCalendarUnitYear,
        fromDate = date
    )
    return components.year.toInt()
}

actual fun getStartOfMonth(timestamp: Long): Long {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val calendar = NSCalendar.currentCalendar
    val components = calendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth,
        fromDate = date
    )
    components.day = 1
    components.hour = 0
    components.minute = 0
    components.second = 0
    val startOfMonth = calendar.dateFromComponents(components)
    return ((startOfMonth?.timeIntervalSince1970 ?: 0.0) * 1000).toLong()
}

actual fun formatDate(timestamp: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val formatter = NSDateFormatter()
    formatter.dateFormat = "MMM dd, yyyy"
    return formatter.stringFromDate(date)
}

