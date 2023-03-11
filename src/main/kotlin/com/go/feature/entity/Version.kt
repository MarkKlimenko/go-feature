package com.go.feature.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("VERSIONS")
data class Version(
    @field:Id
    @field:Column("ID")
    val id: String,

    @field:Column("NAMESPACE")
    val namespace: String,

    @field:Column("VERSION")
    val version: String,
)
