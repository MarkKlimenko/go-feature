package com.go.feature.service.index

import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.entity.IndexVersion
import com.go.feature.persistence.entity.Namespace
import com.go.feature.persistence.repository.FeatureRepository
import com.go.feature.persistence.repository.FilterRepository
import com.go.feature.persistence.repository.IndexVersionRepository
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.util.exception.ValidationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class IndexLoaderService(
    val indexService: IndexService,
    val indexVersionRepository: IndexVersionRepository,
    val namespaceRepository: NamespaceRepository,
    val filterRepository: FilterRepository,
    val featureRepository: FeatureRepository,
) {

    @Scheduled(fixedDelayString = "\${application.index.ttl}")
    fun loadIndexes() = runBlocking {
        logger.debug("${LOG_PREFIX} Start index update checker")

        indexVersionRepository.findAll()
            .collect { indexVersion: IndexVersion ->
                val internalIndexVersion: String? = indexService.getInternalIndexVersion(indexVersion.namespace)

                if (internalIndexVersion == null || internalIndexVersion != indexVersion.indexVersionValue) {
                    val namespace: Namespace = namespaceRepository.findById(indexVersion.namespace)
                        ?: throw ValidationException("Namespace not found for index=${indexVersion}")

                    if (namespace.status == Namespace.Status.ENABLED) {
                        logger.info("${LOG_PREFIX} Start index update for namespace=${namespace.name}")

                        val filters: List<Filter> =
                            filterRepository.findByNamespace(indexVersion.namespace).toList()
                        val features: List<Feature> =
                            featureRepository.findByNamespaceAndStatus(indexVersion.namespace, Feature.Status.ENABLED)
                                .toList()

                        indexService.createIndex(indexVersion, namespace, filters, features)

                        logger.info("${LOG_PREFIX} Finish index update for namespace=${namespace.name}")
                    }
                } else {
                    logger.debug("${LOG_PREFIX} Index for namespaceId=${indexVersion.namespace} is already up to date")
                }
            }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "INDEX_LOADER:"
    }
}