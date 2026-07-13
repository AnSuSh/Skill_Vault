package com.quickthought.skillvault.ui.about

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.quickthought.skillvault.billing.BillingManager
import com.quickthought.skillvault.util.VaultLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()

    private val _purchaseSuccess = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess.asStateFlow()

    private val _errorFlowBilling = MutableStateFlow<String?>(null)
    val errorFlowBilling: StateFlow<String?> = _errorFlowBilling.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                billingManager.purchaseSuccessFlow.collect {
                    _purchaseSuccess.value = true
                }
            }
            launch {
                billingManager.errorFlow.collect {
                    _errorFlowBilling.value = it
                }
            }
            launch {
                billingManager.isConnectionReady.collect { isReady ->
                    if (isReady) {
                        fetchProducts()
                    }
                }
            }
        }
    }

    fun startBillingConnection() {
        billingManager.startConnection()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            val details = billingManager.getProductDetails()
            VaultLogger.infoLog("Fetched products: $details")
            _productDetails.value = details
        }
    }

    fun buyProduct(activity: Activity, details: ProductDetails) {
        billingManager.launchBillingFlow(activity, details)
    }

    fun resetPurchaseSuccess() {
        _purchaseSuccess.value = false
    }

    override fun onCleared() {
        billingManager.onDestroy()
    }
}
