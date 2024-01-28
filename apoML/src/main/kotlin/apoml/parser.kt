package apoml

import parsec.Parsec
import parsec.*

/**
 * Shortcut for Char-stream parsers
 */
private typealias P<T> = Parsec<Char, T>

/**
 * Token parser factory; enables optional whitespace around [this] parser.
 */
fun <T> P<T>.tok() = ws skipAnd this andSkip ws

/**
 * The big ApoML parser assignment
 */
val apoML: P<ApoExp> = TODO()