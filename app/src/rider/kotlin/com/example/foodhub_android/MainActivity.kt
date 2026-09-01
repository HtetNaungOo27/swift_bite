package com.example.foodhub_android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.ui.features.auth.login.SignInScreen
import com.example.foodhub_android.ui.features.notifications.NotificationsList
import com.example.foodhub_android.ui.features.notifications.NotificationsViewModel
import com.example.foodhub_android.ui.feature.deliveries.DeliveriesScreen
import com.example.foodhub_android.ui.feature.orders.RiderOrderDetailsScreen
import com.example.foodhub_android.ui.feature.orders.RiderOrdersScreen
import com.example.foodhub_android.ui.feature.wallet.RiderWalletScreen
import com.example.foodhub_android.ui.components.NotificationPermissionPrompt
import com.example.foodhub_android.ui.FoodHubNavHost
import com.example.foodhub_android.ui.navigation.AuthScreen
import com.example.foodhub_android.ui.navigation.AppSettings
import com.example.foodhub_android.ui.features.settings.AppSettingsScreen
import com.example.foodhub_android.ui.features.account.AccountScreen
import com.example.foodhub_android.ui.navigation.Account
import com.example.foodhub_android.ui.navigation.Payouts
import com.example.foodhub_android.ui.features.payout.PayoutScreen
import com.example.foodhub_android.ui.navigation.Home
import com.example.foodhub_android.ui.navigation.Notification
import com.example.foodhub_android.ui.navigation.RiderActiveOrders
import com.example.foodhub_android.ui.navigation.RiderWallet
import com.example.foodhub_android.ui.navigation.RiderOrderDetails
import com.example.foodhub_android.ui.theme.RiderTheme
import com.example.foodhub_android.ui.theme.ThemeMode
import com.example.foodhub_android.ui.theme.ThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseFoodHubActivity() {
    @Inject lateinit var session: FoodHubSession
    private val homeViewModel: HomeViewModel by viewModels()
    private val notificationViewModel: NotificationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemePreferences.initialize(applicationContext)
            val themeMode = ThemePreferences.mode.collectAsStateWithLifecycle().value
            val systemDark = isSystemInDarkTheme()
            RiderTheme(darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }) {
                val sessionScope = session.cacheScope.collectAsStateWithLifecycle().value
                NotificationPermissionPrompt(
                    isAuthenticated = sessionScope.isNotBlank() && session.getToken() != null
                )
                val navController = rememberNavController()
                val currentDestination = navController.currentBackStackEntryAsState().value?.destination
                val unreadCount = notificationViewModel.unreadCount.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    homeViewModel.event.collectLatest { event ->
                        when (event) {
                            is HomeViewModel.HomeEvent.NavigateToOrderDetail ->
                                navController.navigate(RiderOrderDetails(event.orderID)) {
                                    popUpTo(Home) { inclusive = false }
                                    launchSingleTop = true
                                }
                        }
                    }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val isFullScreenRoute = currentDestination?.hierarchy?.any {
                            it.route == RiderOrderDetails::class.qualifiedName ||
                                it.route == AuthScreen::class.qualifiedName ||
                                it.route == AppSettings::class.qualifiedName
                        } == true
                        if (session.getToken() != null && !isFullScreenRoute && currentDestination != null) {
                            val items = listOf(
                                Triple(Home, R.drawable.ic_home, "Jobs"),
                                Triple(RiderActiveOrders, R.drawable.ic_orders, "Orders"),
                                Triple(RiderWallet, R.drawable.ic_cart, "Wallet"),
                                Triple(Notification, R.drawable.ic_notification, "Alerts")
                            )
                            NavigationBar(
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                                tonalElevation = 10.dp
                            ) {
                                items.forEach { (route, icon, label) ->
                                    val selected = currentDestination?.hierarchy?.any {
                                        it.route == route::class.qualifiedName
                                    } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(Home) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Box(Modifier.size(32.dp)) {
                                                Icon(
                                                    painterResource(icon),
                                                    contentDescription = label,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                                if (route == Notification && unreadCount.value > 0) {
                                                    Badge(Modifier.align(Alignment.TopEnd)) {
                                                        Text(unreadCount.value.coerceAtMost(99).toString())
                                                    }
                                                }
                                            }
                                        },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    FoodHubNavHost(
                        navController = navController,
                        startDestination = if (session.getToken() == null) AuthScreen else Home,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable<AuthScreen> { SignInScreen(navController, isCustomer = false) }
                        composable<Home> { DeliveriesScreen(navController) }
                        composable<AppSettings> { AppSettingsScreen(navController) }
                        composable<Account> { AccountScreen(navController) }
                        composable<Payouts> { PayoutScreen(navController) }
                        composable<RiderActiveOrders> { RiderOrdersScreen(navController) }
                        composable<RiderWallet> { RiderWalletScreen() }
                        composable<Notification> { NotificationsList(navController, notificationViewModel) }
                        composable<RiderOrderDetails> {
                            RiderOrderDetailsScreen(navController)
                        }
                    }
                }
                LaunchedEffect(Unit) { processIntent(intent, homeViewModel) }
            }
        }
    }

}
