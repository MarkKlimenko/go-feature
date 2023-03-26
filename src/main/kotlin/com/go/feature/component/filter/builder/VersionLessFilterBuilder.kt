package com.go.feature.component.filter.builder

import com.go.feature.component.filter.util.parseVersion
import com.go.feature.persistence.entity.Filter
import org.springframework.stereotype.Component

@Component
class VersionLessFilterBuilder : LessFilterBuilder() {
    override fun getOperator(): Filter.Operator = Filter.Operator.VERSION_LESS

    override fun parseValue(field: String, value: String?): Double? {
        return parseVersion(field, value)
    }
}