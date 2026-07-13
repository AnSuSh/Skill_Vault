package com.quickthought.skillvault.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Password
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Vault : Screen("vault", "Vault", Icons.AutoMirrored.Filled.List)
    object Generator : Screen("generator", "Generator", Icons.Default.Password)
    object About : Screen("about", "About Us", Icons.Default.Info)
}