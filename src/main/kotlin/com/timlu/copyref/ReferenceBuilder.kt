package com.timlu.copyref

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile

object ReferenceBuilder {

    fun buildReference(project: Project, editor: Editor, virtualFile: VirtualFile): String? {
        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) return null

        val document = editor.document
        val startLine = document.getLineNumber(selectionModel.selectionStart) + 1
        val endLine = document.getLineNumber(selectionModel.selectionEnd) + 1
        val relativePath = buildRelativePath(project, virtualFile)

        return if (startLine == endLine) {
            "@${relativePath}#L${startLine}"
        } else {
            "@${relativePath}#L${startLine}-${endLine}"
        }
    }

    private fun buildRelativePath(project: Project, file: VirtualFile): String {
        val projectBasePath = project.basePath ?: return file.name
        val fileIndex = ProjectFileIndex.getInstance(project)
        val contentRoot = fileIndex.getContentRootForFile(file)

        if (contentRoot != null) {
            val modulePath = getRelativePath(projectBasePath, contentRoot.path)
            val fileRelPath = getRelativePath(contentRoot.path, file.path)

            return if (modulePath.isNotEmpty()) {
                "$modulePath/$fileRelPath"
            } else {
                fileRelPath
            }
        }

        return getRelativePath(projectBasePath, file.path)
    }

    private fun getRelativePath(basePath: String, fullPath: String): String {
        val normalizedBase = basePath.trimEnd('/')
        val normalizedFull = fullPath.trimEnd('/')

        return if (normalizedFull.startsWith(normalizedBase)) {
            normalizedFull.removePrefix(normalizedBase).trimStart('/')
        } else {
            normalizedFull
        }
    }
}
