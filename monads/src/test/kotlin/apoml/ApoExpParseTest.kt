package apoml

import parsec.*
import apoml.ApoExp.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ApoExpParseTest {
    val program = exp() andSkip eos()

    class TestInputProvider: InputProvider<Int> {
        private var next: Int = 0
        override fun input(from: Int, to: Int): Int {
            val cur = next
            next = next + 1

            // clip the value to the range
            return if (cur > to) to else if (cur < from) from else cur
        }
    }

    /**
     * Convenience method to interpret [ApoExp]s with its concrete semantics.
     */
    fun ApoExp.eval(inputProvider: InputProvider<Int> = TestInputProvider()): Int =
        Concrete(inputProvider).eval(this)

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
        expectParse("20") {
            assertEquals(IntLit(20), value)
            assertEquals(20, value.eval())
        }
        expectParse("22") {
            assertEquals(IntLit(22), value)
            assertEquals(22, value.eval())
        }
    }

    @Test
    fun `unary -`() = program.run {
        expectParse("-1") {
            assertEquals(UnaryMin(IntLit(1)), value)
            assertEquals(-1, value.eval())
        }
        expectParse("--1") {
            assertEquals(UnaryMin(UnaryMin(IntLit(1))), value)
            assertEquals(1, value.eval())
        }
    }

    @Test
    fun `plus expressions`() = program.run {
        expectParse("20 + 22") {
            assertEquals(Plus(IntLit(20), IntLit(22)), value)
            assertEquals(42, value.eval())
        }

        expectParse("20+22") {
            assertEquals(Plus(IntLit(20), IntLit(22)), value)
            assertEquals(42, value.eval())
        }

        // right associative plus
        expectParse("1 + 2 + 3") {
            assertEquals(Plus(IntLit(1), Plus(IntLit(2), IntLit(3))), value)
            assertEquals(6, value.eval())
        }

        expectError("1 + 2 +")
    }

    @Test
    fun `+ and * binding`() = program.run {
        expectParse("37 * 3 + 2") {
            assertEquals(
                Plus(Mult(IntLit(37), IntLit(3)), IntLit(2)),
                value
            )
        }

        expectParse("37 * - 3 + 2") {
            assertEquals(
                Plus(Mult(IntLit(37), UnaryMin(IntLit(3))), IntLit(2)),
                value
            )
        }
    }

    @Test
    fun `+ and * binding with parens`() = program.run {
        expectParse("37 * (3 + 2)") {
            assertEquals(
                Mult(IntLit(37), Plus(IntLit(3), IntLit(2))),
                value
            )
            assertEquals(37 * (3 + 2), value.eval())
        }

        expectParse("37 * - (3 + 2)") {
            assertEquals(
                Mult(IntLit(37), UnaryMin(Plus(IntLit(3), IntLit(2)))),
                value
            )

            assertEquals(37 * -(3 + 2), value.eval())
        }
    }

    @Test
    fun `initial semantics`() = initial.run {
        // the initial semantics gives us an embedded DSL to construct the
        // abstract syntax.
        val exp: ApoExp = - ofInt(37) * (ofInt(3) + ofInt(2))
        assertEquals(
            Mult(UnaryMin(IntLit(37)), Plus(IntLit(3), IntLit(2))),
            exp
        )
        assertEquals(- 37 * (3 + 2), exp.eval())
    }

    @Test
    fun `input`() = program.run {
        expectParse("?") {
            assertEquals(Input(), value)
            assertEquals(0, value.eval())
        }

        expectParse("? + ?") {
            assertEquals(Plus(Input(), Input()), value)
            assertEquals(1, value.eval())
        }

        expectParse("?(1,1) + ?(1,2]") {
            assertEquals(Plus(Input(1, 1), Input(1, 1)), value)
            assertEquals(2, value.eval())
            assertEquals(Pair(2, 2), intervalAnalysis.eval(value))
        }

        expectParse("?(0,1) + ?(10,20]") {
            assertEquals(Plus(Input(0, 1), Input(10, 19)), value)
            // second input will clip to lowerbound of the range
            assertEquals(10, value.eval())
            assertEquals(Pair(10, 20), intervalAnalysis.eval(value))
        }
    }
}