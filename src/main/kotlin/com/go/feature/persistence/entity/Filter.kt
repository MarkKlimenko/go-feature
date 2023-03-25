package com.go.feature.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("filters")
data class Filter(
    @field:Id
    @field:Column("id")
    val id: String,

    @field:Column("name")
    val name: String,

    @field:Column("namespace")
    val namespace: String,

    @field:Column("parameter")
    val parameter: String,

    @field:Column("operator")
    val operator: Operator,

    @field:Column("description")
    val description: String? = null,

    @field:Version
    val version: Int? = null
) {
    enum class Operator(
        val value: String
    ) {
        EQ("eq"),
        CONTAINS("contains"),

        MORE("more")
    }
}
