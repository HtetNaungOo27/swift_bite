package com.example.foodhub_android.ui.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.foodhub_android.R
import com.example.foodhub_android.ui.GroupSocialButtons
import com.example.foodhub_android.ui.navigation.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthScreen(navController: NavController, isCustomer: Boolean = true, viewModel: AuthScreenViewModel = hiltViewModel()) {
    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                AuthScreenViewModel.SignInNavigationEvent.NavigateToHome -> navController.navigate(Home) { popUpTo(com.example.foodhub_android.ui.navigation.AuthScreen) { inclusive = true } }
                AuthScreenViewModel.SignInNavigationEvent.NavigateToSignUp -> navController.navigate(SignUp)
            }
        }
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.align(Alignment.Center).fillMaxWidth()
                .navigationBarsPadding().padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Text("SWIFTBITE", Modifier.padding(horizontal = 14.dp, vertical = 7.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                if (isCustomer) "Great food, one tap away" else "Welcome to your workspace",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(7.dp))
            Text(
                if (isCustomer) "Discover local favorites and get them delivered." else "Manage your day with a clear, simple workflow.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            if (isCustomer) {
                GroupSocialButtons(viewModel = viewModel)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { navController.navigate(SignUp) }, Modifier.fillMaxWidth().height(54.dp)) { Text("Create account with email") }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { navController.navigate(Login) }, Modifier.fillMaxWidth().height(54.dp)) {
                Text(if (isCustomer) "Sign in" else "Continue to sign in")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() = AuthScreen(rememberNavController())
