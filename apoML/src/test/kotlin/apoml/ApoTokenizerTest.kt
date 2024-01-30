package apoml

import parsec.*
import apoml.ApoExp.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ApoTokenizerTest {

    @Test
    fun `tokenize ws-separated names`() {
        val toks = "x y z".stream.tokenize().asSequence().toList()
        assertEquals(listOf("x", "y", "z"), toks)
    }

    @Test
    fun `tokenize addition`() {
        val toks = "x+1".stream.tokenize().asSequence().toList()
        assertEquals(listOf("x", "+", "1"), toks)
    }

    @Test
    fun `tokenize input with bounds`() {
        val toks = "?(1,100)".stream.tokenize().asSequence().toList()
        assertEquals(listOf("?", "(", "1", ",", "100", ")"), toks)
    }
}