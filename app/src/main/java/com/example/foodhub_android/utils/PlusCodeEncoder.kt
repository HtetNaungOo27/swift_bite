package com.example.foodhub_android.utils

import kotlin.math.floor

/** Generates a standard 10-digit Open Location Code from a map coordinate. */
object PlusCodeEncoder {
    private const val ALPHABET = "23456789CFGHJMPQRVWX"
    private val pairResolutions = doubleArrayOf(20.0, 1.0, 0.05, 0.0025, 0.000125)

    fun encode(latitude: Double, longitude: Double): String {
        var lat = latitude.coerceIn(-90.0, 90.0)
        var lon = normalizeLongitude(longitude)
        if (lat == 90.0) lat -= 1e-12
        lat += 90.0
        lon += 180.0

        return buildString(11) {
            pairResolutions.forEachIndexed { index, resolution ->
                val latDigit = floor(lat / resolution).toInt().coerceIn(0, ALPHABET.lastIndex)
                val lonDigit = floor(lon / resolution).toInt().coerceIn(0, ALPHABET.lastIndex)
                append(ALPHABET[latDigit])
                append(ALPHABET[lonDigit])
                lat -= latDigit * resolution
                lon -= lonDigit * resolution
                if (index == 3) append('+')
            }
        }
    }

    private fun normalizeLongitude(value: Double): Double {
        var result = value
        while (result < -180.0) result += 360.0
        while (result >= 180.0) result -= 360.0
        return result
    }
}
