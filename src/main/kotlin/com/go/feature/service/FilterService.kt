package com.go.feature.service

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.controller.dto.filter.FilterCreateRequest
import com.go.feature.controller.dto.filter.FilterEditRequest
import com.go.feature.controller.dto.filter.FilterResponse
import com.go.feature.controller.dto.filter.FiltersResponse
import com.go.feature.converter.FilterConverter
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.repository.FilterRepository
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.service.index.IndexService
import com.go.feature.util.checkStorageForUpdateAction
import com.go.feature.util.exception.ValidationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FilterService(
    val filterRepository: FilterRepository,
    val filterConverter: FilterConverter,
    val namespaceRepository: NamespaceRepository,
    val indexVersionService: IndexVersionService,
    val indexService: IndexService,
    val applicationProperties: ApplicationProperties,
) {

    suspend fun getFilters(namespace: String): FiltersResponse {
        val filters: List<FilterResponse> = filterRepository.findByNamespace(namespace)
            .map { filterConverter.convert(it) }
            .toList()

        return FiltersResponse(
            filters = filters
        )
    }

    suspend fun getFilter(id: String): FilterResponse =
        filterRepository.findById(id)
            ?.let { filterConverter.convert(it) }
            ?: throw ValidationException("Filter not found")

    // TODO: check Transactional
    @Transactional(rollbackFor = [Exception::class])
    suspend fun createFilter(request: FilterCreateRequest): FilterResponse {
        checkStorageForUpdateAction(applicationProperties)

        namespaceRepository.findById(request.namespace)
            ?: throw ValidationException("Unsupported namespace")

        filterRepository.findByNameAndNamespace(request.name, request.namespace)
            ?.let { throw ValidationException("Filter already exists") }

        return filterRepository.save(filterConverter.create(request))
            .let {
                indexVersionService.update(it.namespace)
                filterConverter.convert(it)
            }
    }

    // TODO: check Transactional
    @Transactional(rollbackFor = [Exception::class])
    suspend fun editFilter(id: String, request: FilterEditRequest): FilterResponse {
        checkStorageForUpdateAction(applicationProperties)

        val requiredFilter: Filter = filterRepository.findById(id)
            ?: throw ValidationException("Filter not found")

        return filterRepository.save(filterConverter.edit(requiredFilter, request))
            .let {
                indexVersionService.update(it.namespace)
                filterConverter.convert(it)
            }
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun deleteFilter(id: String) {
        checkStorageForUpdateAction(applicationProperties)

        val deletedFilter: Filter = filterRepository.findById(id)
            ?: throw ValidationException("Filter not found")

        if (indexService.isFilterUsedByFeatures(deletedFilter)) {
            throw ValidationException("Filter is used by features")
        }
        filterRepository.deleteById(id)

        indexVersionService.update(id)
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