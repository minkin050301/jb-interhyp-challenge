package de.tum.hack.jb.interhyp.challenge.util

import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
private val jsDateNow: () -> Double = js("Date.now")

@OptIn(ExperimentalWasmJsInterop::class)
private val jsGetDay: (Double) -> Double = js("(function(ts) { var d = new Date(ts); return d.getDate(); })")

@OptIn(ExperimentalWasmJsInterop::class)
private val jsGetMonth: (Double) -> Double = js("(function(ts) { var d = new Date(ts); return d.getMonth(); })")

@OptIn(ExperimentalWasmJsInterop::class)
private val jsGetYear: (Double) -> Double = js("(function(ts) { var d = new Date(ts); return d.getFullYear(); })")

@OptIn(ExperimentalWasmJsInterop::class)
private val jsGetStartOfMonth: (Double) -> Double = js("(function(ts) { var d = new Date(ts); return new Date(d.getFullYear(), d.getMonth(), 1).getTime(); })")

@OptIn(ExperimentalWasmJsInterop::class)
actual fun currentTimeMillis(): Long {
    return jsDateNow().toLong()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getDayOfMonth(timestamp: Long): Int {
    return jsGetDay(timestamp.toDouble()).toInt()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getMonth(timestamp: Long): Int {
    return jsGetMonth(timestamp.toDouble()).toInt()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getYear(timestamp: Long): Int {
    return jsGetYear(timestamp.toDouble()).toInt()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getStartOfMonth(timestamp: Long): Long {
    return jsGetStartOfMonth(timestamp.toDouble()).toLong()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun formatDate(timestamp: Long): String {
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthIndex = jsGetMonth(timestamp.toDouble()).toInt()
    val day = jsGetDay(timestamp.toDouble()).toInt()
    val year = jsGetYear(timestamp.toDouble()).toInt()
    val month = months[monthIndex]
    return "$month ${day.toString().padStart(2, '0')}, $year"
}
