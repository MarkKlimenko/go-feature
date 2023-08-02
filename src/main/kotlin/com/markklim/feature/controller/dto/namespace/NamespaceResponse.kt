package com.markklim.feature.controller.dto.namespace

import com.markklim.feature.dto.status.Status

data class NamespaceResponse(
    val id: String,
    val name: String,
    val status: Status,
    val version: Int
)
