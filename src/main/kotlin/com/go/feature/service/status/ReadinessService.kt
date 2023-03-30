package com.go.feature.service.status

import com.go.feature.controller.dto.service.ProbeResponse
import com.go.feature.controller.dto.service.ProbeResponse.Status.UP
import com.go.feature.controller.dto.service.ProbeResponse.Status.WAITING
import com.go.feature.service.index.IndexLoaderService
import org.springframework.stereotype.Service

@Service
class ReadinessService(
    val indexLoaderService: IndexLoaderService
) {
    fun checkReadiness(): ProbeResponse {
        return ProbeResponse(
            status = if (indexLoaderService.isIndexLoaded()) {
                UP
            } else {
                WAITING
            }
        )
    }
}