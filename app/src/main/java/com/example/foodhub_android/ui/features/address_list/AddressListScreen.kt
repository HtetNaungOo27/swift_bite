package com.example.foodhub_android.ui.features.address_list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

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

    when(val addressState = state.value){
        is AddressListViewModel.AddressState.Loading -> {

        }

        is AddressListViewModel.AddressState.Success -> {

        }

        is AddressListViewModel.AddressState.Error -> {

        }
    }

}