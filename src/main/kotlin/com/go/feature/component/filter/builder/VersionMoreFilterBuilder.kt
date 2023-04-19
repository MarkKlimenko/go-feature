package com.go.feature.component.filter.builder

import com.go.feature.component.filter.util.parseVersion
import com.go.feature.dto.operator.FilterOperator
import org.springframework.stereotype.Component

@Component
class VersionMoreFilterBuilder : MoreFilterBuilder() {
    override fun getOperator(): FilterOperator = FilterOperator.VERSION_MORE

    override fun parseValue(field: String, value: String): Double = parseVersion(field, value)
}