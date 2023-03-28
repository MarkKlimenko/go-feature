package com.go.feature.persistence.entity

import com.go.feature.dto.status.Status
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("namespaces")
data class Namespace(
    @field:Id
    @field:Column("id")
    val id: String,

    @field:Column("name")
    val name: String,

    @field:Column("status")
    val status: Status,

    @field:Version
    val version: Int? = null
)
