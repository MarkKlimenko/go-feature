package com.go.feature.component.filter.builder

import com.go.feature.component.filter.util.FILTER_DISABLED_VALUE
import com.go.feature.component.filter.util.composeStringEqFilter
import com.go.feature.dto.operator.FilterOperator
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.search.BooleanClause
import org.springframework.stereotype.Component

@Component
class ContainsFilterBuilder : FilterBuilder {
    override fun getOperator(): FilterOperator = FilterOperator.CONTAINS

    override fun buildField(field: String, value: String?): Field {
        return if (value != null) {
            TextField(field, value, Field.Store.NO)
        } else {
            TextField(field, FILTER_DISABLED_VALUE, Field.Store.NO)
        }
    }

    override fun buildClause(field: String, value: String?): BooleanClause {
        return composeStringEqFilter(field, value)
    }
}