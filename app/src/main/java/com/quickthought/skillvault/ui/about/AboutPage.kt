package com.quickthought.skillvault.ui.about

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.quickthought.skillvault.R
import com.quickthought.skillvault.ui.list.components.Footer
import com.quickthought.skillvault.util.VaultLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    viewModel: AboutViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val packageName = context.packageName
    val playStoreUrl = "https://play.google.com/store/apps/details?id=$packageName"

    var showSupportOptions by remember { mutableStateOf(false) }
    val productDetails by viewModel.productDetails.collectAsState()
    val purchaseSuccess by viewModel.purchaseSuccess.collectAsState()
    val billingErrors by viewModel.errorFlowBilling.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    val feedbackText by viewModel.feedbackText.collectAsState()
    val isSubmittingFeedback by viewModel.isSubmittingFeedback.collectAsState()
    val feedbackSubmissionSuccess by viewModel.feedbackSubmissionSuccess.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startBillingConnection()
    }

    if (purchaseSuccess) {
        ThankYouScreen(onDismiss = {
            viewModel.resetPurchaseSuccess()
        })
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("About Us") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    AboutActionCard(
                        title = "Share App",
                        subtitle = "Spread the word to friends and family",
                        icon = Icons.Default.Share,
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Securely manage your credentials with Skill Vault! Download now: $playStoreUrl"
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Share Skill Vault"
                                )
                            )
                        }
                    )

                    AboutActionCard(
                        title = "Rate Us",
                        subtitle = "Love the app? Let us know on Play Store",
                        icon = Icons.Default.Star,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, playStoreUrl.toUri())
                            context.startActivity(intent)
                        }
                    )

                    AboutActionCard(
                        title = "Support Us",
                        subtitle = "Help us keep the app free and secure",
                        icon = Icons.Default.CardGiftcard,
                        onClick = {
                            showSupportOptions = true
                        }
                    )

                    AboutActionCard(
                        title = "Check For Updates",
                        subtitle = "Check for new features and improvements",
                        icon = Icons.Default.Update,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, playStoreUrl.toUri())
                            context.startActivity(intent)
                        }
                    )

                    FeedbackSection(
                        text = feedbackText,
                        isSubmitting = isSubmittingFeedback,
                        submissionSuccess = feedbackSubmissionSuccess,
                        onTextChanged = { viewModel.onFeedbackTextChanged(it) },
                        onSubmit = { viewModel.submitFeedback() },
                        onResetStatus = { viewModel.resetFeedbackStatus() }
                    )

                    Footer()
                }
            }
        }

        if (showSupportOptions) {
            ModalBottomSheet(
                onDismissRequest = { showSupportOptions = false },
                sheetState = sheetState
            ) {
                SupportOptionsContent(
                    productDetails = productDetails,
                    onSupportClick = { details ->
                        showSupportOptions = false
                        val activity = context as? Activity
                        if (activity != null) {
                            viewModel.buyProduct(activity, details)
                        }
                    }
                )
            }
        }

        billingErrors?.let { error ->
            VaultLogger.errorLog("Billing error: $error")
        }
    }
}

@Composable
fun FeedbackSection(
    text: String,
    isSubmitting: Boolean,
    submissionSuccess: Boolean?,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onResetStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Feedback,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.feedback_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (submissionSuccess == true) {
                Text(
                    text = stringResource(R.string.feedback_success),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                TextButton(
                    onClick = onResetStatus,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Send More")
                }
            } else {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.feedback_hint)) },
                    enabled = !isSubmitting,
                    maxLines = 5
                )

                if (submissionSuccess == false) {
                    Text(
                        text = stringResource(R.string.feedback_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = onSubmit,
                    enabled = text.isNotBlank() && !isSubmitting,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.feedback_submit))
                    }
                }
            }
        }
    }
}

@Composable
fun SupportOptionsContent(
    productDetails: List<ProductDetails>,
    onSupportClick: (ProductDetails) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Support Skill Vault",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose a tier to help us maintain and improve the app. Your support means the world to us!",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (productDetails.isEmpty()) {
            Text(
                text = "Loading support tiers...",
                modifier = Modifier.padding(vertical = 32.dp),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            productDetails.forEach { details ->
                val offer = details.oneTimePurchaseOfferDetails
                val price = offer?.formattedPrice ?: ""

                AboutActionCard(
                    title = details.name,
                    subtitle = details.description,
                    icon = Icons.Default.CardGiftcard,
                    onClick = { onSupportClick(details) },
                    trailingText = price
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    trailingText: String? = null
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
