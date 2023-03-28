package com.go.feature.component.filter.builder

import com.go.feature.dto.operator.FilterOperator
import org.apache.lucene.document.Field
import org.apache.lucene.search.BooleanClause

interface FilterBuilder {
    fun getOperator(): FilterOperator

    fun buildField(field: String, value: String?): Field

    fun buildClause(field: String, value: String?): BooleanClause
}