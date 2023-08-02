package com.markklim.feature.service.filter

import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.persistence.repository.FilterRepository
import com.markklim.feature.service.feature.FeatureService
import com.markklim.feature.service.index.IndexVersionService
import com.markklim.feature.util.checkStorageForUpdateAction
import com.markklim.feature.util.exception.client.ClientException
import com.markklim.feature.util.message.FILTER_NOT_FOUND_ERROR
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