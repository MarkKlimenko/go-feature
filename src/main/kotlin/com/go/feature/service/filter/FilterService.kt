package com.go.feature.service.filter

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.configuration.properties.LocalizationProperties
import com.go.feature.controller.dto.filter.FilterCreateRequest
import com.go.feature.controller.dto.filter.FilterEditRequest
import com.go.feature.controller.dto.filter.FilterResponse
import com.go.feature.controller.dto.filter.FiltersResponse
import com.go.feature.converter.FilterConverter
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.repository.FilterRepository
import com.go.feature.service.index.IndexVersionService
import com.go.feature.util.checkStorageForUpdateAction
import com.go.feature.util.exception.client.ClientException
import com.go.feature.util.message.FILTER_ALREADY_EXISTS_ERROR
import com.go.feature.util.message.FILTER_NOT_FOUND_ERROR
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FilterService(
    val filterRepository: FilterRepository,
    val filterConverter: FilterConverter,
    val indexVersionService: IndexVersionService,
    val applicationProperties: ApplicationProperties,
    val localizationProperties: LocalizationProperties,
) {

    suspend fun getFilters(namespaceId: String): FiltersResponse {
        val filters: List<FilterResponse> = filterRepository.findByNamespace(namespaceId)
            .map { filterConverter.convert(it) }
            .toList()

        return FiltersResponse(
            filters = filters
        )
    }

    suspend fun getFilter(id: String): FilterResponse =
        filterRepository.findById(id)
            ?.let { filterConverter.convert(it) }
            ?: throw ClientException(FILTER_NOT_FOUND_ERROR)

    // TODO: check filters/features size before loading
    @Transactional(rollbackFor = [Exception::class])
    suspend fun createFilter(request: FilterCreateRequest): FilterResponse {
        checkStorageForUpdateAction(applicationProperties)

        filterRepository.findByNameAndNamespace(request.name, request.namespace)
            ?.let { throw ClientException(FILTER_ALREADY_EXISTS_ERROR) }

        return filterRepository.save(filterConverter.create(request))
            .let {
                indexVersionService.update(it.namespace)
                filterConverter.convert(it)
            }
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun editFilter(id: String, request: FilterEditRequest): FilterResponse {
        checkStorageForUpdateAction(applicationProperties)

        val requiredFilter: Filter = filterRepository.findById(id)
            ?: throw ClientException(FILTER_NOT_FOUND_ERROR)

        return filterRepository.save(filterConverter.edit(requiredFilter, request))
            .let {
                indexVersionService.update(it.namespace)
                filterConverter.convert(it)
            }
    }

    suspend fun deleteAllForNamespace(namespaceId: String) {
        filterRepository.deleteAllByNamespace(namespaceId)
    }

    suspend fun createFiltersForSettings(namespaceId: String, settings: LoadedSettings): List<Filter> {
        val filters: List<Filter> = filterConverter.create(namespaceId, settings.filters)
        filterRepository.saveAll(filters).collect()
        return filters
    }
}