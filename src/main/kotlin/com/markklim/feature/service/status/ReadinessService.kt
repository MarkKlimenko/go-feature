package com.markklim.feature.service.status

import com.markklim.feature.controller.dto.service.ProbeResponse
import com.markklim.feature.controller.dto.service.ProbeResponse.Status.UP
import com.markklim.feature.controller.dto.service.ProbeResponse.Status.WAITING
import com.markklim.feature.service.index.IndexLoaderService
import org.springframework.stereotype.Service

@Service
class ReadinessService(
    val indexLoaderService: IndexLoaderService
) {
    fun checkReadiness(): ProbeResponse {
        return ProbeResponse(
            status = if (indexLoaderService.isIndexLoaded()) UP else WAITING
        )
    }
}