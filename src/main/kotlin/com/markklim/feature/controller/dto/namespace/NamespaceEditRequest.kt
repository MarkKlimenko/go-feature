package com.markklim.feature.controller.dto.namespace

import com.markklim.feature.dto.status.Status
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class NamespaceEditRequest(
    @field:Length(min = 1, max = 100)
    @field:NotBlank
    val name: String,

    val status: Status,

    @field:Min(0)
    val version: Int,
)
