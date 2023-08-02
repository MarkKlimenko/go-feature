package com.markklim.feature.service.status

import com.markklim.feature.controller.dto.service.ProbeResponse
import org.springframework.stereotype.Service

@Service
class LivenessService {
    fun checkLiveness(): ProbeResponse {
        return ProbeResponse(
            status = ProbeResponse.Status.UP
        )
    }
}