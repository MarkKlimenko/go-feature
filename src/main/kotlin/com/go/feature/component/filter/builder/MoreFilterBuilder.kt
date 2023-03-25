package com.go.feature.component.filter.builder

import com.go.feature.component.filter.util.composeNumberMoreFilter
import com.go.feature.persistence.entity.Filter
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.search.BooleanClause
import org.springframework.stereotype.Component

@Component
class MoreFilterBuilder : FilterBuilder {
    override fun getOperator(): Filter.Operator = Filter.Operator.MORE

    override fun buildField(field: String, value: String?): Field {
        val longValue: Long? = parseValue(field, value)

        return if (longValue != null) {
            LongField(field, longValue)
        } else {
            LongField(field, Long.MAX_VALUE)
        }
    }

    override fun buildClause(field: String, value: String?): BooleanClause {
        return composeNumberMoreFilter(field, parseValue(field, value))
    }

    private fun parseValue(field: String, value: String?): Long? {
        return try {
            value?.toLong()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Value is not compatible with filter; field=${field}, value=${value}")
        }
    }
}