package com.quickthought.skillvault

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quickthought.skillvault.ui.Screen
import com.quickthought.skillvault.ui.autofill.AutofillScreen
import com.quickthought.skillvault.ui.generator.PasswordGeneratorScreen
import com.quickthought.skillvault.ui.list.CredentialListScreen
import com.quickthought.skillvault.ui.list.CredentialListViewModel
import com.quickthought.skillvault.ui.about.AboutScreen
import com.quickthought.skillvault.ui.theme.SkillVaultTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: CredentialListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        // Optional: Keep the splash screen on screen longer if you're
        // loading data or checking authentication
        /*
        splashScreen.setKeepOnScreenCondition {
            viewModel.isAppInitializing.value
        }
        */

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Handle shortcut intent
        handleIntent(intent)

        // Prevents screenshots and masks the app in the recent apps switcher
        // Only apply the secure flag if the app is NOT in debug mode
        if (!BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        setContent {
            SkillVaultTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf(Screen.Vault, Screen.Autofill, Screen.Generator)

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Vault.route,
                        modifier = Modifier
                            .padding(bottom = innerPadding.calculateBottomPadding())
                            .consumeWindowInsets(WindowInsets.navigationBars), // Only consume bottom bar insets
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it }, // Slide in from the right
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it }, // Slide out to the left
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -it }, // Slide in from the left
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it }, // Slide out to the right
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        composable(Screen.Vault.route) {
                            CredentialListScreen(
                                viewModel,
                                onAboutClick = { navController.navigate(Screen.About.route) }
                            )
                        }
                        composable(Screen.Autofill.route) { AutofillScreen() }
                        composable(Screen.Generator.route) { PasswordGeneratorScreen() }
                        composable(Screen.About.route) { AboutScreen(onBack = { navController.popBackStack() }) }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Check autofill status whenever the user returns to the app
        viewModel.processAction(com.quickthought.skillvault.ui.list.CredentialListContract.ViewAction.CheckAutofillService)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.getStringExtra("shortcut_action")
        if (action != null) {
            viewModel.handleShortcutAction(action)
        }
    }
}