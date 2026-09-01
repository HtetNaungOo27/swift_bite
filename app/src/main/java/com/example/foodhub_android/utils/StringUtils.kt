package com.example.foodhub_android.utils
object StringUtils {

    fun formatCurrency(value: Double): String {
        val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("my", "MM"))
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        return "${formatter.format(value * 1000.0)} Ks"
    }
}
