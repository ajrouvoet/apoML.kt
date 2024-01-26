package apoml

import arrow.core.getOrElse
import parsec.Parsec
import parsec.*

/**
 * Shortcut for Char-stream parsers
 */
private typealias P<T> = Parsec<Char, T>

/**
 * Optional non-breaking whitespace parser
 */
val nbws = oneOf(" \t").many()

/**
 * Token parser factory; enables optional whitespace around [this] parser.
 */
fun <T> P<T>.tok() = nbws skipAnd this andSkip nbws

val int: P<Int> =
    digit.plus()
        .map { (head, tail) ->
            (listOf(head) + tail)
                .joinToString("")
                .toInt(10)
        }

val intLit: P<ApoExp.IntLit> = int.map { ApoExp.IntLit(it) }

fun plusExp(): P<ApoExp.Plus> =
    ( rec { exp2() }
      andSkip exactly('+').tok()
      and rec { exp() }
    ) .map { (l, r) -> ApoExp.Plus(l, r) }

/** Expressions with the least binding power */
fun exp(): P<ApoExp> = choice(
    plusExp(),
    exp2(),
)

fun multExp(): P<ApoExp.Mult> =
    ( rec { exp3() }
      andSkip exactly('*').tok()
      and rec { exp2() }
    ) .map { (l, r) -> ApoExp.Mult(l, r) }

/** Expressions at the binding power of multiplication */
fun exp2(): P<ApoExp> = choice(
    multExp(),
    exp3()
)

val unaryMin: P<ApoExp.UnaryMin> =
    ( exactly('-').tok()
      skipAnd rec { exp3() }
    ) .map { e -> ApoExp.UnaryMin(e) }

val parenthesized: P<ApoExp> =
    ( exactly('(').tok()
      skipAnd rec { exp() }
      andSkip exactly(')').tok()
    )

private enum class RangeDelim {
    Open, Closed
}

private val lRangeDelimiter = choice(
    exactly('(') .map { RangeDelim.Open },
    exactly('[') .map { RangeDelim.Closed },
)

private val rRangeDelimiter = choice(
    exactly(')') .map { RangeDelim.Open },
    exactly(']') .map { RangeDelim.Closed },
)

val range: P<Pair<Int, Int>> = (
      pure<Char, _> { l: RangeDelim, from: Int, to: Int, r: RangeDelim ->
          val lb = if (l == RangeDelim.Open) from else from + 1
          val ub = if (r == RangeDelim.Open) to else to - 1
          Pair(lb, ub)
      }
      * lRangeDelimiter
      * (int andSkip exactly(','))
      * int
      * rRangeDelimiter
    )

val input: P<ApoExp.Input> =
    ( str("?").tok()
      skipAnd range.optional
    ) .map { range ->
        val (from, to) = range.getOrElse { Pair(Int.MIN_VALUE, Int.MAX_VALUE) }
        ApoExp.Input(from, to)
    }

/** Expressions at the binding power of parens */
fun exp3(): P<ApoExp> = choice(
    parenthesized,
    input,
    unaryMin,
    intLit,
)
