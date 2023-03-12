package com.go.feature.controller.dto.namespace

data class NamespaceResponse(
    val id: String,
    val name: String,
    val status: NamespaceStatus
)
