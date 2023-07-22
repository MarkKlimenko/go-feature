package com.go.feature.util.exception.client

open class LocalizedException(
    message: String,
    val values: Array<out String>?,
    val valuesMap: Map<String, String>?
) : RuntimeException(message)