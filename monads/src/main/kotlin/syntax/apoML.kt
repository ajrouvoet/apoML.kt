package syntax

import parsec.Parsec
import parsec.*

private typealias P<T> = Parsec<Char, T>

sealed interface ApoExp {
    data class IntLit(val value: Int): ApoExp

    data class UnaryMin(val exp: ApoExp): ApoExp

    data class Mult(val left: ApoExp, val right: ApoExp): ApoExp

    data class Plus(val left: ApoExp, val right: ApoExp): ApoExp
}

val nbws = oneOf(" \t").many()
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
    (rec { exp2() } and (exactly('+').tok() skipAnd rec { exp1() }))
        .map { (l, r) -> ApoExp.Plus(l, r) }

// weakly binding operators
fun exp1(): P<ApoExp> = choice(
    plusExp(),
    exp2(),
)

fun multExp(): P<ApoExp.Mult> =
    (rec { exp3() } and (exactly('*').tok() skipAnd rec { exp2() }))
        .map { (l, r) -> ApoExp.Mult(l, r) }

// strongly binding operators
fun exp2(): P<ApoExp> = choice(
    multExp(),
    exp3()
)

val unaryMin: P<ApoExp.UnaryMin> =
    (exactly('-').tok() skipAnd rec { exp3() })
        .map { e -> ApoExp.UnaryMin(e) }

val parenthesized: P<ApoExp> =
    exactly('(').tok() skipAnd rec { exp1() } andSkip exactly(')').tok()

fun exp3(): P<ApoExp> = choice(
    parenthesized,
    unaryMin,
    intLit,
)
