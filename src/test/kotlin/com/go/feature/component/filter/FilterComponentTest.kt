package com.go.feature.component.filter

import com.go.feature.component.filter.builder.EqFilterBuilder
import com.go.feature.component.filter.builder.FilterBuilder
import com.go.feature.dto.operator.FilterOperator
import com.go.feature.util.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FilterComponentTest {
    @Test
    fun getFilterBuilderByUnsupportedOperator() {
        val filterBuilders: List<FilterBuilder> = listOf(
            EqFilterBuilder()
        )

        val filterComponent = FilterComponent(filterBuilders)

        assertThrows<ValidationException>(
            "Expected message: Filter operator 'CONTAINS' is not supported for current app version"
        ) { filterComponent.getFilterBuilder(FilterOperator.CONTAINS) }
    }
}