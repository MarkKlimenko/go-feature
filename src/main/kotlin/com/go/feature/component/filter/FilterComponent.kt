package com.go.feature.component.filter

import com.go.feature.component.filter.builder.FilterBuilder
import com.go.feature.persistence.entity.Filter
import com.go.feature.util.exception.ValidationException
import org.springframework.stereotype.Component

@Component
class FilterComponent(
    filterBuilders: List<FilterBuilder>
) {
    private val filterBuildersMap: Map<Filter.Operator, FilterBuilder> = filterBuilders.associateBy { it.getOperator() }

    fun getFilterBuilder(operator: Filter.Operator): FilterBuilder {
        return filterBuildersMap[operator]
            ?: throw ValidationException("Filter operator '${operator.value}' is not supported for current app version")
    }
}