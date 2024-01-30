package apoml

import arrow.core.none
import arrow.core.some
import parsec.*

val ws: Parsec<Char, Unit> = match<Char>({ it.isWhitespace() }).many().map {}

fun token(p: Parsec<Char, String>): Parsec<Char, String> = ws skipAnd p andSkip ws
fun token(kw: String): Parsec<Char, String> = token(exactly(kw.toList()).map { kw })

val anychar: Parsec<Char, String> = any<Char>().map { it.toString() }

val ident: Parsec<Char, String> = (
    match<Char>({ it.isLetter() || it == '_' }) { "Invalid character for variable name '$it'" }
    and match<Char>({ it.isLetter() || it.isDigit() || it == '_' }) { "Invalid character for variable name '$it'" } .many()
).map { (head, tail) -> tail.joinToString("", prefix=head.toString()) }

val tokenizer: Parsec<Char, String> = choice(
    // reserved keywords
    token("let"),
    token("in"),
    // punctuation
    token(","),
    token("?"),
    token("("),
    token(")"),
    token("["),
    token("]"),
    token("*"),
    token("+"),
    token("-"),
    // int literals
    token(digit.plus().map { (hd, tl) -> tl.joinToString("", prefix = hd.toString()) }),
    // identifiers
    token(ident),
    // catch all garbage
    anychar
)

/**
 * Apply the ApoML tokenizer to [this] char stream,
 * producing a stream of tokens represented as [String]s.
 */
fun Stream<Char>.tokenize(): Stream<String> = Stream {
    when (val res = tokenizer.run(this@tokenize)) {
        is Result.Err -> none()
        is Result.Ok -> Pair(res.value, res.remainder.tokenize()).some()
    }
}