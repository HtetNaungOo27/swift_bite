package com.example.foodhub_android.ui.feature.address_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.data.models.Address
import com.example.foodhub_android.ui.components.FoodHubHeader
import com.example.foodhub_android.ui.components.FoodHubPage
import com.example.foodhub_android.ui.components.StatePane
import com.example.foodhub_android.ui.navigation.AddAddress
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddressListScreen(
    navController: NavController,
    viewModel: AddressListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                AddressListViewModel.AddressEvent.NavigateToAddAddress,
                AddressListViewModel.AddressEvent.NavigateToEditAddress -> navController.navigate(AddAddress)
                is AddressListViewModel.AddressEvent.NavigateBack -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set("address", event.address)
                    navController.popBackStack()
                }
                else -> Unit
            }
        }
    }

    val addressAdded by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("isAddressAdded", false)
        ?.collectAsState(false) ?: remember { mutableStateOf(false) }
    LaunchedEffect(addressAdded) {
        if (addressAdded) {
            navController.currentBackStackEntry?.savedStateHandle?.set("isAddressAdded", false)
            viewModel.getAddress()
        }
    }

    FoodHubPage {
        FoodHubHeader(
            title = "Delivery address",
            subtitle = "Choose where your order should arrive",
            onBack = navController::popBackStack,
            action = {
                FilledTonalIconButton(onClick = viewModel::onAddAddressClicked) {
                    Icon(Icons.Rounded.AddLocationAlt, contentDescription = "Add address")
                }
            }
        )
        when (val value = state) {
            AddressListViewModel.AddressState.Loading ->
                StatePane("Finding your addresses", "This will only take a moment", loading = true)

            is AddressListViewModel.AddressState.Error ->
                StatePane(
                    title = "Couldn’t load addresses",
                    message = value.message,
                    icon = Icons.Rounded.Refresh,
                    actionLabel = "Try again",
                    onAction = viewModel::getAddress
                )

            is AddressListViewModel.AddressState.Success -> {
                if (value.data.isEmpty()) {
                    StatePane(
                        title = "No saved addresses",
                        message = "Add an address to make checkout faster.",
                        icon = Icons.Rounded.LocationOn,
                        actionLabel = "Add address",
                        onAction = viewModel::onAddAddressClicked
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(value.data) { address -> AddressRow(address) { viewModel.onAddressSelected(address) } }
                        item { Spacer(Modifier.navigationBarsPadding().height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressRow(address: Address, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.Top) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(
                    Icons.Rounded.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.padding(11.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(address.addressLine1, style = MaterialTheme.typography.titleMedium)
                val details = listOfNotNull(address.addressLine2, address.city, address.state, address.zipCode, address.country)
                    .filter { it.isNotBlank() }.joinToString(", ")
                if (details.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(details, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                address.landmark?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text("Landmark: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                address.plusCode?.takeIf { it.isNotBlank() }?.let {
                    Text("Plus code: $it", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
