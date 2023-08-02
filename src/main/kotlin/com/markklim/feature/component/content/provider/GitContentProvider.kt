package com.markklim.feature.component.content.provider

import mu.KLogging
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import java.io.File

class GitContentProvider(
    private val uri: String,
    private val localDirectory: String,
    private val branch: String,
) : ContentProvider {
    private val fileContentProvider = FileContentProvider()

    override fun getContent(location: String, fileType: String): List<ByteArray> {
        retrieveRepositoryFiles()
        return fileContentProvider.getContent("$localDirectory/$location", fileType)
    }

    private fun retrieveRepositoryFiles() {
        val isRepositoryExists: Boolean =
            try {
                val git = Git.open(File(localDirectory))
                git.pull().call()
                true
            } catch (ignored: RepositoryNotFoundException) {
                logger.debug("$LOG_PREFIX Local repository not found")
                false
            }

        if (!isRepositoryExists) {
            Git.cloneRepository()
                .setURI(uri)
                .setDirectory(File(localDirectory))
                .setBranch(branch)
                .setDepth(1)
                .call()
        }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "GIT_CONTENT_PROVIDER:"
    }
}