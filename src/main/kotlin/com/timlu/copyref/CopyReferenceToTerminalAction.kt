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

                val content = try {
                    terminalManager.getContainer(targetWidget)?.content
                } catch (_: Exception) {
                    null
                }
                if (content != null) {
                    toolWindow.contentManager.setSelectedContent(content, true)
                }

                writeToWidget(project, targetWidget, text)
            } catch (ex: Exception) {
                LOG.warn("Failed to paste to terminal", ex)
                notify(project, "Failed: ${ex.message}", NotificationType.ERROR)
            }
        }, true)
    }

    private fun findPreferredWidget(
        manager: TerminalToolWindowManager,
        widgets: Collection<TerminalWidget>
    ): TerminalWidget? {
        for (widget in widgets) {
            val displayName = try {
                manager.getContainer(widget)?.content?.displayName?.lowercase()
            } catch (_: Exception) {
                null
            } ?: continue
            if (PREFERRED_NAMES.any { displayName.contains(it) }) {
                return widget
            }
        }
        return null
    }

    private fun writeToWidget(project: Project, widget: TerminalWidget, text: String) {
        // Log available methods for debugging ReworkedTerminalWidget
        LOG.info("Widget class: ${widget.javaClass.name}")
        val allMethods = widget.javaClass.methods.map { "${it.name}(${it.parameterTypes.joinToString(", ") { it.simpleName }})" }
        LOG.info("Available methods: ${allMethods.filter { it.contains("send") || it.contains("type") || it.contains("paste") || it.contains("write") }}")

        val connector = runCatching { widget.ttyConnector }.getOrNull()
        if (connector != null) {
            runCatching { connector.write(text) }
                .onSuccess { return }
                .onFailure { LOG.warn("ttyConnector.write() failed", it) }
        }

        // Fallback: reflection typeText
        val methods = widget.javaClass.methods
        val typeMethod = methods.firstOrNull {
            it.name == "typeText" && it.parameterCount == 1
                    && it.parameterTypes[0] == String::class.java
        }
        if (typeMethod != null) {
            runCatching {
                typeMethod.isAccessible = true
                typeMethod.invoke(widget, text)
            }.onSuccess { return }
                .onFailure { LOG.warn("typeText() failed", it) }
        }

        // Fallback: reflection pasteText
        val pasteMethod = methods.firstOrNull {
            it.name == "pasteText" && it.parameterCount == 1
                    && it.parameterTypes[0] == String::class.java
        }
        if (pasteMethod != null) {
            runCatching {
                pasteMethod.isAccessible = true
                pasteMethod.invoke(widget, text)
            }.onSuccess { return }
                .onFailure { LOG.warn("pasteText() failed", it) }
        }

        // Fallback: try bracketed paste mode via sendCommandToExecute with escape sequences
        try {
            val sendMethod = methods.firstOrNull {
                it.name == "sendCommandToExecute" && it.parameterCount == 1
                        && it.parameterTypes[0] == String::class.java
            }
            if (sendMethod != null) {
                sendMethod.isAccessible = true
                // Use bracketed paste mode escape sequences
                val pasteText = "\u001B[200~$text\u001B[201~"
                sendMethod.invoke(widget, pasteText)
                LOG.info("Used sendCommandToExecute with bracketed paste mode")
                return
            }
        } catch (ex: Exception) {
            LOG.warn("sendCommandToExecute with bracketed paste failed", ex)
        }

        notify(project, "Cannot write to terminal widget (class: ${widget.javaClass.name})", NotificationType.ERROR)
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
