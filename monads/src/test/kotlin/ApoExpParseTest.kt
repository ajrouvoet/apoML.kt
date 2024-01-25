import parsec.*
import syntax.*
import syntax.ApoExp.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ApoExpParseTest {

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
    fun `intExp parser`() {
        intLit.run {
            expectParse("20 + 22") {
                assertEquals(IntLit(20), value)
            }
        }
    }

    @Test
    fun `unary -`() {
        exp3().run {
            expectParse("-1") {
                assertEquals(UnaryMin(IntLit(1)), value)
            }
            expectParse("--1") {
                assertEquals(UnaryMin(UnaryMin(IntLit(1))), value)
            }
        }
    }

    @Test
    fun `plusExp parser`() {
        (plusExp() andSkip eos()).run {
            expectParse("20 + 22") {
                assertEquals(Plus(IntLit(20), IntLit(22)), value)
            }

            expectParse("20+22") {
                assertEquals(Plus(IntLit(20), IntLit(22)), value)
            }

            // right associative plus
            expectParse("1 + 2 + 3") {
                assertEquals(Plus(IntLit(1), Plus(IntLit(2), IntLit(3))), value)
            }

            expectError("1 + 2 +")
        }
    }

    @Test
    fun `+ and * binding`() {
        (exp1() andSkip eos()).run {
            expectParse("37 * 3 + 2") {
                assertEquals(
                    Plus(Mult(IntLit(37), IntLit(3)), IntLit(2)),
                    value
                )
            }
        }
    }

    @Test
    fun `+ and * binding with parens`() {
        (exp1() andSkip eos()).run {
            expectParse("37 * (3 + 2)") {
                assertEquals(
                    Mult(IntLit(37), Plus(IntLit(3), IntLit(2))),
                    value
                )
            }
        }
    }
}