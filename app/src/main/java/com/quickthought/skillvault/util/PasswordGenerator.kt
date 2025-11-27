package com.quickthought.skillvault.util

object PasswordGenerator {
    private const val CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+"

    fun generate(length: Int = 16): String {
        return (1..length)
            .map { CHARS.random() }
            .joinToString("")
    }
}