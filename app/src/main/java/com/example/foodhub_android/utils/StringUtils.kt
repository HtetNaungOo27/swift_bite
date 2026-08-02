package com.example.foodhub_android.utils
object StringUtils {

    fun formatCurrency(value: Double): String {
        val currencyFomatter = java.text.NumberFormat.getCurrencyInstance()
        currencyFomatter.currency = java.util.Currency.getInstance("USD")
        return currencyFomatter.format(value)
    }
}