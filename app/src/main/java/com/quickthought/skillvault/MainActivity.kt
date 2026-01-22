package com.quickthought.skillvault

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.quickthought.skillvault.ui.list.CredentialListScreen
import com.quickthought.skillvault.ui.list.CredentialListViewModel
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
            SkillVaultTheme { CredentialListScreen(viewModel) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.getStringExtra("shortcut_action")
        if (action != null) {
            viewModel.handleShortcutAction(action)
        }
    }
}