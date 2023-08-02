package com.markklim.feature.util

import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.util.exception.client.ClientException
import com.markklim.feature.util.message.STORAGE_DISABLED_ERROR

fun checkStorageForUpdateAction(applicationProperties: ApplicationProperties) {
    if (!applicationProperties.storage.enabled) {
        throw ClientException(STORAGE_DISABLED_ERROR)
    }
}