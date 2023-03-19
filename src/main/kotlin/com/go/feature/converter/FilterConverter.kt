package com.go.feature.converter

import com.go.feature.persistence.entity.Filter
import com.go.feature.service.loader.dto.LoadedSettings
import com.go.feature.util.randomId
import org.springframework.stereotype.Component

@Component
class FilterConverter {
    val stringToOperatorMap: Map<String, Filter.Operator> = Filter.Operator.values().associateBy { it.value }

    fun convert(namespaceId: String, filterSettings: List<LoadedSettings.Filter>): List<Filter> {
        return filterSettings.map {
            Filter(
                id = randomId(),
                name = it.name,
                namespace = namespaceId,
                parameter = it.parameter,
                operator = convertOperator(it.operator),
                description = it.description
            )
        }
    }

    fun convertOperator(operator: String): Filter.Operator {
        return stringToOperatorMap[operator]
            ?: throw IllegalArgumentException("Operator '${operator}' is not supported")
    }
}