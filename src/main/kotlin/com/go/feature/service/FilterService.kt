package com.go.feature.service

import com.go.feature.converter.FilterConverter
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.repository.FilterRepository
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service

@Service
class FilterService(
    val filterRepository: FilterRepository,
    val filterConverter: FilterConverter,
) {

    suspend fun deleteAllForNamespace(namespaceId: String) {
        filterRepository.deleteAllByNamespace(namespaceId)
    }

    suspend fun createFiltersForSettings(namespaceId: String, settings: LoadedSettings): List<Filter> {
        val filters: List<Filter> = filterConverter.convert(namespaceId, settings.filters)
        filterRepository.saveAll(filters).collect()
        return filters
    }
}