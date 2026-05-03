package com.quickthought.skillvault.ui.list.components

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.quickthought.skillvault.R

/**
 * Helper class to manage the BiometricPrompt flow outside of the Composable.
 * Requires the activity context to run.
 */
class BiometricAuthenticator(private val context: Context) {

    private val executor = ContextCompat.getMainExecutor(context)
    private val biometricManager = BiometricManager.from(context)

    // Check if biometric authentication is available
    private fun canAuthenticate(): Boolean {
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun prompt(
        title: String = context.getString(R.string.verify_identity),
        subtitle: String = context.getString(R.string.authenticate_to_reveal_password),
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        if (!canAuthenticate()) {
            onFailure() // Fail immediately if not supported
            return
        }

        // We must retrieve the FragmentActivity since BiometricPrompt needs the FragmentManager
        val activity = context as? FragmentActivity ?: run {
            onFailure()
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            // Allows the user to fall back to screen PIN/Pattern/Password if biometrics fail
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailure()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}