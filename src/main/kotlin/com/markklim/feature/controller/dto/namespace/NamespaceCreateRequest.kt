package com.markklim.feature.controller.dto.namespace

import com.markklim.feature.dto.status.Status
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.NotBlank

data class NamespaceCreateRequest(
    @field:Length(min = 1, max = 100)
    @field:NotBlank
    val name: String,

    val status: Status
)
