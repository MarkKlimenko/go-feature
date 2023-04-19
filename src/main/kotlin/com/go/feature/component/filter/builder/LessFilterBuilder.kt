package com.go.feature.component.filter.builder

import com.go.feature.component.filter.util.composeNumberLessFilter
import com.go.feature.component.filter.util.parseDouble
import com.go.feature.dto.operator.FilterOperator
import org.apache.lucene.document.DoubleField
import org.apache.lucene.document.Field
import org.apache.lucene.search.BooleanClause
import org.springframework.stereotype.Component

@Component
class LessFilterBuilder : FilterBuilder {
    override fun getOperator(): FilterOperator = FilterOperator.LESS

    override fun buildField(field: String, value: String?): Field {
        val doubleValue: Double? = value?.let { parseValue(field, value) }

        return if (doubleValue != null) {
            DoubleField(field, doubleValue)
        } else {
            DoubleField(field, Double.MIN_VALUE)
        }
    }

    override fun buildClause(field: String, value: String?): BooleanClause {
        val doubleValue: Double? = value?.let { parseValue(field, value) }
        return composeNumberLessFilter(field, doubleValue)
    }

    fun parseValue(field: String, value: String): Double = parseDouble(field, value)
}