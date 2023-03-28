package com.go.feature.converter

import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.persistence.entity.Filter
import com.go.feature.util.randomId
import org.springframework.stereotype.Component

@Component
class FilterConverter {
    fun convert(namespaceId: String, filterSettings: List<LoadedSettings.Filter>): List<Filter> {
        return filterSettings.map {
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
    }
}