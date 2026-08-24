package com.timlu.copyref

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class CopyReferenceWithEli5ActionTest {

    @Test
    fun `eli5 action exposes an explanation prompt`() {
        val prompt = "Explain the following code to me like I'm 5 years old (ELI5)."
        assertTrue(prompt.contains("ELI5"))
        assertTrue(prompt.contains("like I'm 5 years old"))
    }
}
