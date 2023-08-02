package com.markklim.feature.service.filter

import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.controller.dto.filter.FilterCreateRequest
import com.markklim.feature.controller.dto.filter.FilterEditRequest
import com.markklim.feature.controller.dto.filter.FilterResponse
import com.markklim.feature.controller.dto.filter.FiltersResponse
import com.markklim.feature.converter.FilterConverter
import com.markklim.feature.dto.settings.loader.LoadedSettings
import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.persistence.repository.FilterRepository
import com.markklim.feature.service.index.IndexVersionService
import com.markklim.feature.util.checkStorageForUpdateAction
import com.markklim.feature.util.exception.client.ClientException
import com.markklim.feature.util.message.FILTER_ALREADY_EXISTS_ERROR
import com.markklim.feature.util.message.FILTER_NOT_FOUND_ERROR
import com.markklim.feature.util.message.FILTER_SIZE_EXCEEDS_ERROR
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FilterService(
    val filterRepository: FilterRepository,
    val filterConverter: FilterConverter,
    val indexVersionService: IndexVersionService,
    val properties: ApplicationProperties,
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

    @Transactional(rollbackFor = [Exception::class])
    suspend fun createFilter(request: FilterCreateRequest): FilterResponse {
        checkStorageForUpdateAction(properties)
        checkFilterCount(request.namespace)

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
        checkStorageForUpdateAction(properties)

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
        return filterRepository.saveAll(filters).toList()
    }

    private suspend fun checkFilterCount(namespaceId: String) {
        val currentCount: Long = filterRepository.countByNamespace(namespaceId)

        if (currentCount >= properties.filter.maxSize) {
            throw ClientException(
                FILTER_SIZE_EXCEEDS_ERROR,
                mapOf("filterSize" to properties.filter.maxSize.toString())
            )
        }
    }
}