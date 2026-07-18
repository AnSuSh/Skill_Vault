package com.quickthought.skillvault.autofill

import android.app.assist.AssistStructure
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.util.VaultLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * More  -https://developer.android.com/identity/autofill/autofill-services
 * */
@AndroidEntryPoint
class SkillVaultAutofillService : AutofillService() {

    @Inject
    lateinit var repository: CredentialRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val contexts = request.fillContexts
        val structure = contexts[contexts.size - 1].structure

        val packageName = structure.activityComponent.packageName
        val fieldData = FieldData()
        traverseStructure(structure, fieldData)

        VaultLogger.infoLog("Autofill request for domain: ${fieldData.domain}, package: $packageName")

        serviceScope.launch {
            try {
                val credentials =
                    repository.getCredentialsForAutofill(fieldData.domain, packageName)
                if (credentials.isEmpty()) {
                    callback.onSuccess(null)
                    return@launch
                }

                val responseBuilder = FillResponse.Builder()

                credentials.forEach { credential ->
                    val dataset = createDataset(credential, fieldData)
                    if (dataset != null) {
                        responseBuilder.addDataset(dataset)
                    }
                }

                callback.onSuccess(responseBuilder.build())
            } catch (e: Exception) {
                VaultLogger.errorLog("Error during autofill: ${e.message}")
                callback.onFailure(e.message)
            }
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        callback.onSuccess()
    }

    private fun traverseStructure(structure: AssistStructure, fieldData: FieldData) {
        val nodes = structure.windowNodeCount
        for (i in 0 until nodes) {
            val node = structure.getWindowNodeAt(i).rootViewNode
            traverseNode(node, fieldData)
        }
    }

    private fun traverseNode(node: AssistStructure.ViewNode, fieldData: FieldData) {
        val hints = node.autofillHints
        if (hints != null) {
            for (hint in hints) {
                when (hint.lowercase()) {
                    View.AUTOFILL_HINT_USERNAME.lowercase(), "username", "email" -> {
                        fieldData.usernameId = node.autofillId
                    }

                    View.AUTOFILL_HINT_PASSWORD.lowercase(), "password" -> {
                        fieldData.passwordId = node.autofillId
                    }
                }
            }
        }

        // Web domain detection
        if (node.webDomain != null) {
            fieldData.domain = node.webDomain
        }

        for (i in 0 until node.childCount) {
            traverseNode(node.getChildAt(i), fieldData)
        }
    }

    private suspend fun createDataset(
        credential: CredentialItemUI,
        fieldData: FieldData
    ): Dataset? {
        val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        presentation.setTextViewText(
            android.R.id.text1,
            "${credential.accountName} (${credential.username})"
        )

        val datasetBuilder = Dataset.Builder(presentation)
        var added = false

        fieldData.usernameId?.let {
            datasetBuilder.setValue(it, AutofillValue.forText(credential.username))
            added = true
        }

        fieldData.passwordId?.let {
            try {
                val password = repository.getDecryptedPassword(credential.credentialId)
                datasetBuilder.setValue(it, AutofillValue.forText(password))
                added = true
            } catch (e: Exception) {
                VaultLogger.errorLog("Failed to decrypt password for autofill: ${e.message}")
            }
        }

        return if (added) datasetBuilder.build() else null
    }

    private class FieldData {
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null
        var domain: String? = null
    }
}
