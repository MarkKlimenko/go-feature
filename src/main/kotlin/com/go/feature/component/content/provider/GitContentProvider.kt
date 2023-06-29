package com.go.feature.component.content.provider

import mu.KLogging
import org.eclipse.jgit.api.Git
import java.io.File

// TODO: implement git loader
// git show
class GitContentProvider : ContentProvider {
    override fun getContent(location: String, fileType: String): List<ByteArray> {
        Git.cloneRepository()
            .setURI("https://github.com/MarkKlimenko/go-feature.git")
            .setDirectory(File("tmp/setting/git"))
            .setBranch("settings")
            .call()

        TODO("Not yet implemented")
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "GIT_CONTENT_PROVIDER:"
    }
}