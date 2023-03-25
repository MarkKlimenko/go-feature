package com.go.feature.component.filter.builder

import com.go.feature.component.filter.util.composeNumberMoreFilter
import com.go.feature.component.filter.util.parseLong
import com.go.feature.persistence.entity.Filter
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.search.BooleanClause
import org.springframework.stereotype.Component

@Component
class MoreFilterBuilder : FilterBuilder {
    override fun getOperator(): Filter.Operator = Filter.Operator.MORE

    override fun buildField(field: String, value: String?): Field {
        val longValue: Long? = parseLong(field, value)

        return if (longValue != null) {
            LongField(field, longValue)
        } else {
            LongField(field, Long.MAX_VALUE)
        }
    }

    override fun buildClause(field: String, value: String?): BooleanClause {
        return composeNumberMoreFilter(field, parseLong(field, value))
    }
}