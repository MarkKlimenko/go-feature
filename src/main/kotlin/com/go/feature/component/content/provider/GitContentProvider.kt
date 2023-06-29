package com.go.feature.component.content.provider

import mu.KLogging
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.transport.URIish
import java.io.File

// TODO: implement git loader
// git show
class GitContentProvider : ContentProvider {
    override fun getContent(location: String, fileType: String): List<ByteArray> {

        var git: Git? = null

        // try to create and update repository
        try {
            git = Git.open(File("tmp/setting/git"))
            git.pull().call()
            // TODO: update here
        } catch (e: RepositoryNotFoundException) {
            logger.debug("Local repository not found")
        }

        if(git == null) {
            git = Git.cloneRepository()
                .setURI("https://github.com/MarkKlimenko/go-feature.git")
                .setDirectory(File("tmp/setting/git"))
                .setBranch("feature/implement_git_settings_loader")
                .setDepth(1)
                .call()
        }


        //todo: get files


        TODO("Not yet implemented")
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "GIT_CONTENT_PROVIDER:"
    }
}