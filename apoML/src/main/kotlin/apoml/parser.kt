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
val ws = oneOf(" \t\r\n").many()

/**
 * Token parser factory; enables optional whitespace around [this] parser.
 */
fun <T> P<T>.tok() = ws skipAnd this andSkip ws

fun keyword(kw: String) = str(kw).tok()

val int: P<Int> =
    digit.plus()
        .map { (head, tail) ->
            tail
                .joinToString("", prefix = head.toString())
                .toInt(10)
        }

val intLit: P<ApoExp.IntLit> = int.map { ApoExp.IntLit(it) }

val id: P<String> = satisfy({ it.isLetterOrDigit() || it in "_'" }) {
        "Character '$it' not allowed in an identifier"
    }
    .plus()
    .map { (head, tail) -> tail.joinToString("", prefix = head.toString()) }

fun plusExp(): P<ApoExp.Plus> =
    ( rec { exp2() }
      andSkip exactly('+').tok()
      and rec { exp() }
    ) .map { (l, r) -> ApoExp.Plus(l, r) }

fun letExp(): P<ApoExp.LetIn> = (
    pure<Char,_> { id: String, e1: ApoExp, e2: ApoExp -> ApoExp.LetIn(id, e1, e2) }
    * (keyword("let") skipAnd id andSkip keyword("="))
    * rec { exp1() }
    * (keyword("in") skipAnd rec { exp() })
)

fun exp(): P<ApoExp> = choice(
    letExp(),
    exp1()
)

/** Expressions with the least binding power */
fun exp1(): P<ApoExp> = choice(
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
      * (int andSkip exactly(',').tok())
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

val varExp: P<ApoExp.Var> = (
    pure<Char, _> { name: String -> ApoExp.Var(name) }
    * id
)

/** Expressions at the binding power of parens */
fun exp3(): P<ApoExp> = choice(
    parenthesized,
    input,
    unaryMin,
    intLit,
    varExp
)

val apoML = exp() andSkip (ws and eos())