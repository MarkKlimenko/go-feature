package com.go.feature.converter

import com.go.feature.persistence.entity.Feature
import com.go.feature.service.dto.loader.LoadedSettings
import org.springframework.stereotype.Component

@Component
class FeatureConverter {

    fun convertStatus(status: LoadedSettings.Status): Feature.Status {
        return when (status) {
            LoadedSettings.Status.ENABLED -> Feature.Status.ENABLED
            LoadedSettings.Status.DISABLED -> Feature.Status.DISABLED
        }
    }
}