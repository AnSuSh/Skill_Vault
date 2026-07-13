package com.quickthought.skillvault.ui.about

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.quickthought.skillvault.billing.BillingManager
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

    init {
        viewModelScope.launch {
            billingManager.purchaseSuccessFlow.collect {
                _purchaseSuccess.value = true
            }
        }
    }

    fun startBillingConnection() {
        billingManager.startConnection()
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            val details = billingManager.getProductDetails()
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
