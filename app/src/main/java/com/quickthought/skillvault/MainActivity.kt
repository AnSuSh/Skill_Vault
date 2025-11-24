package com.quickthought.skillvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.quickthought.skillvault.ui.list.CredentialListScreen
import com.quickthought.skillvault.ui.theme.SkillVaultTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkillVaultTheme { CredentialListScreen() }
        }
    }
}