package com.markklim.feature.component.filter

import com.markklim.feature.component.filter.builder.EqFilterBuilder
import com.markklim.feature.component.filter.builder.FilterBuilder
import com.markklim.feature.dto.operator.FilterOperator
import com.markklim.feature.util.exception.client.ClientException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FilterComponentTest {
    @Test
    fun getFilterBuilderByUnsupportedOperator() {
        val filterBuilders: List<FilterBuilder> = listOf(
            EqFilterBuilder()
        )

        val filterComponent = FilterComponent(filterBuilders)

        assertThrows<ClientException>(
            "Expected message: Filter operator 'CONTAINS' is not supported for current app version"
        ) { filterComponent.getFilterBuilder(FilterOperator.CONTAINS) }
    }
}