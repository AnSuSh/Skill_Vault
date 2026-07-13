package com.quickthought.skillvault.util

import timber.log.Timber

object VaultLogger {

    fun infoLog(message: String, vararg args: Any?) {
        Timber.i("$message args: ${args.contentToString()}")
    }

    fun debugLog(message: String, vararg args: Any?) {
        Timber.d("$message args: ${args.contentToString()}")
    }

    fun errorLog(message: String, vararg args: Any?) {
        Timber.e("$message args: ${args.contentToString()}")
    }
}