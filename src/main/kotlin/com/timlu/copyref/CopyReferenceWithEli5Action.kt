package com.timlu.copyref

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection

class CopyReferenceWithEli5Action : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null && editor.selectionModel.hasSelection()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile: VirtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val reference = ReferenceBuilder.buildReference(project, editor, virtualFile) ?: return
        val prompt = buildPrompt(reference)

        CopyPasteManager.getInstance().setContents(StringSelection(prompt))
        notify(project, "ELI5 prompt copied to clipboard", NotificationType.INFORMATION)
    }

    private fun buildPrompt(reference: String): String =
        "Explain the following code to me like I'm 5 years old (ELI5). " +
            "Start with a simple high-level summary, then explain what it does step by step. " +
            "Keep the explanation practical and easy to understand.\n\n$reference"

    private fun notify(project: Project, message: String, type: NotificationType) {
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Copy Code Reference")
                .createNotification(message, type)
                .notify(project)
        } catch (_: Exception) {
            // Notifications are best-effort; copying to the clipboard already succeeded.
        }
    }
}
