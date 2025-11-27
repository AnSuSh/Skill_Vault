package com.quickthought.skillvault.ui.list.components

import android.content.ClipData
import android.content.ClipDescription.EXTRA_IS_SENSITIVE
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle

/**
 * Utility to copy sensitive text to the system clipboard.
 */
fun copyTextToClipboard(context: Context, text: String, label: String = "Password") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)

    // Security Best Practice: Flag the content as sensitive.
    // This prevents Android 13+ from showing the text content in the
    // visual confirmation overlay and tells keyboards not to log it.
    clip.description.extras = PersistableBundle().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            putBoolean(EXTRA_IS_SENSITIVE, true)
        } else {
            // For older versions (API 24-32)
            putBoolean("android.content.extra.IS_SENSITIVE", true)
        }
    }

    clipboard.setPrimaryClip(clip)
}