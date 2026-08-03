package com.example.foodhub_android

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.ui.features.auth.AuthScreen
import com.example.foodhub_android.ui.features.auth.login.SignInScreen
import com.example.foodhub_android.ui.features.auth.signup.SignUpScreen
import com.example.foodhub_android.ui.features.home.HomeScreen
import com.example.foodhub_android.ui.features.restaurant_details.RestaurantDetailScreen
import com.example.foodhub_android.ui.navigation.AuthScreen
import com.example.foodhub_android.ui.navigation.Cart
import com.example.foodhub_android.ui.navigation.FoodDetails
import com.example.foodhub_android.ui.navigation.Home
import com.example.foodhub_android.ui.navigation.Login
import com.example.foodhub_android.ui.navigation.RestaurantDetails
import com.example.foodhub_android.ui.navigation.SignUp
import com.example.foodhub_android.ui.navigation.foodItemNavType
import com.example.foodhub_android.ui.theme.FoodHubAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.typeOf
import androidx.compose.animation.SharedTransitionLayout
import com.example.foodhub_android.ui.features.cart.CartScreen
import com.example.foodhub_android.ui.features.food_item_details.FoodDetailsScreen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    var showSplashScreen = true

    @Inject
    lateinit var foodApi: FoodApi
    @Inject
    lateinit var session: FoodHubSession
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                showSplashScreen
            }
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    0.5f,
                    0f
                )
                val zoomY = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_Y,
                    0.5f,
                    0f
                )

                zoomX.duration = 500
                zoomY.duration = 500
                zoomX.interpolator = OvershootInterpolator()
                zoomY.interpolator = OvershootInterpolator()
                zoomX.doOnEnd {
                    screen.remove()
                }
                zoomY.doOnEnd {
                    screen.remove()
                }
                zoomX.start()
                zoomY.start()
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodHubAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    SharedTransitionLayout {
                        NavHost(
                            navController = navController,
                            startDestination = if (session.getToken() != null) Home else AuthScreen,
                            modifier = Modifier.padding(innerPadding),
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                    tween(300)
                                ) + fadeOut(tween(300))
                            }

                        ) {
                            composable<SignUp> {
                                SignUpScreen(navController)
                            }
                            composable<AuthScreen> {
                                AuthScreen(navController)
                            }
                            composable<Login> {
                                SignInScreen(navController)
                            }
                            composable<Home> {
                                HomeScreen(navController)
                            }
                            composable<RestaurantDetails> {
                                val route = it.toRoute<RestaurantDetails>()
                                RestaurantDetailScreen(
                                    navController = navController,
                                    name = route.restaurantName,
                                    imageUrl = route.restaurantImageUrl,
                                    restaurantID = route.restaurantId,
                                    animatedVisibilityScope = this@composable
                                )

                            }
                            composable<FoodDetails>(
                                typeMap = mapOf(typeOf<FoodItem>() to foodItemNavType)
                            ) {
                                val route = it.toRoute<FoodDetails>()
                                FoodDetailsScreen(
                                    navController,
                                    foodItem = route.foodItem,
                                    this
                                )
                            }
                            composable<Cart> {
                                CartScreen(navController)
                            }
                        }
                    }
                }
            }
        }
        if(::foodApi.isInitialized){
            Log.d("MainActivity","FoodApi initialized.")
        }
        CoroutineScope(Dispatchers.IO).launch {
            delay(3000)
            showSplashScreen = false
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    FoodHubAndroidTheme {
//        Greeting("Android")
//    }
//}

@Preview(showBackground = true)
@Composable
private fun PreviewMainActivity() {
    FoodHubAndroidTheme {
        AuthScreen(rememberNavController())
    }
}
