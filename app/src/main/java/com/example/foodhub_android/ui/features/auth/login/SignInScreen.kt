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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foodhub_android.R
import com.example.foodhub_android.ui.FoodHubTextField
import com.example.foodhub_android.ui.GroupSocialButtons
import com.example.foodhub_android.ui.theme.Orange
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.foodhub_android.ui.features.auth.signup.SignUpViewModel
import com.example.foodhub_android.ui.navigation.AuthScreen
import com.example.foodhub_android.ui.navigation.Home
import com.example.foodhub_android.ui.navigation.Login
import com.example.foodhub_android.ui.navigation.SignUp
import kotlinx.coroutines.flow.collectLatest





@Composable
fun SignInScreen(navController: NavController,viewModel: SignInViewModel = hiltViewModel()) {
    Box(modifier = Modifier.fillMaxSize()) {

        val email = viewModel.email.collectAsStateWithLifecycle()
        val password = viewModel.password.collectAsStateWithLifecycle()
        val errorMessage = remember { mutableStateOf<String?>(null) }
        val loading = remember { mutableStateOf(false) }

        val uiState = viewModel.uiState.collectAsState()
        when(uiState.value){

            is SignInViewModel.SignInEvent.Error -> {
                // show error
                loading.value = false
                errorMessage.value = "Failed"
            }

            is SignInViewModel.SignInEvent.Loading -> {
                loading.value = true
                errorMessage.value = null
            }

            else -> {
                loading.value = false
                errorMessage.value = null
            }
        }

        val context = LocalContext.current
        LaunchedEffect(true) {
            viewModel.navigationEvent.collectLatest { event ->
                when(event){
                    is SignInViewModel.SignInNavigationEvent.NavigateToHome -> {
                        navController.navigate(Home){
                            popUpTo(AuthScreen){
                                inclusive = true
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
        Image(
            painter = painterResource(R.drawable.ic_auth_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Space reserved for the decorative circles
            Spacer(modifier = Modifier.height(104.dp))

            Text(
                text = stringResource(R.string.sign_in),
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            FoodHubTextField(
                value = email.value,
                onValueChange = { viewModel.onEmailChange(it) },
                label = {
                    Text(
                        text = stringResource(R.string.email),
                        color = Color(0xFF9B9BA8)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            FoodHubTextField(
                value = password.value,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = {
                    Text(
                        text = stringResource(R.string.password),
                        color = Color(0xFF9B9BA8)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_eye),
                        contentDescription = "Show password",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
            Text(text = errorMessage.value?: "", color =Color.Red)

            Button(
                onClick = viewModel::onSignInClick,
                modifier = Modifier
                    .height(48.dp),
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange
                )
            ) {
                Box {
                    AnimatedContent(targetState = loading.value,
                        transitionSpec = {
                            fadeIn(animationSpec = tween (300))+ scaleIn(initialScale = 0.8f) togetherWith
                                    fadeOut(animationSpec = tween (300))+ scaleOut(targetScale = 0.8f)
                        }
                        ) { target ->
                        if(target){
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 32.dp).size(24.dp)
                            )
                        }else{
                            Text(
                                text= stringResource(R.string.sign_in),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }

                    }

                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = buildAnnotatedString {
                    append("Don't have an account? ")
                    withStyle(
                        SpanStyle(
                            color = Orange,
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

            // Push social controls toward the bottom on taller displays.
            Spacer(
                modifier = Modifier
                    .heightIn(min = 30.dp)
                    .weight(1f, fill = false)
            )

            GroupSocialButtons(
                color = Color.Black,
                onFacebookClick = {}
            ) {
                // Google click
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSignUpScreen() {
    SignInScreen(rememberNavController())
}
