package com.go.feature.controller.dto.filter

import com.go.feature.dto.operator.FilterOperator
import com.go.feature.dto.status.FilterStatus
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

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
