package com.go.feature.converter.util

import com.go.feature.dto.operator.FilterOperator
import com.go.feature.dto.status.FilterStatus
import com.go.feature.persistence.entity.Filter
import com.go.feature.util.exception.localized.ClientException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FilterConverterUtilTest {

    @Test
    fun getFilterIdByNameTest() {
        val nameToFilterMap: Map<String, Filter> = mapOf(
            "testFilter" to Filter(
                id = "testId",
                name = "testFilter",
                namespace = "",
                parameter = "",
                operator = FilterOperator.EQ,
                status = FilterStatus.ENABLED
            )
        )

        assertEquals(
            "testId",
            getFilterIdByName(nameToFilterMap, "testFilter")
        )
    }

    @Test
    fun getFilterIdByNotSupportedNameTest() {
        assertThrows<ClientException> {
            getFilterIdByName(mapOf(), "name")
        }
    }
}