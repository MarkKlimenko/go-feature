package com.go.feature.service.filter

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.repository.FilterRepository
import com.go.feature.service.feature.FeatureService
import com.go.feature.service.index.IndexVersionService
import com.go.feature.util.checkStorageForUpdateAction
import com.go.feature.util.exception.client.ClientException
import com.go.feature.util.message.FILTER_NOT_FOUND_ERROR
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FilterRemovalService(
    val filterRepository: FilterRepository,
    val indexVersionService: IndexVersionService,
    val featureService: FeatureService,
    val applicationProperties: ApplicationProperties,
) {

    @Transactional(rollbackFor = [Exception::class])
    suspend fun deleteFilter(id: String) {
        checkStorageForUpdateAction(applicationProperties)

        val deletedFilter: Filter = filterRepository.findById(id)
            ?: throw ClientException(FILTER_NOT_FOUND_ERROR)

        featureService.validateFilterNotUsedByFeatures(deletedFilter)
        filterRepository.deleteById(id)
        indexVersionService.update(deletedFilter.namespace)
    }
}