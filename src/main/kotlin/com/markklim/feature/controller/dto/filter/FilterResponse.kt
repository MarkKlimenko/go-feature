package com.markklim.feature.controller.dto.filter

import com.markklim.feature.dto.operator.FilterOperator
import com.markklim.feature.dto.status.FilterStatus

data class FilterResponse(
    val id: String,
    val name: String,
    val namespace: String,
    val parameter: String,
    val operator: FilterOperator,
    val status: FilterStatus,
    val description: String? = null,
    val version: Int
)
