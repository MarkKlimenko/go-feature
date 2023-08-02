package com.markklim.feature.util.exception.client

open class LocalizedException(
    message: String,
    val values: Array<out String>?,
    val valuesMap: Map<String, Any>?
) : RuntimeException(message)