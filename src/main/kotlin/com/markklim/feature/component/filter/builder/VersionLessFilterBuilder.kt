package com.markklim.feature.component.filter.builder

import com.markklim.feature.component.filter.util.parseVersion
import com.markklim.feature.dto.operator.FilterOperator
import org.springframework.stereotype.Component

@Component
class VersionLessFilterBuilder : LessFilterBuilder() {
    override fun getOperator(): FilterOperator = FilterOperator.VERSION_LESS

    override fun parseValue(field: String, value: String): Double = parseVersion(field, value)
}