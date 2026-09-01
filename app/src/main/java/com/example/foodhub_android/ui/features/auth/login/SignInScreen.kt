package com.example.foodhub_android.ui.features.auth.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foodhub_android.R
import com.example.foodhub_android.ui.FoodHubTextField
import com.example.foodhub_android.ui.GroupSocialButtons
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.foodhub_android.ui.features.auth.signup.SignUpViewModel
import com.example.foodhub_android.ui.navigation.AuthScreen
import com.example.foodhub_android.ui.navigation.Home
import com.example.foodhub_android.ui.navigation.Login
import com.example.foodhub_android.ui.navigation.SignUp
import com.example.foodhub_android.ui.navigation.OrderDetails
import com.example.foodhub_android.ui.navigation.RiderOrderDetails
import kotlinx.coroutines.flow.collectLatest





@Composable
fun SignInScreen(navController: NavController,isCustomer: Boolean = true,viewModel: SignInViewModel = hiltViewModel()) {
    Box(modifier = Modifier.fillMaxSize()) {

        val email = viewModel.email.collectAsStateWithLifecycle()
        val password = viewModel.password.collectAsStateWithLifecycle()
        val validation by viewModel.validation.collectAsStateWithLifecycle()
        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        var showReset by rememberSaveable { mutableStateOf(false) }
        var resetEmail by rememberSaveable { mutableStateOf("") }
        var resetCode by rememberSaveable { mutableStateOf("") }
        var newPassword by rememberSaveable { mutableStateOf("") }
        var codeRequested by rememberSaveable { mutableStateOf(false) }
        val resetState by viewModel.resetState.collectAsStateWithLifecycle()

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val loading = uiState is SignInViewModel.SignInEvent.Loading
        val errorMessage = if (uiState is SignInViewModel.SignInEvent.Error) "Email or password is incorrect. Please try again." else null

        val context = LocalContext.current
        val roleLabel = when {
            context.packageName.endsWith(".restaurant") -> "RESTAURANT PORTAL"
            context.packageName.endsWith(".rider") -> "RIDER PORTAL"
            else -> "CUSTOMER APP"
        }
        LaunchedEffect(true) {
            viewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is SignInViewModel.SignInNavigationEvent.NavigateToHome -> {
                        navController.navigate(Home) {
                            popUpTo(AuthScreen) {
                                inclusive = true
                            }
                        }
                        event.pendingOrderId?.let { orderId ->
                            if (navController.context.packageName.endsWith(".rider")) {
                                navController.navigate(RiderOrderDetails(orderId)) { launchSingleTop = true }
                            } else {
                                navController.navigate(OrderDetails(orderId)) { launchSingleTop = true }
                            }
                        }
                    }

                    is SignInViewModel.SignInNavigationEvent.NavigateToSignUp -> {
                        navController.navigate(SignUp)
                    }

                    else -> {

                    }
                }
            }

        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(44.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    "SWIFTBITE",
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Welcome back",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = when (roleLabel) {
                    "RESTAURANT PORTAL" -> "Sign in to manage your restaurant"
                    "RIDER PORTAL" -> "Sign in to view deliveries and earnings"
                    else -> "Sign in to order from restaurants near you"
                },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            FoodHubTextField(
                value = email.value,
                onValueChange = { viewModel.onEmailChange(it) },
                label = {
                    Text(
                        text = stringResource(R.string.email),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth()
                ,isError = validation.email != null,
                supportingText = { Text(validation.email ?: " ") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            FoodHubTextField(
                value = password.value,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = {
                    Text(
                        text = stringResource(R.string.password),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = validation.password != null,
                supportingText = { Text(validation.password ?: " ") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { resetEmail = email.value; showReset = true },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Forgot password?") }
            errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = viewModel::onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Box {
                    AnimatedContent(
                        targetState = loading,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f) togetherWith
                                    fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)
                        }
                    ) { target ->
                        if (target) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 32.dp).size(24.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.sign_in),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 32.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                    }

                }

            }

            Spacer(modifier = Modifier.height(20.dp))
            if (isCustomer) {
                Text(
                    text = buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("Sign Up")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onSignUpClicked() }
                        .padding(vertical = 8.dp),
                    color = Color(0xFF5B5B5E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier
                        .heightIn(min = 30.dp)
                        .weight(1f, fill = false)
                )

                GroupSocialButtons(
                    color = MaterialTheme.colorScheme.onBackground,
                    viewModel
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        if (showReset) {
            if (resetState is SignInViewModel.PasswordResetState.CodeSent) {
                LaunchedEffect(resetState) {
                    codeRequested = true
                    (resetState as SignInViewModel.PasswordResetState.CodeSent).debugCode?.let { resetCode = it }
                }
            }
            if (resetState is SignInViewModel.PasswordResetState.Success) {
                LaunchedEffect(resetState) {
                    showReset = false
                    codeRequested = false
                    newPassword = ""
                    viewModel.clearPasswordReset()
                }
            }
            AlertDialog(
                onDismissRequest = {
                    if (resetState !is SignInViewModel.PasswordResetState.Loading) {
                        showReset = false; codeRequested = false; viewModel.clearPasswordReset()
                    }
                },
                title = { Text(if (codeRequested) "Set a new password" else "Recover your account") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            if (codeRequested) "Enter the six-digit code sent to your account. It expires in 15 minutes."
                            else "We’ll send a single-use reset code if this account exists.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(resetEmail, { resetEmail = it }, label = { Text("Email") }, singleLine = true, enabled = !codeRequested)
                        if (codeRequested) {
                            OutlinedTextField(resetCode, { resetCode = it.filter(Char::isDigit).take(6) }, label = { Text("6-digit code") }, singleLine = true)
                            OutlinedTextField(newPassword, { newPassword = it }, label = { Text("New password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                        }
                        if (resetState is SignInViewModel.PasswordResetState.Error) {
                            Text((resetState as SignInViewModel.PasswordResetState.Error).message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (codeRequested) viewModel.confirmPasswordReset(resetEmail, resetCode, newPassword)
                            else viewModel.requestPasswordReset(resetEmail)
                        },
                        enabled = resetState !is SignInViewModel.PasswordResetState.Loading
                    ) {
                        Text(if (codeRequested) "Update password" else "Send reset code")
                    }
                },
                dismissButton = { TextButton(onClick = { showReset = false; codeRequested = false; viewModel.clearPasswordReset() }) { Text("Cancel") } }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSignUpScreen() {
    SignInScreen(rememberNavController())
}
