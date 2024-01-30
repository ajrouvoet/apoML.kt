package apoml

import arrow.core.*
import parsec.Parsec
import parsec.*

/**
 * Shortcut for String-stream parsers
 */
private typealias P<T> = Parsec<String, T>

val int: P<Int> = any<String>()
    .flatMap {
        when (val i = it.toIntOrNull()) {
            null -> fail("Expected int, got '$it'")
            else -> pure(i)
        }
    }

val id: P<String> = any<String>()
    .filter({ "Not a valid identifier '$it'" }) {
        it.matches(Regex("[a-zA-Z_][a-zA-Z0-9]*"))
    }

val intLit: P<ApoExp.IntLit> = int.map { ApoExp.IntLit(it) }
fun additionParser(): P<ApoExp> =
    (rec { exp2() } * (exactly("+") skipAnd rec { exp2() }).plus())
        .map { (e, es) -> addition(e, es) }

fun letExp(): P<ApoExp.LetIn> = (
    ((exactly("let") skipAnd id andSkip exactly("="))
        * rec { exp1() }
        * (exactly("in") skipAnd rec { exp() })
    ) .map { (name, lhs, rhs) -> ApoExp.LetIn(name, lhs, rhs) }
)

fun exp(): P<ApoExp> = choice(
    letExp(),
    exp1()
)

fun exp1(): P<ApoExp> = choice(
    additionParser(),
    exp2(),
)

fun multiplicationParser(): P<ApoExp> =
    ( rec { exp3() }
      * (exactly("*") skipAnd rec { exp3() }).plus()
    ) .map { (e, es) -> multiplication(e, es) }

/** Expressions at the binding power of multiplication */
fun exp2(): P<ApoExp> = choice(
    multiplicationParser(),
    exp3()
)

val unaryMin: P<ApoExp.UnaryMin> =
    (exactly("-") skipAnd rec { exp3() })
        .map { e -> ApoExp.UnaryMin(e) }

val parenthesized: P<ApoExp> =
    exactly("(") skipAnd rec { exp() } andSkip exactly(")")

private enum class RangeDelim {
    Open, Closed
}

private val lRangeDelimiter = choice(
    exactly("(") .map { RangeDelim.Open },
    exactly("[") .map { RangeDelim.Closed },
)

private val rRangeDelimiter = choice(
    exactly(")") .map { RangeDelim.Open },
    exactly("]") .map { RangeDelim.Closed },
)

val range: P<Pair<Int, Int>> =
     ( lRangeDelimiter
         * (int andSkip exactly(","))
         * int
         * rRangeDelimiter
     ).map { (l: RangeDelim, from: Int, to: Int, r: RangeDelim) ->
         val lb = if (l == RangeDelim.Open) from else from + 1
         val ub = if (r == RangeDelim.Open) to else to - 1
         Pair(lb, ub)
     }

val input: P<ApoExp.Input> =
    ( exactly("?") skipAnd range.optional ).map { range ->
        val (from, to) = range.getOrElse { Pair(Int.MIN_VALUE, Int.MAX_VALUE) }
        ApoExp.Input(from, to)
    }

val varExp: P<ApoExp.Var> = (
    id .map { ApoExp.Var(it) }
)

/** Expressions at the binding power of parens */
fun exp3(): P<ApoExp> = choice(
    parenthesized,
    input,
    unaryMin,
    intLit,
    varExp
)

val apoML = exp() andSkip eos()