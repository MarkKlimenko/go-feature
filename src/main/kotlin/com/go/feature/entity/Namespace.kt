package com.go.feature.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("NAMESPACES")
data class Namespace(
    @field:Id
    @field:Column("NAME")
    val name: String,

    @field:Column("STATUS")
    val status: Status,
) {
    enum class Status {
        ENABLED, DISABLED
    }
}
