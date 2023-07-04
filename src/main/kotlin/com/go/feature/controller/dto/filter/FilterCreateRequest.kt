package com.go.feature.controller.dto.filter

import com.go.feature.dto.operator.FilterOperator
import com.go.feature.dto.status.FilterStatus

data class FilterCreateRequest(
    val name: String,
    val namespace: String,
    val parameter: String,
    val operator: FilterOperator,
    val status: FilterStatus,
    val description: String? = null,
)
