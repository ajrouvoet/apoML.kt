package parsec.parsec

import parsec.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ParsecTest {

    private fun <T> Parsec<Char, T>.expectError(input: String, onErr: Result.Err<Char>.() -> Unit = {}) =
        when (val res = this.run(input.stream)) {
            is Result.Err -> res.onErr()
            is Result.Ok -> fail("Expected failed parse, but succeeded with value '${res.value}'")
        }

    private fun <T> Parsec<Char, T>.expectParse(input: String, onValue: Result.Ok<Char, T>.() -> Unit = {}) =
        when (val res = this.run(input.stream)) {
            is Result.Err -> fail("Expected successful parse, but failed with msg: ${res.message}")
            is Result.Ok -> res.onValue()
        }

    @Test
    fun `pure`() {
        val parser = pure<Char, _>(1)

        parser.run {
            expectParse("xy") {
                assertEquals(1, value)
            }
            expectParse("xyz") {
                assertEquals(1, value)
            }
        }
    }

    @Test
    fun `match`() {
        val parser = match<Char>({ it == '!' }) { "Unexpected: not an exclamation" }

        parser.run {
            expectParse("!")
            expectParse("!.")
            expectError(".!")
        }
    }

    @Test
    fun `readn`() {
        val parser = readn<Char>(2)

        parser.run {
            expectError("x") // too little input

            expectParse("xy")  { assertEquals(listOf('x', 'y'), value) }
            expectParse("xyz") { assertEquals(listOf('x', 'y'), value) }
        }
    }

    @Test
    fun `exactly and exactly `() {
        val parser = exactly('x') and exactly('y')

        parser.run {
            expectError("x") // too little input

            expectParse("xy")
            expectParse("xyz")
        }
    }

    @Test
    fun `exactly andSkip eos `() {
        val parser = exactly('x') andSkip eos()

        parser.run {
            expectParse("x") { assertEquals('x', value) }

            expectError("") // too little input
            expectError("xy") // too much input
        }
    }

    @Test
    fun `tryOrRewind exactly`() {
        val parser = tryOrRewind(exactly('x'))

        parser.run {
            expectParse("x") {
                assertEquals('x', value)
                assertTrue(remainder.next().isNone())
            }

            expectError("y") {
                assertTrue(remainder.next().isSome())
            }
        }
    }
}