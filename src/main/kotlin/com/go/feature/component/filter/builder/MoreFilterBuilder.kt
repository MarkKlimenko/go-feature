package com.go.feature.component.filter.builder

import com.go.feature.component.filter.util.composeNumberMoreFilter
import com.go.feature.component.filter.util.parseDouble
import com.go.feature.persistence.entity.Filter
import org.apache.lucene.document.DoubleField
import org.apache.lucene.document.Field
import org.apache.lucene.search.BooleanClause
import org.springframework.stereotype.Component

@Component
class MoreFilterBuilder : FilterBuilder {
    override fun getOperator(): Filter.Operator = Filter.Operator.MORE

    override fun buildField(field: String, value: String?): Field {
        val doubleValue: Double? = parseDouble(field, value)

        return if (doubleValue != null) {
            DoubleField(field, doubleValue)
        } else {
            DoubleField(field, Double.MAX_VALUE)
        }
    }

    override fun buildClause(field: String, value: String?): BooleanClause {
        return composeNumberMoreFilter(field, parseDouble(field, value))
    }
}