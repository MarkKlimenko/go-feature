package com.markklim.feature.converter

import com.markklim.feature.controller.dto.filter.FilterCreateRequest
import com.markklim.feature.controller.dto.filter.FilterEditRequest
import com.markklim.feature.controller.dto.filter.FilterResponse
import com.markklim.feature.dto.settings.loader.LoadedSettings
import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.util.randomId
import org.springframework.stereotype.Component

@Component
class FilterConverter {
    fun create(request: FilterCreateRequest): Filter =
        Filter(
            id = randomId(),
            name = request.name,
            namespace = request.namespace,
            parameter = request.parameter,
            operator = request.operator,
            status = request.status,
            description = request.description
        )

    fun create(namespaceId: String, filterSettings: List<LoadedSettings.Filter>): List<Filter> =
        filterSettings.map {
            Filter(
                id = randomId(),
                name = it.name,
                namespace = namespaceId,
                parameter = it.parameter,
                operator = it.operator,
                status = it.status,
                description = it.description
            )
        }

    fun convert(filter: Filter): FilterResponse =
        FilterResponse(
            id = filter.id,
            name = filter.name,
            namespace = filter.namespace,
            parameter = filter.parameter,
            operator = filter.operator,
            status = filter.status,
            description = filter.description,
            version = filter.version!!
        )

    fun edit(editedFilter: Filter, request: FilterEditRequest): Filter =
        editedFilter.copy(
            name = request.name,
            parameter = request.parameter,
            operator = request.operator,
            status = request.status,
            description = request.description,
            version = request.version,
        )
}