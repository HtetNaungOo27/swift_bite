package com.example.foodhub_android

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.Order
import com.example.foodhub_android.ui.FoodHubNavHost
import com.example.foodhub_android.ui.feature.home.HomeScreen
import com.example.foodhub_android.ui.feature.menu.add.AddMenuItemScreen
import com.example.foodhub_android.ui.feature.menu.image.ImagePickerScreen
import com.example.foodhub_android.ui.feature.menu.list.ListMenuItemsScreen
import com.example.foodhub_android.ui.feature.order_details.OrderDetailsScreen
import com.example.foodhub_android.ui.feature.order_list.OrderListScreen
import com.example.foodhub_android.ui.features.auth.AuthScreen
import com.example.foodhub_android.ui.features.auth.login.SignInScreen
import com.example.foodhub_android.ui.features.auth.signup.SignUpScreen
import com.example.foodhub_android.ui.features.notifications.NotificationsList
import com.example.foodhub_android.ui.features.notifications.NotificationsViewModel
import com.example.foodhub_android.ui.components.NotificationPermissionPrompt
import com.example.foodhub_android.ui.navigation.AddMenu
import com.example.foodhub_android.ui.navigation.AppSettings
import com.example.foodhub_android.ui.features.settings.AppSettingsScreen
import com.example.foodhub_android.ui.features.account.AccountScreen
import com.example.foodhub_android.ui.navigation.Account
import com.example.foodhub_android.ui.navigation.Payouts
import com.example.foodhub_android.ui.features.payout.PayoutScreen
import com.example.foodhub_android.ui.navigation.AuthScreen
import com.example.foodhub_android.ui.navigation.Home
import com.example.foodhub_android.ui.navigation.ImagePicker
import com.example.foodhub_android.ui.navigation.Login
import com.example.foodhub_android.ui.navigation.MenuList
import com.example.foodhub_android.ui.navigation.NavRoute
import com.example.foodhub_android.ui.navigation.Notification
import com.example.foodhub_android.ui.navigation.OrderDetails
import com.example.foodhub_android.ui.navigation.OrderList
import com.example.foodhub_android.ui.navigation.OrderSuccess
import com.example.foodhub_android.ui.navigation.SignUp
import com.example.foodhub_android.ui.theme.RestaurantTheme
import com.example.foodhub_android.ui.theme.ThemeMode
import com.example.foodhub_android.ui.theme.ThemePreferences
import com.example.foodhub_android.ui.theme.Mustard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseFoodHubActivity() {
    var showSplashScreen = true

    @Inject
    lateinit var foodApi: FoodApi

    @Inject
    lateinit var session: FoodHubSession

    sealed class BottomNavItem(val route: NavRoute, val icon: Int) {
        object Home : BottomNavItem(com.example.foodhub_android.ui.navigation.Home, R.drawable.ic_home)
        object Notification :
            BottomNavItem(
                com.example.foodhub_android.ui.navigation.Notification,
                R.drawable.ic_notification
            )

        object Orders : BottomNavItem(
            com.example.foodhub_android.ui.navigation.OrderList,
            R.drawable.ic_orders
        )

        object Menu : BottomNavItem(
            com.example.foodhub_android.ui.navigation.MenuList,
            android.R.drawable.ic_menu_more
        )
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                showSplashScreen
            }
            setOnExitAnimationListener { screen ->
                screen.remove()
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemePreferences.initialize(applicationContext)
            val themeMode = ThemePreferences.mode.collectAsStateWithLifecycle().value
            val systemDark = isSystemInDarkTheme()
            RestaurantTheme(darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }) {
                val sessionScope = session.cacheScope.collectAsStateWithLifecycle().value
                NotificationPermissionPrompt(
                    isAuthenticated = sessionScope.isNotBlank() && session.getToken() != null
                )

                val shouldShowBottomNav = remember {
                    mutableStateOf(false)
                }
                val navItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Notification,
                    BottomNavItem.Orders,
                    BottomNavItem.Menu
                )
                val navController = rememberNavController()
                val notificationViewModel: NotificationsViewModel = hiltViewModel()
                val unreadCount = notificationViewModel.unreadCount.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = true) {
                    viewModel.event.collectLatest {
                        when (it) {
                            is HomeViewModel.HomeEvent.NavigateToOrderDetail -> {
                                navController.navigate(OrderDetails(it.orderID)) {
                                    popUpTo(Home) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val currentRoute =
                            navController.currentBackStackEntryAsState().value?.destination
                        AnimatedVisibility(visible = shouldShowBottomNav.value) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 10.dp
                            ) {
                                navItems.forEach { item ->
                                    val selected =
                                        currentRoute?.hierarchy?.any { it.route == item.route::class.qualifiedName } == true

                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(Home) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Box(modifier = Modifier.size(48.dp)) {
                                                Icon(
                                                    painter = painterResource(id = item.icon),
                                                    contentDescription = item::class.simpleName?.removeSuffix("Item") ?: "Navigation item",
                                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.align(Center)
                                                )

                                                if (item.route == Notification && unreadCount.value > 0) {
                                                    ItemCount(unreadCount.value)
                                                }
                                            }
                                        },
                                        label = { Text(item::class.simpleName?.removeSuffix("Item") ?: "") }
                                    )
                                }
                            }
                        }
                    }) { innerPadding ->

                    SharedTransitionLayout {
                        FoodHubNavHost(
                            navController = navController,
                            startDestination = if (session.getToken() != null) Home else AuthScreen,
                            modifier = Modifier.padding(innerPadding),
                        ) {
                            composable<SignUp> {
                                shouldShowBottomNav.value = false
                                SignUpScreen(navController)
                            }
                            composable<AuthScreen> {
                                shouldShowBottomNav.value = false
                                AuthScreen(navController, false)
                            }
                            composable<Login> {
                                shouldShowBottomNav.value = false
                                SignInScreen(navController,false)
                            }
                            composable<Home> {
                                shouldShowBottomNav.value = true
                                HomeScreen(navController)
                            }
                            composable<AppSettings> {
                                shouldShowBottomNav.value = false
                                AppSettingsScreen(navController)
                            }
                            composable<Account> { AccountScreen(navController) }
                            composable<Payouts> { PayoutScreen(navController) }
                            composable<Notification> {
                                SideEffect {
                                    shouldShowBottomNav.value = true
                                }
                                NotificationsList(navController, notificationViewModel)
                            }
                            composable<OrderList> {
                                shouldShowBottomNav.value = true
                                OrderListScreen(navController)
                            }
                            composable<OrderDetails> {
                                shouldShowBottomNav.value = false
                                val orderID = it.toRoute<OrderDetails>().orderID
                                OrderDetailsScreen(orderID, navController)
                            }
                            composable<MenuList> {
                                shouldShowBottomNav.value = true
                                ListMenuItemsScreen(navController, this)
                            }
                            composable<AddMenu> {
                                shouldShowBottomNav.value = false
                                AddMenuItemScreen(navController)
                            }
                            composable<ImagePicker> {
                                shouldShowBottomNav.value = false
                                ImagePickerScreen(navController)
                            }
                        }
                    }

                }
            }
        }

        if (::foodApi.isInitialized) {
            Log.d("MainActivity", "FoodApi initialized")
        }
        showSplashScreen = false
        processIntent(intent, viewModel)
    }
}

@Composable
fun BoxScope.ItemCount(count: Int) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(Mustard)
            .align(Alignment.TopEnd)
    ) {
        Text(
            text = "${count}",
            modifier = Modifier
                .align(Alignment.Center),
            color = Color.White,
            style = TextStyle(fontSize = 10.sp)
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RestaurantTheme {
        Greeting("Android")
    }
}
