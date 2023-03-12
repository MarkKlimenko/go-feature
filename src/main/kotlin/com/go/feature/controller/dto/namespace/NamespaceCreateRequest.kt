package com.go.feature.controller.dto.namespace

data class NamespaceCreateRequest(
    val name: String,
    val status: NamespaceStatus
)
