package com.markklim.feature.service.index

import com.markklim.feature.dto.status.Status
import com.markklim.feature.persistence.entity.Feature
import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.persistence.entity.IndexVersion
import com.markklim.feature.persistence.entity.Namespace
import com.markklim.feature.persistence.repository.FeatureRepository
import com.markklim.feature.persistence.repository.FilterRepository
import com.markklim.feature.persistence.repository.IndexVersionRepository
import com.markklim.feature.persistence.repository.NamespaceRepository
import com.markklim.feature.util.exception.client.ClientException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class IndexLoaderService(
    val indexService: IndexService,
    val indexVersionRepository: IndexVersionRepository,
    val namespaceRepository: NamespaceRepository,
    val filterRepository: FilterRepository,
    val featureRepository: FeatureRepository,
) {
    private val isLoaded: AtomicBoolean = AtomicBoolean(false)

    @Scheduled(fixedDelayString = "\${application.index.ttl}")
    fun loadIndexesScheduler() = runBlocking {
        loadIndexes()
    }

    fun isIndexLoaded(): Boolean = isLoaded.get()

    suspend fun loadIndexes() {
        logger.debug("$LOG_PREFIX Start index update checker")

        indexVersionRepository.findAll()
            .collect { indexVersion: IndexVersion ->
                val internalIndexVersion: String? = indexService.getInternalIndexVersion(indexVersion.namespace)

                if (internalIndexVersion == null || internalIndexVersion != indexVersion.indexVersionValue) {
                    try {
                        loadIndexForNamespace(indexVersion)
                    } catch (e: Exception) {
                        logger.error("Error: ", e)
                    }
                } else {
                    logger.debug("$LOG_PREFIX Index for namespaceId=${indexVersion.namespace} is already up to date")
                }
            }

        logger.debug("$LOG_PREFIX Finish index update checker")
        isLoaded.set(true)
    }

    private suspend fun loadIndexForNamespace(indexVersion: IndexVersion) {
        val namespace: Namespace = namespaceRepository.findById(indexVersion.namespace)
            ?: throw ClientException("Namespace not found for index=$indexVersion")

        if (namespace.status == Status.ENABLED) {
            logger.info("$LOG_PREFIX Start index update for namespace=${namespace.name}")

            val filters: List<Filter> =
                filterRepository.findByNamespace(indexVersion.namespace).toList()
            val features: List<Feature> =
                featureRepository.findByNamespaceAndStatus(indexVersion.namespace, Status.ENABLED).toList()

            indexService.createIndex(indexVersion, namespace, filters, features)

            logger.info("$LOG_PREFIX Finish index update for namespace=${namespace.name}")
        }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "INDEX_LOADER:"
    }
}