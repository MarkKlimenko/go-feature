package com.go.feature.controller.dto.namespace

data class NamespaceEditRequest(
    val name: String,
    val status: NamespaceStatus
)
