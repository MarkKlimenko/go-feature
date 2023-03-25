package com.go.feature.component.filter.util

fun parseDouble(field: String, value: String?): Double? {
    return try {
        value?.toDouble()
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Value is not compatible with filter; field=${field}, value=${value}")
    }
}