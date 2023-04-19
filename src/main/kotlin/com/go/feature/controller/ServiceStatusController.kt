package com.go.feature.controller

import com.go.feature.controller.dto.service.ProbeResponse
import com.go.feature.service.status.LivenessService
import com.go.feature.service.status.ReadinessService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("service")
class ServiceStatusController(
    val livenessService: LivenessService,
    val readinessService: ReadinessService
) {
    @GetMapping("liveness")
    suspend fun checkLiveness(): ProbeResponse = livenessService.checkLiveness()

    @GetMapping("readiness")
    suspend fun checkReadiness(): ProbeResponse = readinessService.checkReadiness()
}