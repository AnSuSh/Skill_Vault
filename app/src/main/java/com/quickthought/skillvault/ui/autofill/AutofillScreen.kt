package com.quickthought.skillvault.ui.autofill

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickthought.skillvault.domain.model.AddressUI
import com.quickthought.skillvault.domain.model.CreditCardUI
import com.quickthought.skillvault.ui.autofill.components.AddressItem
import com.quickthought.skillvault.ui.autofill.components.CreditCardItem
import com.quickthought.skillvault.ui.autofill.components.EmailItem
import com.quickthought.skillvault.util.VaultLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutofillScreen(
    viewModel: AutofillViewModel = hiltViewModel()
) {
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val cards by viewModel.creditCards.collectAsStateWithLifecycle()
    val emails by viewModel.emails.collectAsStateWithLifecycle()
    val presetName by viewModel.presetName.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var nameInput by remember { mutableStateOf(presetName) }
    LaunchedEffect(presetName) { nameInput = presetName }

    // Dialog states
    var showAddressDialog by remember { mutableStateOf(false) }
    var showCardDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Autofill Center") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text("Identity Settings", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Username Preset") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = {
                            VaultLogger.infoLog("Save preset button clicked")
                            viewModel.updatePresetName(nameInput)
                            Toast.makeText(context, "Preset updated", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Save")
                        }
                    }
                )
                Text(
                    text = "This name will be used as a base to generate randomized usernames during autofill.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Emails Section
            item {
                SectionHeader("Email Addresses", onAdd = {
                    VaultLogger.infoLog("Add email clicked")
                    showEmailDialog = true
                })
            }
            items(emails) { email ->
                EmailItem(email.email, onDelete = {
                    VaultLogger.infoLog("Delete email clicked: ${email.email}")
                    viewModel.deleteEmail(email)
                })
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Addresses Section
            item {
                SectionHeader("Addresses", onAdd = {
                    VaultLogger.infoLog("Add address clicked")
                    showAddressDialog = true
                })
            }
            items(addresses) { address ->
                AddressItem(address, onDelete = {
                    VaultLogger.infoLog("Delete address clicked: ${address.label}")
                    viewModel.deleteAddress(address.id)
                })
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Cards Section
            item {
                SectionHeader("Payment Cards", onAdd = {
                    VaultLogger.infoLog("Add credit card clicked")
                    showCardDialog = true
                })
            }
            items(cards) { card ->
                CreditCardItem(card, onDelete = {
                    VaultLogger.infoLog("Delete card clicked: ${card.label}")
                    viewModel.deleteCard(card.id)
                })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showEmailDialog) {
        AddEmailDialog(
            onDismiss = { showEmailDialog = false },
            onConfirm = { email ->
                viewModel.saveEmail(email)
                showEmailDialog = false
            }
        )
    }

    if (showAddressDialog) {
        AddAddressDialog(
            onDismiss = { showAddressDialog = false },
            onConfirm = { address ->
                viewModel.saveAddress(address)
                showAddressDialog = false
            }
        )
    }

    if (showCardDialog) {
        AddCreditCardDialog(
            onDismiss = { showCardDialog = false },
            onConfirm = { card, number, cvv ->
                viewModel.saveCreditCard(card, number, cvv)
                showCardDialog = false
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, onAdd: () -> Unit, addLabel: String = "Add") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onAdd) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(addLabel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmailDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Email Address") },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (email.isNotBlank()) onConfirm(email) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressDialog(onDismiss: () -> Unit, onConfirm: (AddressUI) -> Unit) {
    var label by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Address") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (e.g. Home)") })
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") })
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street") })
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") })
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") })
                OutlinedTextField(value = zip, onValueChange = { zip = it }, label = { Text("Zip Code") })
                OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(AddressUI(0, label, fullName, street, city, state, zip, country))
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCardDialog(onDismiss: () -> Unit, onConfirm: (CreditCardUI, String, String) -> Unit) {
    var label by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Credit Card") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (e.g. Work Visa)") })
                OutlinedTextField(value = holder, onValueChange = { holder = it }, label = { Text("Cardholder Name") })
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Card Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Row {
                    OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("MM") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("YYYY") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                OutlinedTextField(value = cvv, onValueChange = { cvv = it }, label = { Text("CVV") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand (Optional)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(CreditCardUI(0, label, holder, "****", month, year, brand), number, cvv)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
