package com.markklim.feature.converter.util

import com.markklim.feature.dto.operator.FilterOperator
import com.markklim.feature.dto.status.FilterStatus
import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.util.exception.client.ClientException
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