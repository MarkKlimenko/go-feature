package com.markklim.feature.converter.util

import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.util.exception.client.ClientException

fun getFilterIdByName(nameToFilterMap: Map<String, Filter>, filterName: String): String {
    val filter: Filter = nameToFilterMap[filterName]
        ?: throw ClientException("No filter with name=$filterName")

    return filter.id
}