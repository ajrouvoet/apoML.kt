package apoml

import parsec.*
import apoml.ApoExp.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ApoExpParseTest {
    val program = apoML

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
    fun `int expressions`() = program.run {
        expectParse("20")
        expectParse("22")
    }

    @Test
    fun `unary -`() = program.run {
        expectParse("-1")
        expectParse("--1")
    }

    @Test
    fun `plus expressions`() = program.run {
        expectParse("20 + 22")
        expectParse("20+22")

        // left associative plus
        expectParse("1 + 2 + 3") // TODO { assert ... }

        expectError("1 + 2 +")
    }

    @Test
    fun `mult expressions`() = program.run {
        expectParse("21 * 2")

        // left associative times
        expectParse("1 * 2 * 3") // TODO { assert ... }

        expectError("1 * 2 *")
    }

    @Test
    fun `+ and * binding`() = program.run {
        expectParse("37 * 3 + 2")

        expectParse("37 * - 3 + 2")
    }

    @Test
    fun `+ and * binding with parens`() = program.run {
        expectParse("37 * (3 + 2)")

        expectParse("37 * - (3 + 2)")
    }

    @Test
    fun `input`() = program.run {
        expectParse("?")

        expectParse("? + ?")

        expectParse("?(1,1) + ?(1,2]")

        expectParse("?(0,1) + ?(10,20]")
    }

    @Test
    fun `let in`() = program.run {
        expectParse("let x = 1 in x + 2")

        expectParse("let x = ? in x + 2")

        expectParse("let x = ?(0, 100) in x * 2")
        expectParse("""
            let x = ?(0, 100) in 
            x * 2
        """)
    }
}