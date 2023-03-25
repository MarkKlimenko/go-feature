package com.go.feature.component.filter

import com.go.feature.component.filter.builder.FilterBuilder
import com.go.feature.persistence.entity.Filter
import org.springframework.stereotype.Component

@Component
class FilterComponent(
    filterBuilders: List<FilterBuilder>
) {
    private val filterBuildersMap: Map<Filter.Operator, FilterBuilder> = filterBuilders.associateBy { it.getOperator() }

    fun getFilterBuilder(operator: Filter.Operator): FilterBuilder {
        return filterBuildersMap[operator]
            ?: throw IllegalArgumentException("Filter operator '${operator.value}' is not supported for current app version")
    }
}