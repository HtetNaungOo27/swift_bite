package com.example.foodhub_android.ui.feature.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foodhub_android.data.models.RiderWallet
import com.example.foodhub_android.ui.components.*
import com.example.foodhub_android.utils.StringUtils

@Composable
fun RiderWalletScreen(viewModel: RiderWalletViewModel = hiltViewModel()) {
    FoodHubPage {
        FoodHubHeader("COD wallet", "Cash collection and settlement")
        when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
            RiderWalletViewModel.State.Loading -> StatePane("Loading wallet", "Checking your delivery balance", loading = true)
            is RiderWalletViewModel.State.Error -> StatePane("Couldn’t load wallet", state.message, Icons.Rounded.Refresh, actionLabel = "Retry", onAction = viewModel::refresh)
            is RiderWalletViewModel.State.Success -> WalletContent(state.wallet, false, viewModel::settle)
            is RiderWalletViewModel.State.Settling -> WalletContent(state.wallet, true, viewModel::settle)
        }
    }
}

@Composable
private fun WalletContent(wallet: RiderWallet, settling: Boolean, settle: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.extraLarge) {
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Rounded.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary)
                Text("Cash to settle", style = MaterialTheme.typography.labelLarge)
                Text(StringUtils.formatCurrency(wallet.amountToSettle), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("COD cash minus your earned delivery fees", style = MaterialTheme.typography.bodySmall)
            }
        }
        WalletRow("COD cash collected", wallet.cashCollected)
        WalletRow("Delivery earnings", wallet.deliveryEarnings)
        Text("${wallet.completedDeliveries} completed deliveries", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Button(onClick = settle, enabled = wallet.amountToSettle > 0 && !settling, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)) {
            Text(if (settling) "Settling…" else "Record settlement")
        }
        Text("For this school project, settlement records the handover locally in the backend. A production app would connect to a payment provider.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable private fun WalletRow(label: String, value: Double) {
    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f)); Text(StringUtils.formatCurrency(value), style = MaterialTheme.typography.titleMedium)
        }
    }
}
