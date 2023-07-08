package com.go.feature.controller.exception

data class ErrorResponse(
    val message: String?,
    val traceId: String?,
    val validations: Map<String, String?>?
)
