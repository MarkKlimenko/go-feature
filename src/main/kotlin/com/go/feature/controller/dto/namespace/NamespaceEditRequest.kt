package com.go.feature.controller.dto.namespace

import com.go.feature.dto.status.Status

data class NamespaceEditRequest(
    val name: String,
    val status: Status
)
