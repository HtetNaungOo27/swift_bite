package com.example.foodhub_android.ui.features.address_list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import com.example.foodhub_android.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.ui.features.cart.AddressCard
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button


@Composable
fun AddressListScreen(
    navController: NavController,
    viewModel: AddressListViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = true){
        viewModel.event.collectLatest {
            when(val addressEvent = it){
                is AddressListViewModel.AddressEvent.NavigateToAddAddress -> {

                }
                is AddressListViewModel.AddressEvent.NavigateToEditAddress -> {

                }
                else -> {

                }
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = null,
                    modifier = Modifier.clickable{
                        navController.popBackStack()
                    }
                )

            Text(text = "Address List", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.size(40.dp))
        }
        when(val addressState = state.value){
            is AddressListViewModel.AddressState.Loading -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Show loading
                    CircularProgressIndicator()
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

            }

            is AddressListViewModel.AddressState.Success -> {
                LazyColumn(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    items(addressState.data) { address ->
                        AddressCard (address = address, onAddressClicked = {})
                    }
                }

            }

            is AddressListViewModel.AddressState.Error -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = addressState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Button(onClick = { viewModel.getAddress()}) {
                        Text(text = "Retry")
                    }
                }

            }
        }
    }

}