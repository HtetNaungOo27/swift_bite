package com.example.foodhub_android.data.models

data class PayoutAccount(val bankName: String, val accountName: String, val accountNumberMasked: String)
data class PayoutItem(val id: String, val amount: Double, val status: String, val reference: String?, val requestedAt: String, val processedAt: String?)
data class PayoutOverview(val account: PayoutAccount?, val availableBalance: Double, val history: List<PayoutItem>)
data class SavePayoutAccountRequest(val bankName: String, val accountName: String, val accountNumber: String)
data class RequestPayout(val amount: Double)
