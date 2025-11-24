package com.quickthought.skillvault

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.quickthought.skillvault.di.EncryptionService

class MainViewModel(
    private val encryptionService: EncryptionService,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

}