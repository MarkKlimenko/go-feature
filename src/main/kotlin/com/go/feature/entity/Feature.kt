package com.go.feature.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("features")
data class Feature(
    @field:Id
    @field:Column("id")
    val id: String,

    @field:Column("name")
    val name: String,

    @field:Column("namespace")
    val namespace: String,

    @field:Column("filters")
    val filters: List<Filter>,

    @field:Column("status")
    val status: Status,

    @field:Column("description")
    val description: String? = null,

    @field:Version
    val version: Int? = null
) {
    data class Filter(
        val id: String,
        val value: String,
    )

    enum class Status {
        ENABLED, DISABLED
    }
}
