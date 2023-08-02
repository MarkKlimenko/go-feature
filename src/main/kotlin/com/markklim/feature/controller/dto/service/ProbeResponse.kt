package com.markklim.feature.controller.dto.service

data class ProbeResponse(
    val status: Status
) {
    enum class Status {
        UP,
        WAITING
    }
}