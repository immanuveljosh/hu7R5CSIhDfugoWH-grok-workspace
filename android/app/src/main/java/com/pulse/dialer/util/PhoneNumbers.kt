package com.pulse.dialer.util

object PhoneNumbers {
    fun digitsAndSymbols(raw: String): String =
        raw.filter { it.isDigit() || it == '*' || it == '#' || it == '+' }

    fun display(raw: String): String {
        val v = raw.trim()
        if (v.isEmpty()) return ""
        val hasPlus = v.startsWith("+")
        val rest = v.filter { it.isDigit() || it == '*' || it == '#' }
        if (rest.any { it == '*' || it == '#' }) return v
        if (hasPlus) {
            if (rest.startsWith("91") && rest.length == 12) {
                return "+91 ${rest.substring(2, 7)} ${rest.substring(7)}"
            }
            if (rest.startsWith("1") && rest.length == 11) {
                return "+1 ${rest.substring(1, 4)} ${rest.substring(4, 7)} ${rest.substring(7)}"
            }
            return "+$rest"
        }
        return when {
            rest.length == 10 -> "${rest.substring(0, 3)} ${rest.substring(3, 6)} ${rest.substring(6)}"
            else -> v
        }
    }

    fun initials(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (parts.isEmpty()) return "#"
        if (parts.size == 1) return parts[0].take(2).uppercase()
        return (parts.first().first().toString() + parts.last().first()).uppercase()
    }
}
