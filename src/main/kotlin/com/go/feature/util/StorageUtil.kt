package com.go.feature.util

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.util.exception.client.ClientException
import com.go.feature.util.message.STORAGE_DISABLED_ERROR

fun checkStorageForUpdateAction(applicationProperties: ApplicationProperties) {
    if (!applicationProperties.storage.enabled) {
        throw ClientException(STORAGE_DISABLED_ERROR)
    }
}