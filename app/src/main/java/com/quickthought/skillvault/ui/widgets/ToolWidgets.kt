package com.quickthought.skillvault.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quickthought.skillvault.R

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.button_cancel))
            }
        },
        title = { Text(title) },
        text = { Text(text) }
    )
}

@Composable
fun AutofillPromptDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onNeverAskAgain: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(stringResource(R.string.autofill_prompt_button_settings))
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = { onDismiss() }) {
                    Text(stringResource(R.string.autofill_prompt_button_not_now))
                }
                TextButton(onClick = { onNeverAskAgain() }) {
                    Text(stringResource(R.string.autofill_prompt_button_manual))
                }
            }
        },
        title = { Text(title) },
        text = { Text(text) }
    )
}
