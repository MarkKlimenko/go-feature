package com.markklim.feature.component.filter

import com.markklim.feature.component.filter.builder.FilterBuilder
import com.markklim.feature.dto.operator.FilterOperator
import com.markklim.feature.util.exception.client.ClientException
import com.markklim.feature.util.message.FILTER_OPERATOR_NOT_SUPPORTED_ERROR
import org.springframework.stereotype.Component

@Component
class FilterComponent(
    filterBuilders: List<FilterBuilder>
) {
    private val filterBuildersMap: Map<FilterOperator, FilterBuilder> =
        filterBuilders.associateBy { it.getOperator() }

    fun getFilterBuilder(operator: FilterOperator): FilterBuilder =
        filterBuildersMap[operator]
            ?: throw ClientException(
                FILTER_OPERATOR_NOT_SUPPORTED_ERROR,
                mapOf("operator" to operator)
            )
}