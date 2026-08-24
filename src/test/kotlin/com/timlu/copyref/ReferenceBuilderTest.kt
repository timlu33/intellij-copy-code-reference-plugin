package com.timlu.copyref

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReferenceBuilderTest {

    @Test
    fun `returns null when there is no selection`() {
        val editor = mockk<Editor>()
        val selectionModel = mockk<com.intellij.openapi.editor.SelectionModel>()
        every { editor.selectionModel } returns selectionModel
        every { selectionModel.hasSelection() } returns false

        val result = ReferenceBuilder.buildReference(
            mockk<Project>(),
            editor,
            mockk<VirtualFile>()
        )

        assertNull(result)
    }

    @Test
    fun `builds single line reference`() {
        val project = mockk<Project>()
        val editor = mockk<Editor>()
        val selectionModel = mockk<com.intellij.openapi.editor.SelectionModel>()
        val document = mockk<Document>()
        val file = mockk<VirtualFile>()

        every { editor.selectionModel } returns selectionModel
        every { selectionModel.hasSelection() } returns true
        every { selectionModel.selectionStart } returns 0
        every { selectionModel.selectionEnd } returns 4
        every { editor.document } returns document
        every { document.getLineNumber(0) } returns 0
        every { document.getLineNumber(4) } returns 0
        every { project.basePath } returns null
        every { file.name } returns "Main.kt"

        assertEquals("@Main.kt#L1", ReferenceBuilder.buildReference(project, editor, file))
    }

    @Test
    fun `builds multi line reference`() {
        val project = mockk<Project>()
        val editor = mockk<Editor>()
        val selectionModel = mockk<com.intellij.openapi.editor.SelectionModel>()
        val document = mockk<Document>()
        val file = mockk<VirtualFile>()

        every { editor.selectionModel } returns selectionModel
        every { selectionModel.hasSelection() } returns true
        every { selectionModel.selectionStart } returns 0
        every { selectionModel.selectionEnd } returns 10
        every { editor.document } returns document
        every { document.getLineNumber(0) } returns 0
        every { document.getLineNumber(10) } returns 2
        every { project.basePath } returns null
        every { file.name } returns "Main.kt"

        assertEquals("@Main.kt#L1-3", ReferenceBuilder.buildReference(project, editor, file))
    }
}
