package com.quickthought.skillvault.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveInfo
import android.service.autofill.SaveRequest
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import android.widget.Toast
import android.widget.inline.InlinePresentationSpec
import com.quickthought.skillvault.R
import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.domain.model.AddressUI
import com.quickthought.skillvault.domain.model.CreditCardUI
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.util.IdentityGenerator
import com.quickthought.skillvault.util.UserPreferences
import com.quickthought.skillvault.util.VaultLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * SkillVaultAutofillService handles autofill requests for passwords, addresses, and credit cards.
 * It provides both automatic form-wide filling and manual backup suggestions for focused fields.
 */
@AndroidEntryPoint
class SkillVaultAutofillService : AutofillService() {

    @Inject
    lateinit var repository: CredentialRepository

    @Inject
    lateinit var identityGenerator: IdentityGenerator

    @Inject
    lateinit var userPreferences: UserPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val contexts = request.fillContexts
        val structure = contexts[contexts.size - 1].structure
        val packageName = structure.activityComponent.packageName
        val isManual = (request.flags and FillRequest.FLAG_MANUAL_REQUEST) != 0
        
        VaultLogger.infoLog("onFillRequest started for package: $packageName, manual=$isManual")

        val fieldData = FieldData()
        traverseStructure(structure, fieldData)

        VaultLogger.infoLog("Traverse finished. Fields found: ${fieldData.summary()}")

        serviceScope.launch {
            try {
                val responseBuilder = FillResponse.Builder()
                var hasData = false
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    responseBuilder.setFlags(FillResponse.FLAG_DISABLE_ACTIVITY_ONLY)
                }

                var inlineSpec: Any? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    inlineSpec = request.inlineSuggestionsRequest?.inlinePresentationSpecs?.getOrNull(0)
                }

                val focusedId = fieldData.focusedId
                val focusedType = fieldData.focusedType
                
                // 1. Credentials (Matching Domain/Package)
                val credentials = repository.getCredentialsForAutofill(fieldData.domain, packageName)
                VaultLogger.infoLog("Found ${credentials.size} matching credentials")
                
                credentials.forEach { credential ->
                    // Standard Form-Wide Suggestion (Fills all known fields)
                    createDataset(credential, fieldData, inlineSpec)?.let {
                        responseBuilder.addDataset(it)
                        hasData = true
                    }
                    
                    // Backup: Focused-Only Suggestions (Fills only the field the user clicked)
                    if (focusedId != null) {
                        responseBuilder.addDataset(createManualFieldDataset(
                            label = "Fill Email: ${credential.username}",
                            value = credential.username,
                            targetId = focusedId
                        ))
                        val pass = repository.getDecryptedPassword(credential.credentialId)
                        responseBuilder.addDataset(createManualFieldDataset(
                            label = "Fill Password",
                            value = pass,
                            targetId = focusedId
                        ))
                        hasData = true
                    }
                }

                // 2. Universal Manual Fallbacks (If matching found OR user requested via long-press)
                if (focusedId != null && (isManual || credentials.isNotEmpty())) {
                    VaultLogger.infoLog("Adding manual backup suggestions")
                    val presetName = withTimeoutOrNull(500) { userPreferences.presetName.first() } ?: ""
                    
                    // Generate Username Suggestion
                    val genUser = identityGenerator.generateUsername(presetName)
                    responseBuilder.addDataset(createManualFieldDataset("Generate Username ($genUser)", genUser, focusedId))
                    
                    // Generate Password Suggestion
                    val genPass = identityGenerator.generatePassword()
                    responseBuilder.addDataset(createManualFieldDataset("Generate Password", genPass, focusedId))
                    hasData = true

                    // Emails as individual backups
                    val emails = withTimeoutOrNull(500) { repository.getEmails().first() } ?: emptyList()
                    emails.forEach { email ->
                        responseBuilder.addDataset(createManualFieldDataset("Fill Email: ${email.email}", email.email, focusedId))
                        hasData = true
                    }
                }

                // 3. Contextual Generation (Standard flow)
                if (packageName != this@SkillVaultAutofillService.packageName && !isManual) {
                    val presetName = withTimeoutOrNull(500) { userPreferences.presetName.first() } ?: ""
                    
                    if (focusedType == FieldType.USERNAME || focusedType == FieldType.EMAIL) {
                        val username = identityGenerator.generateUsername(presetName)
                        val password = identityGenerator.generatePassword() // Generate both for comprehensive fill
                        responseBuilder.addDataset(createGenerationDataset(
                            label = getString(R.string.autofill_generate_username, username),
                            username = username,
                            password = password,
                            fieldData = fieldData
                        ))
                        hasData = true
                    }

                    if (focusedType == FieldType.PASSWORD) {
                        val username = identityGenerator.generateUsername(presetName)
                        val password = identityGenerator.generatePassword()
                        responseBuilder.addDataset(createGenerationDataset(
                            label = getString(R.string.autofill_generate_password, password),
                            username = username,
                            password = password,
                            fieldData = fieldData
                        ))
                        hasData = true
                    }
                }
                
