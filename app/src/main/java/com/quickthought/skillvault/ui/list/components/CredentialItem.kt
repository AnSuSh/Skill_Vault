package com.quickthought.skillvault.ui.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.theme.SkillVaultTheme

@Composable
fun CredentialItem(
    credential: CredentialItemUI,
    onClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth(),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Credential Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Copy Button (Triggers Biometric Auth)
                IconButton(onClick = onCopyClick) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy Password",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error // Make it red!
                    )
                }
            }
        },
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = credential.accountName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = credential.username,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CredentialItemPreview() {
    SkillVaultTheme {
        CredentialItem(
            credential = CredentialItemUI(
                credentialId = 1,
                accountName = "Google",
                username = "user@gmail.com",
            ),
            onClick = {},
            onCopyClick = {},
            onDeleteClick = {}
        )
    }
}