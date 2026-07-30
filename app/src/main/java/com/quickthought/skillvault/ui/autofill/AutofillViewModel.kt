package com.quickthought.skillvault.ui.autofill

import android.accounts.AccountManager
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.data.local.EmailEntity
import com.quickthought.skillvault.domain.model.AddressUI
import com.quickthought.skillvault.domain.model.CreditCardUI
import com.quickthought.skillvault.util.UserPreferences
import com.quickthought.skillvault.util.VaultLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutofillViewModel @Inject constructor(
    private val repository: CredentialRepository,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val addresses: StateFlow<List<AddressUI>> = repository.getAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creditCards: StateFlow<List<CreditCardUI>> = repository.getCreditCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emails: StateFlow<List<EmailEntity>> = repository.getEmails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presetName: StateFlow<String> = userPreferences.presetName
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updatePresetName(name: String) {
        VaultLogger.infoLog("Updating preset name to: $name")
        viewModelScope.launch {
            userPreferences.updatePresetName(name)
        }
    }

    fun saveEmail(email: String) {
        VaultLogger.infoLog("Saving manual email: $email")
        viewModelScope.launch {
            try {
                repository.saveEmail(EmailEntity(email = email, isSystemAccount = false))
                VaultLogger.infoLog("Successfully saved email")
            } catch (e: Exception) {
                VaultLogger.errorLog("Failed to save email", e)
            }
        }
    }

    fun saveAddress(address: AddressUI) {
        VaultLogger.infoLog("Saving manual address: ${address.label}")
        viewModelScope.launch {
            try {
                repository.saveAddress(address)
                VaultLogger.infoLog("Successfully saved address")
            } catch (e: Exception) {
                VaultLogger.errorLog("Failed to save address", e)
            }
        }
    }

    fun saveCreditCard(card: CreditCardUI, number: String, cvv: String) {
        VaultLogger.infoLog("Saving manual credit card: ${card.label}")
        viewModelScope.launch {
            try {
                repository.saveCreditCard(card, number, cvv)
                VaultLogger.infoLog("Successfully saved credit card")
            } catch (e: Exception) {
                VaultLogger.errorLog("Failed to save credit card", e)
            }
        }
    }

    fun deleteEmail(email: EmailEntity) {
        VaultLogger.infoLog("Deleting email: ${email.email}")
        viewModelScope.launch {
            repository.deleteEmail(email)
        }
    }

    fun deleteAddress(id: Int) {
        VaultLogger.infoLog("Deleting address with ID: $id")
        viewModelScope.launch {
            repository.deleteAddress(id)
        }
    }

    fun deleteCard(id: Int) {
        VaultLogger.infoLog("Deleting card with ID: $id")
        viewModelScope.launch {
            repository.deleteCreditCard(id)
        }
    }
}
