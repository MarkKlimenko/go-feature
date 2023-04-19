package com.go.feature.component.filter

import com.go.feature.component.filter.builder.FilterBuilder
import com.go.feature.dto.operator.FilterOperator
import com.go.feature.util.exception.ValidationException
import org.springframework.stereotype.Component

@Component
class FilterComponent(
    filterBuilders: List<FilterBuilder>
) {
    private val filterBuildersMap: Map<FilterOperator, FilterBuilder> = filterBuilders.associateBy { it.getOperator() }

    fun getFilterBuilder(operator: FilterOperator): FilterBuilder {
        return filterBuildersMap[operator]
            ?: throw ValidationException("Filter operator '$operator' is not supported for current app version")
    }
}