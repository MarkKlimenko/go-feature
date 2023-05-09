package com.go.feature.converter.util

import com.go.feature.persistence.entity.Filter
import com.go.feature.util.exception.ValidationException

fun getFilterIdByName(nameToFilterMap: Map<String, Filter>, filterName: String): String {
    val filter: Filter = nameToFilterMap[filterName]
        ?: throw ValidationException("No filter with name=$filterName")

    return filter.id
}