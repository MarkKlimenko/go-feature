package com.go.feature.configuration.flyway

import com.go.feature.service.NamespaceService
import com.go.feature.service.settings.SettingsLoaderService
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.flywaydb.core.api.callback.Callback
import org.flywaydb.core.api.callback.Context
import org.flywaydb.core.api.callback.Event
import org.springframework.stereotype.Component

/**
 * Required for proper db initialization (default data propagation)
 */
@Component
class PostMigrationCallback(
    val namespaceService: NamespaceService,
    val settingsLoaderService: SettingsLoaderService,
) : Callback {

    override fun supports(event: Event, context: Context): Boolean {
        return event === Event.AFTER_MIGRATE
    }

    override fun handle(event: Event, context: Context) {
        runBlocking {
            try {
                settingsLoaderService.loadSettings()
            } catch (e: Exception) {
                logger.error("Error loading settings: ", e)
            }

            try {
                namespaceService.createDefaultNamespace()
            } catch (e: Exception) {
                logger.error("Error creating default namespace: ", e)
            }
        }
    }

    override fun canHandleInTransaction(event: Event, context: Context): Boolean {
        return true
    }

    override fun getCallbackName(): String {
        return PostMigrationCallback::class.java.simpleName
    }

    companion object: KLogging()
}