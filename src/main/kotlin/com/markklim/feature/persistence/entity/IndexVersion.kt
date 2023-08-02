package com.markklim.feature.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("index_versions")
data class IndexVersion(
    @field:Id
    @field:Column("id")
    val id: String,

    @field:Column("namespace")
    val namespace: String,

    @field:Column("index_version")
    val indexVersionValue: String,

    @field:Version
    val version: Int? = null
)
