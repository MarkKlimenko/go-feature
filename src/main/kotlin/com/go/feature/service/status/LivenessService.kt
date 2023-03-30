package com.go.feature.service.status

import com.go.feature.controller.dto.service.ProbeResponse
import org.springframework.stereotype.Service

@Service
class LivenessService {
    fun checkLiveness(): ProbeResponse {
        return ProbeResponse(
            status = ProbeResponse.Status.UP
        )
    }
}