                // 4. Addresses & Cards
                if (focusedType == FieldType.ADDRESS) {
                    val addresses = withTimeoutOrNull(500) { repository.getAddresses().first() } ?: emptyList()
                    addresses.forEach { address ->
                        responseBuilder.addDataset(createAddressDataset(address, fieldData, inlineSpec))
                        hasData = true
                    }
                }
                
                if (focusedType == FieldType.CARD) {
                    val cards = withTimeoutOrNull(500) { repository.getCreditCards().first() } ?: emptyList()
                    cards.forEach { card ->
                        responseBuilder.addDataset(createCreditCardDataset(card, fieldData, inlineSpec))
                        hasData = true
                    }
                }

                if (setupSaveInfo(fieldData, responseBuilder)) {
                    hasData = true
                }

                if (hasData) {
                    val response = responseBuilder.build()
                    VaultLogger.infoLog("onFillRequest success")
                    callback.onSuccess(response)
                } else {
                    VaultLogger.infoLog("No data found to fill, returning null")
                    callback.onSuccess(null)
                }
            } catch (e: Exception) {
                VaultLogger.errorLog("Autofill error: ${e.message}", e)
                callback.onFailure(e.message)
            }
        }
    }

    private fun setupSaveInfo(fieldData: FieldData, responseBuilder: FillResponse.Builder): Boolean {
        val saveTypes = mutableListOf<Int>()
        val saveIds = mutableListOf<AutofillId>()

        if (fieldData.usernameId != null || fieldData.passwordId != null) {
            saveTypes.add(SaveInfo.SAVE_DATA_TYPE_PASSWORD)
            fieldData.usernameId?.let { saveIds.add(it) }
            fieldData.passwordId?.let { saveIds.add(it) }
        }

        if (fieldData.hasAddressFields()) {
            saveTypes.add(SaveInfo.SAVE_DATA_TYPE_ADDRESS)
            saveIds.addAll(fieldData.getAddressIds())
        }

        if (fieldData.cardNumberId != null) {
            saveTypes.add(SaveInfo.SAVE_DATA_TYPE_CREDIT_CARD)
            saveIds.addAll(fieldData.getCreditCardIds())
        }

        return if (saveTypes.isNotEmpty()) {
            val typeMask = saveTypes.reduce { acc, type -> acc or type }
            responseBuilder.setSaveInfo(SaveInfo.Builder(typeMask, saveIds.toTypedArray()).build())
            true
        } else {
            false
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val contexts = request.fillContexts
        val structure = contexts[contexts.size - 1].structure
        val packageName = structure.activityComponent.packageName
        val fieldData = FieldData()
        traverseStructure(structure, fieldData)

        serviceScope.launch {
            try {
                if (!fieldData.usernameValue.isNullOrBlank() && !fieldData.passwordValue.isNullOrBlank()) {
                    repository.saveCredential(
                        CredentialItemUI(0, fieldData.domain ?: packageName, fieldData.usernameValue!!, fieldData.domain, packageName),
                        fieldData.passwordValue!!
                    )
                    Toast.makeText(this@SkillVaultAutofillService, R.string.autofill_save_success, Toast.LENGTH_SHORT).show()
                }
                callback.onSuccess()
            } catch (e: Exception) {
                callback.onFailure(e.message)
            }
        }
    }

    private fun traverseStructure(structure: AssistStructure, fieldData: FieldData) {
        for (i in 0 until structure.windowNodeCount) {
            traverseNode(structure.getWindowNodeAt(i).rootViewNode, fieldData)
        }
    }

    private fun traverseNode(node: AssistStructure.ViewNode, fieldData: FieldData) {
        if (node.visibility != View.VISIBLE) {
            for (i in 0 until node.childCount) traverseNode(node.getChildAt(i), fieldData)
            return
        }

        val hints = node.autofillHints ?: emptyArray()
        val id = node.autofillId
        val idEntry = node.idEntry ?: ""
        val text = node.text?.toString() ?: ""
        val value = node.autofillValue?.textValue?.toString() ?: text
        
        if (idEntry.contains("stub", ignoreCase = true)) {
            for (i in 0 until node.childCount) traverseNode(node.getChildAt(i), fieldData)
            return
        }

        if (node.webDomain != null) fieldData.domain = node.webDomain

        val isInput = node.autofillType != View.AUTOFILL_TYPE_NONE || 
                      node.className?.contains("EditText", true) == true || 
                      node.className?.contains("WebView", true) == true

        if (isInput) {
            val type = determineFieldType(hints, idEntry)
            if (type != FieldType.NONE) {
                if (node.isFocused) {
                    fieldData.focusedId = id
                    fieldData.focusedType = type
                    VaultLogger.infoLog("Focused node identified: idEntry=$idEntry, type=$type")
                }
                
                when (type) {
                    FieldType.USERNAME, FieldType.EMAIL -> {
                        if (fieldData.usernameId == null) { fieldData.usernameId = id; fieldData.usernameValue = value }
                    }
                    FieldType.PASSWORD -> {
                        if (fieldData.passwordId == null) { fieldData.passwordId = id; fieldData.passwordValue = value }
                    }
                    FieldType.ADDRESS -> {
                        if (fieldData.streetId == null) { fieldData.streetId = id; fieldData.streetValue = value }
                    }
                    FieldType.CARD -> {
                        if (fieldData.cardNumberId == null) { fieldData.cardNumberId = id; fieldData.cardNumberValue = value }
                    }
                    else -> {}
                }
            } else if (node.isFocused) {
                fieldData.focusedId = id
                fieldData.focusedType = FieldType.NONE
                VaultLogger.infoLog("Focused generic input identified: idEntry=$idEntry")
            }
        }
        
        for (i in 0 until node.childCount) traverseNode(node.getChildAt(i), fieldData)
    }

    private fun determineFieldType(hints: Array<String>, idEntry: String): FieldType {
        return when {
            hints.any { it.contains("password") } || idEntry.contains("password", true) || idEntry.contains("passwd", true) -> FieldType.PASSWORD
            hints.any { it.contains("emailAddress") } || idEntry.contains("email", true) -> FieldType.EMAIL
            hints.any { it.contains("username") } || idEntry.contains("username", true) || idEntry.contains("login", true) -> FieldType.USERNAME
            hints.any { it.contains("postalAddress") || it.contains("streetAddress") } || idEntry.contains("address", true) -> FieldType.ADDRESS
            hints.any { it.contains("creditCardNumber") } || idEntry.contains("card", true) && idEntry.contains("number", true) -> FieldType.CARD
            else -> FieldType.NONE
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun createDataset(
        credential: CredentialItemUI, 
        fieldData: FieldData,
        spec: Any?
    ): Dataset? {
        val presentation = createPresentation("${credential.accountName} (${credential.username})")
        val builder = Dataset.Builder(presentation)
        
        var added = false
        fieldData.usernameId?.let { 
            builder.setValue(it, AutofillValue.forText(credential.username))
            added = true
        }
        fieldData.passwordId?.let {
            val pass = repository.getDecryptedPassword(credential.credentialId)
            builder.setValue(it, AutofillValue.forText(pass))
            added = true
        }
        
        return if (added) builder.build() else null
    }

    private fun createManualFieldDataset(label: String, value: String, targetId: AutofillId): Dataset {
        return Dataset.Builder(createPresentation(label))
            .setValue(targetId, AutofillValue.forText(value))
            .build()
    }

    @Suppress("DEPRECATION")
    private fun createGenerationDataset(
        label: String,
        username: String,
        password: String,
        fieldData: FieldData
    ): Dataset {
        val presentation = createPresentation(label)
        val builder = Dataset.Builder(presentation)
        
        fieldData.usernameId?.let { builder.setValue(it, AutofillValue.forText(username)) }
        fieldData.passwordId?.let { builder.setValue(it, AutofillValue.forText(password)) }
        
        return builder.build()
    }

    @Suppress("DEPRECATION")
    private fun createAddressDataset(address: AddressUI, fieldData: FieldData, spec: Any?): Dataset {
        val presentation = createPresentation("${address.label}: ${address.fullName}")
        val builder = Dataset.Builder(presentation)
        fieldData.streetId?.let { builder.setValue(it, AutofillValue.forText(address.street)) }
        return builder.build()
    }

    @Suppress("DEPRECATION")
    private suspend fun createCreditCardDataset(card: CreditCardUI, fieldData: FieldData, spec: Any?): Dataset {
        val presentation = createPresentation("${card.brand ?: "Card"}: ${card.maskedCardNumber}")
        val builder = Dataset.Builder(presentation)
        val (number, cvv) = repository.getDecryptedCardDetails(card.id)
        fieldData.cardNumberId?.let { builder.setValue(it, AutofillValue.forText(number)) }
        return builder.build()
    }

    private fun createPresentation(text: String): RemoteViews {
        val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        presentation.setTextViewText(android.R.id.text1, text)
        return presentation
    }

    private enum class FieldType { NONE, USERNAME, EMAIL, PASSWORD, ADDRESS, CARD }

    private class FieldData {
        var domain: String? = null
        var focusedId: AutofillId? = null
        var focusedType: FieldType = FieldType.NONE
        
        var usernameId: AutofillId? = null; var usernameValue: String? = null
        var passwordId: AutofillId? = null; var passwordValue: String? = null
        var streetId: AutofillId? = null; var streetValue: String? = null
        var cardNumberId: AutofillId? = null; var cardNumberValue: String? = null

        fun hasAddressFields() = streetId != null
        fun getAddressIds() = listOfNotNull(streetId)
        fun getCreditCardIds() = listOfNotNull(cardNumberId)
        
        fun summary(): String {
            return "focused=$focusedType, user=${usernameId!=null}, pass=${passwordId!=null}, domain=$domain"
        }
    }
}
