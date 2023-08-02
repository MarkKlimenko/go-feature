package com.markklim.feature.persistence.entity

import com.markklim.feature.dto.status.Status
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

    /**
     * List<Filter>
     */
    @field:Column("filters")
    val filters: String,

    @field:Column("status")
    val status: Status,

    @field:Column("description")
    val description: String? = null,

    @field:Version
    val version: Int? = null

    // TODO: implement required data parameters
) {
    data class Filter(
        val id: String,
        val value: String,
    )
}
