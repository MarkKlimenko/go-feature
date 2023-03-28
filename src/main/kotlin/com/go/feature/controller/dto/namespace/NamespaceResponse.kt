package com.go.feature.controller.dto.namespace

import com.go.feature.dto.status.Status

data class NamespaceResponse(
    val id: String,
    val name: String,
    val status: Status
)
