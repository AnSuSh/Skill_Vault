package com.quickthought.skillvault.ui.autofill.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quickthought.skillvault.domain.model.AddressUI
import com.quickthought.skillvault.domain.model.CreditCardUI

@Composable
fun AddressItem(address: AddressUI, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(address.label, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            Text(address.fullName)
            Text(address.street)
            Text("${address.city}, ${address.state} ${address.zipCode}")
            Text(address.country)
        }
    }
}

@Composable
fun CreditCardItem(card: CreditCardUI, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(card.label, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            Text(card.cardholderName)
            Text(card.maskedCardNumber)
            Text("Expires: ${card.expiryMonth}/${card.expiryYear}")
            card.brand?.let { Text(it) }
        }
    }
}

@Composable
fun EmailItem(email: String, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(email)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
            }
        }
    }
}
