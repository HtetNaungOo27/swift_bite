package com.example.foodhub_android.ui.feature.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.foodhub_android.R
import com.example.foodhub_android.data.models.Address
import com.example.foodhub_android.data.models.CartItem
import com.example.foodhub_android.data.models.CheckoutDetails
import com.example.foodhub_android.ui.feature.food_item_details.FoodItemCounter
import com.example.foodhub_android.ui.navigation.AddressList
import com.example.foodhub_android.ui.navigation.OrderSuccess
import com.example.foodhub_android.utils.StringUtils
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.flow.collectLatest
import com.example.foodhub_android.ui.components.FoodHubHeader
import com.example.foodhub_android.ui.components.FoodHubPage
import com.example.foodhub_android.ui.components.StatePane
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ChevronRight

@Composable
fun CartScreen(navController: NavController, viewModel: CartViewModel){
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val fulfillmentType = viewModel.fulfillmentType.collectAsStateWithLifecycle()
    val showErrorDialog = remember {
        mutableStateOf(false)
    }
    val address = navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Address?>("address", null)
        ?.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = address?.value) {
        address?.value?.let {
            viewModel.onAddressSelected(it)
        }
    }

    val paymentSheet = rememberPaymentSheet(paymentResultCallback = {
        when (it) {
            is PaymentSheetResult.Completed -> {
                viewModel.onPaymentSuccess()
            }
            is PaymentSheetResult.Canceled -> {
                android.util.Log.d("PaymentSheet", "Payment canceled by user")
            }
            is PaymentSheetResult.Failed -> {
                android.util.Log.e("PaymentSheet", "Payment failed", it.error)
                viewModel.onPaymentFailed()
            }
        }
    })



    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest {
            when(it){
                is CartViewModel.CartEvent.onItemRemoveError,
                is CartViewModel.CartEvent.showErrorDialog,
                is CartViewModel.CartEvent.onQuantityUpdateError -> {
                    showErrorDialog.value = true
                }
                is CartViewModel.CartEvent.onAddressClicked -> {
                    navController.navigate(AddressList)
                }
                is CartViewModel.CartEvent.OrderSuccess -> {
                    navController.navigate(OrderSuccess(it.orderId!!))
                }
                is CartViewModel.CartEvent.OnInitiatePayment -> {
                    PaymentConfiguration.init(navController.context, it.data.publishableKey)
                    val customer = PaymentSheet.CustomerConfiguration(
                        it.data.customerId,
                        it.data.ephemeralKeySecret
                    )
                    val paymentSheetConfig = PaymentSheet.Configuration(
                        merchantDisplayName = "SwiftBite",
                        customer = customer,
                        allowsDelayedPaymentMethods = false,
                    )
//                    Initiate payment
                    paymentSheet.presentWithPaymentIntent(
                        it.data.paymentIntentClientSecret,
                        paymentSheetConfig
                    )
                }

                else -> {

                }
            }
        }
    }

    FoodHubPage {
        FoodHubHeader("Your cart", "Review items and choose delivery", onBack = navController::popBackStack)
        when (uiState.value) {
            is CartViewModel.CartUiState.Loading -> {
                StatePane("Preparing your cart", "Checking prices and quantities", loading = true)
            }
            is CartViewModel.CartUiState.Success -> {
                val data = (uiState.value as CartViewModel.CartUiState.Success).data
                if (data.items.size>0){
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(data.items) {
                        CartItemView(cartItem = it, onIncrement = { cartItem, quantity ->
                            viewModel.incrementQuantity(cartItem)
                        }, onDecrement = { cartItem, quantity ->
                            viewModel.decrementQuantity(cartItem)
                        }, onRemove = {
                            viewModel.removeItem(it)
                        })
                    }
                    item {
                        CheckoutDetailsView(data.checkoutDetails, fulfillmentType.value)
                    }
                }
            }else{

                StatePane("Your cart is hungry", "Explore nearby restaurants and add something delicious.", Icons.Rounded.ShoppingCart)

                }
            }
            is CartViewModel.CartUiState.Error -> {
                val message = (uiState.value as CartViewModel.CartUiState.Error).message
                StatePane("Couldn’t load your cart", message, Icons.Rounded.Refresh, actionLabel = "Try again", onAction = viewModel::getCart)

            }

            CartViewModel.CartUiState.Nothing -> {}
        }

            val selectedAddress = viewModel.selectedAddress.collectAsStateWithLifecycle()
            val specialInstructions = viewModel.specialInstructions.collectAsStateWithLifecycle()
            val riderInstructions = viewModel.riderInstructions.collectAsStateWithLifecycle()
            val scheduledFor = viewModel.scheduledFor.collectAsStateWithLifecycle()
            if (uiState.value is CartViewModel.CartUiState.Success && (uiState.value as CartViewModel.CartUiState.Success).data.items.isNotEmpty()) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("How would you like your order?", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = fulfillmentType.value == "DELIVERY",
                        onClick = { viewModel.setFulfillmentType("DELIVERY") },
                        label = { Text("Delivery") }
                    )
                    FilterChip(
                        selected = fulfillmentType.value == "PICKUP",
                        onClick = { viewModel.setFulfillmentType("PICKUP") },
                        label = { Text("Customer pickup") }
                    )
                }
                Text("When?", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = scheduledFor.value == null, onClick = viewModel::scheduleNow, label = { Text("Now") })
                    FilterChip(
                        selected = scheduledFor.value?.contains("T12:00") == false,
                        onClick = viewModel::scheduleInOneHour,
                        label = { Text("In 1 hour") }
                    )
                    FilterChip(
                        selected = scheduledFor.value?.contains("T12:00") == true,
                        onClick = viewModel::scheduleTomorrowLunch,
                        label = { Text("Tomorrow 12:00") }
                    )
                }
                val isPickup = fulfillmentType.value == "PICKUP"
                if (!isPickup) AddressCard(selectedAddress.value ) {
                    viewModel.onAddressClicked()
                }
                OutlinedTextField(
                    value = specialInstructions.value,
                    onValueChange = viewModel::onSpecialInstructionsChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Preparation instructions") },
                    placeholder = { Text("Example: less spicy, no peanuts") },
                    supportingText = { Text("Optional · ${specialInstructions.value.length}/500") },
                    minLines = 2,
                    maxLines = 3
                )
                if (!isPickup) OutlinedTextField(
                    value = riderInstructions.value,
                    onValueChange = viewModel::onRiderInstructionsChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Delivery instructions") },
                    placeholder = { Text("Example: call at the gate, third floor") },
                    supportingText = { Text("Shown only to the rider · ${riderInstructions.value.length}/500") },
                    minLines = 2,
                    maxLines = 3
                )

                Button(
                    onClick = {viewModel.checkout()},
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    enabled = selectedAddress.value != null)
                {
                    Text(text = "Pay securely by card")
                }
                OutlinedButton(
                    onClick = viewModel::checkoutWithCash,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    enabled = selectedAddress.value != null
                ) { Text(if (isPickup) "Pay at restaurant" else "Cash on delivery") }
                Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    if (showErrorDialog.value) {
        AlertDialog(
            onDismissRequest = { showErrorDialog.value = false },
            title = {
                Text(text = viewModel.errorTitle)
            },
            text = {
                Text(text = viewModel.errorMessage)
            },
            confirmButton = {
                TextButton(onClick = { showErrorDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    }

@Composable
fun AddressCard(address: Address?, onAddressClicked: () -> Unit ) {
    androidx.compose.material3.ElevatedCard(
        onClick = onAddressClicked,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(12.dp))
    if (address != null) {
        Column(Modifier.weight(1f)) {
            Text("Deliver to", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = address.addressLine1 , style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "${address.city}, ${address.state}, ${address.country}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            address.landmark?.takeIf { it.isNotBlank() }?.let { Text("Near $it", style = MaterialTheme.typography.bodySmall) }
            address.plusCode?.takeIf { it.isNotBlank() }?.let { Text("Plus code: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
        }
    }else{
        Column(Modifier.weight(1f)) {
            Text("Delivery address", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Choose an address", style = MaterialTheme.typography.titleMedium)
        }
    }
            Icon(Icons.Rounded.ChevronRight, "Change address", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
}
}
@Composable
fun CheckoutDetailsView(checkoutDetails: CheckoutDetails, fulfillmentType: String) {
    val pickup = fulfillmentType == "PICKUP"
    val deliveryFee = if (pickup) 0.0 else checkoutDetails.deliveryFee
    val total = if (pickup) checkoutDetails.totalAmount - checkoutDetails.deliveryFee else checkoutDetails.totalAmount
    Column {
       CheckoutRowItem(title = "Subtotal", value = checkoutDetails.subTotal, currency = "MMK")
        CheckoutRowItem(title = "Tax", value = checkoutDetails.tax, currency = "MMK")
        CheckoutRowItem(title = "Delivery fee", value = deliveryFee, currency = "MMK")
        CheckoutRowItem(title = "Total", value = total, currency = "MMK")
    }
}
@Composable
fun CheckoutRowItem(title: String, value: Double, currency: String) {
    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)){
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
                    Text(text = StringUtils.formatCurrency(value), style = MaterialTheme.typography.titleMedium )
            Text(text = " $currency", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
        VerticalDivider()

    }
}

@Composable
fun CartItemView(
    cartItem: CartItem,
    onIncrement: (CartItem, Int) -> Unit,
     onDecrement: (CartItem, Int) -> Unit,
      onRemove: (CartItem) ->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AsyncImage(
            model = cartItem.menuItemId.imageUrl, contentDescription = null,
            modifier = Modifier
                .size(82.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(verticalArrangement =  Arrangement.Center) {
            Row (verticalAlignment = Alignment.CenterVertically) {
                Text(text = cartItem.menuItemId.name,style = MaterialTheme.typography.titleMedium)
                Spacer(
                    modifier = Modifier.weight(1f))
                        IconButton (onClick = {onRemove.invoke(cartItem) },
                            modifier = Modifier.size(24.dp)){
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove ${cartItem.menuItemId.name} from cart",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (cartItem.selectedModifiers.isNotEmpty()) Text(cartItem.selectedModifiers.joinToString(" · ") { "${it.group}: ${it.option}" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = cartItem.menuItemId.description, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.size(8.dp))
            Row {
                Text(
                    text = StringUtils.formatCurrency(cartItem.menuItemId.price),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(
                    modifier = Modifier.weight(1f))
                FoodItemCounter(
                    count = cartItem.quantity,
                    onCounterIncrement = {onIncrement.invoke(cartItem, cartItem.quantity)},
                    onCounterDecrement = { onDecrement.invoke(cartItem, cartItem.quantity)}
                )

            }

        }
    }

    }
