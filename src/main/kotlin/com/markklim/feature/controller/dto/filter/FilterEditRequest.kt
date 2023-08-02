package com.markklim.feature.controller.dto.filter

import com.markklim.feature.dto.operator.FilterOperator
import com.markklim.feature.dto.status.FilterStatus
import org.hibernate.validator.constraints.Length
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class FilterEditRequest(
    @field:Length(min = 1, max = 100)
    @field:NotBlank
    val name: String,

    @field:Length(min = 1, max = 50)
    @field:NotBlank
    val parameter: String,

    val operator: FilterOperator,

    val status: FilterStatus,

    val description: String? = null,

    @field:Min(0)
    val version: Int,
)
