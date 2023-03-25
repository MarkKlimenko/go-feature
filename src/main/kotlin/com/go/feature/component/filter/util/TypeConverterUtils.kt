package com.go.feature.component.filter.util

fun parseLong(field: String, value: String?): Long? {
    return try {
        value?.toLong()
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Value is not compatible with filter; field=${field}, value=${value}")
    }
}