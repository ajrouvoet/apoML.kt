package apoml

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

val intLit: P<ApoExp.IntLit> =
    digit.plus()
        .map { (head, tail) ->
            val value = (listOf(head) + tail)
                .joinToString("")
                .toInt(10)

            ApoExp.IntLit(value)
        }

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

/** Expressions at the binding power of parens */
fun exp3(): P<ApoExp> = choice(
    parenthesized,
    unaryMin,
    intLit,
)
