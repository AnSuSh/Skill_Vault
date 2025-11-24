package com.quickthought.skillvault.ui.list

import com.quickthought.skillvault.data.local.CredentialEntity

sealed class UIState {
    object Loading : UIState()
    data class Success(val credentials: List<CredentialEntity>) : UIState()
    data class Error(val message: String) : UIState()
}