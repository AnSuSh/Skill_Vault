package com.quickthought.skillvault.domain.model

import com.quickthought.skillvault.data.local.AddressEntity

data class AddressUI(
    val id: Int,
    val label: String,
    val fullName: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val email: String? = null,
    val phone: String? = null
)

fun AddressEntity.toUIModel(): AddressUI {
    return AddressUI(
        id = id,
        label = label,
        fullName = fullName,
        street = street,
        city = city,
        state = state,
        zipCode = zipCode,
        country = country,
        email = email,
        phone = phone
    )
}

fun AddressUI.toEntity(): AddressEntity {
    return AddressEntity(
        id = id,
        label = label,
        fullName = fullName,
        street = street,
        city = city,
        state = state,
        zipCode = zipCode,
        country = country,
        email = email,
        phone = phone
    )
}
