package com.go.feature.component.filter.builder

import com.go.feature.persistence.entity.Filter
import org.apache.lucene.document.Field
import org.apache.lucene.search.BooleanClause

interface FilterBuilder {
    fun getOperator(): Filter.Operator

    fun buildField(field: String, value: String?): Field

    fun buildClause(field: String, value: String?): BooleanClause
}