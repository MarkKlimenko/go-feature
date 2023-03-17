package com.go.feature.service.index

import com.go.feature.persistence.repository.IndexVersionRepository
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class IndexLoaderService(
    val indexVersionRepository: IndexVersionRepository
) {

    //TODO: get from settings 60 - 60
    @Scheduled(fixedDelay = 60000, initialDelayString = "#{new java.util.Random().nextInt(10000)}")
    fun loadIndexes() = runBlocking {
        //TODO: debug
        logger.info("${LOG_PREFIX} Start index update checker")

        indexVersionRepository.findAll()
            .collect {
                logger.info("${LOG_PREFIX} index=${it}")
            }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "INDEX_LOADER:"
    }
}