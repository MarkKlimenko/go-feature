package com.go.feature.util.exception.client

class ClientException : LocalizedException {
    constructor(message: String) : super(
        message = message,
        values = null,
        valuesMap = null
    )

    constructor(message: String, vararg values: String) : super(
        message = message,
        values = values,
        valuesMap = null
    )

    constructor(message: String, valuesMap: Map<String, String>) : super(
        message = message,
        values = null,
        valuesMap = valuesMap
    )
}