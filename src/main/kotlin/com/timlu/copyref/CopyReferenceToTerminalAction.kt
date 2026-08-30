package com.timlu.copyref

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.ui.TerminalWidget
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.awt.datatransfer.StringSelection

class CopyReferenceToTerminalAction : AnAction() {

    companion object {
        private val LOG = Logger.getInstance(CopyReferenceToTerminalAction::class.java)
        private val PREFERRED_NAMES = listOf("opencode", "claude")
    }

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

        CopyPasteManager.getInstance().setContents(StringSelection(reference))
        pasteToTerminal(project, reference)
    }

    private fun pasteToTerminal(project: Project, text: String) {
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID)

        if (toolWindow == null) {
            notify(project, "Terminal tool window not found", NotificationType.ERROR)
            return
        }

        toolWindow.activate({
            try {
                val terminalManager = TerminalToolWindowManager.getInstance(project)
                val widgets = terminalManager.terminalWidgets

                if (widgets.isEmpty()) {
                    notify(project, "No terminal sessions found", NotificationType.WARNING)
                    return@activate
                }

                val targetWidget = findPreferredWidget(terminalManager, widgets)
                    ?: widgets.first()

                val content = runCatching {
                    terminalManager.getContainer(targetWidget)?.content
                }.getOrNull()
                if (content != null) {
                    toolWindow.contentManager.setSelectedContent(content, true)
                }

                writeToWidget(project, targetWidget, text)
            } catch (ex: Exception) {
                LOG.warn("Failed to paste to terminal", ex)
                notify(project, "Failed: ${ex.message ?: "Unknown error"}", NotificationType.ERROR)
            }
        }, true)
    }

    private fun findPreferredWidget(
        manager: TerminalToolWindowManager,
        widgets: Collection<TerminalWidget>
    ): TerminalWidget? {
        for (widget in widgets) {
            val displayName = runCatching {
                manager.getContainer(widget)?.content?.displayName?.lowercase()
            }.getOrNull() ?: continue

            if (PREFERRED_NAMES.any(displayName::contains)) {
                return widget
            }
        }
        return null
    }

    private fun writeToWidget(project: Project, widget: TerminalWidget, text: String) {
        // The reworked terminal exposes a command-oriented API. Prefer it over
        // implementation-specific reflection or direct TTY writes so this action
        // works across supported terminal implementations.
        try {
            widget.executeCommand(text)
            return
        } catch (ex: Exception) {
            LOG.warn("executeCommand() failed", ex)
        }

        // Keep the copied reference available even when the terminal session is
        // temporarily unavailable; the clipboard operation has already succeeded.
        notify(
            project,
            "Reference copied, but could not send it to the terminal",
            NotificationType.WARNING
        )
    }

    private fun notify(project: Project, message: String, type: NotificationType) {
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Copy Code Reference")
                .createNotification(message, type)
                .notify(project)
        } catch (_: Exception) {
            LOG.info("Notification: $message")
        }
    }
}
