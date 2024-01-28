package parsec

import java.util.function.Predicate

interface CharParser: Parsec<Char, Char> {
    fun satisfy(c: Char): Boolean
    fun errMsg(actual: Char): String

    override fun run(s: Stream<Char>): Result<Char, Char> = match({ satisfy(it) }, ::errMsg).run(s)
}

fun satisfy(pred: Predicate<Char>, onErr: (Char) -> String) = object: CharParser {
    override fun satisfy(c: Char): Boolean = pred.test(c)
    override fun errMsg(actual: Char): String = onErr(actual)
}

fun oneOf(chrs: CharSequence) = satisfy({ chrs.contains(it) }) {
    "Expected one of [$chrs], got '$it'"
}

fun noneOf(chrs: CharSequence) = satisfy({ !chrs.contains(it) }) {
    "Expected none of [$chrs], got '$it'"
}

val letter = satisfy({ it.isLetter() }) { actual -> "Expected letter, got $actual"}
val digit  = satisfy({ it.isDigit() }) { actual -> "Expected letter, got $actual"}
fun chr(c: Char) = satisfy({ it == c }) { actual -> "Expected '$c', got $actual"}

fun str(s: String) = exactly(s.toList())

/**
 * Optional whitespace parser
 */
val ws = oneOf(" \t\r\n").many()
