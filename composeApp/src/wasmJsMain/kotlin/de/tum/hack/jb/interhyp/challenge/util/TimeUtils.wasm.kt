package de.tum.hack.jb.interhyp.challenge.util

import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
actual fun currentTimeMillis(): Long {
    return (js("Date.now()") as Number).toLong()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getDayOfMonth(timestamp: Long): Int {
    val getDay: (Double) -> Number = js("(function(t) { return new Date(t).getDate(); })")
    return getDay(timestamp.toDouble()).toInt()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getMonth(timestamp: Long): Int {
    val getMonth: (Double) -> Number = js("(function(t) { return new Date(t).getMonth(); })")
    return getMonth(timestamp.toDouble()).toInt()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getYear(timestamp: Long): Int {
    val getYear: (Double) -> Number = js("(function(t) { return new Date(t).getFullYear(); })")
    return getYear(timestamp.toDouble()).toInt()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getStartOfMonth(timestamp: Long): Long {
    val getStart: (Double) -> Number = js("(function(t) { var d = new Date(t); return new Date(d.getFullYear(), d.getMonth(), 1).getTime(); })")
    return getStart(timestamp.toDouble()).toLong()
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun formatDate(timestamp: Long): String {
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val getMonth: (Double) -> Number = js("(function(t) { return new Date(t).getMonth(); })")
    val getDay: (Double) -> Number = js("(function(t) { return new Date(t).getDate(); })")
    val getYear: (Double) -> Number = js("(function(t) { return new Date(t).getFullYear(); })")
    val monthIndex = getMonth(timestamp.toDouble()).toInt()
    val day = getDay(timestamp.toDouble()).toInt()
    val year = getYear(timestamp.toDouble()).toInt()
    val month = months[monthIndex]
    return "$month ${day.toString().padStart(2, '0')}, $year"
}

