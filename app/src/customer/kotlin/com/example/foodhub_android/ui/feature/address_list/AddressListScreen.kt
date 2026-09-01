package com.example.foodhub_android.ui.feature.address_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.DeleteOutline
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
import com.example.foodhub_android.ui.components.ListSkeleton
import com.example.foodhub_android.ui.navigation.AddAddress
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddressListScreen(
    navController: NavController,
    viewModel: AddressListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var deleteCandidate by remember { mutableStateOf<Address?>(null) }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                AddressListViewModel.AddressEvent.NavigateToAddAddress -> {
                    navController.currentBackStackEntry?.savedStateHandle?.remove<Address>("editAddress")
                    navController.navigate(AddAddress)
                }
                AddressListViewModel.AddressEvent.NavigateToEditAddress -> navController.navigate(AddAddress)
                is AddressListViewModel.AddressEvent.NavigateBack -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set("address", event.address)
                    navController.popBackStack()
                }
                is AddressListViewModel.AddressEvent.Message ->
                    android.widget.Toast.makeText(navController.context, event.text, android.widget.Toast.LENGTH_LONG).show()
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
                ListSkeleton(rows = 3)

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
                        items(value.data, key = { it.id ?: it.addressLine1 }) { address ->
                            AddressRow(
                                address = address,
                                onSelect = { viewModel.onAddressSelected(address) },
                                onEdit = {
                                    navController.currentBackStackEntry?.savedStateHandle?.set("editAddress", address)
                                    navController.navigate(AddAddress)
                                },
                                onDelete = { deleteCandidate = address }
                            )
                        }
                        item { Spacer(Modifier.navigationBarsPadding().height(8.dp)) }
                    }
                }
            }
        }
    }
    deleteCandidate?.let { address ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null) },
            title = { Text("Delete this address?") },
            text = { Text("${address.addressLine1}\n\nAddresses used by an existing order are retained as immutable delivery records.") },
            confirmButton = {
                Button(onClick = { deleteCandidate = null; viewModel.deleteAddress(address) }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteCandidate = null }) { Text("Keep") } }
        )
    }
}

@Composable
private fun AddressRow(address: Address, onSelect: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
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
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onEdit, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Edit")
                    }
                    TextButton(onClick = onDelete, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Delete")
                    }
                }
            }
        }
    }
}
