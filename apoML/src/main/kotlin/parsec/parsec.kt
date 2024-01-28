package parsec

import arrow.core.*

/**
 * What is a Parser of Cs for T?
 *
 * A parser of Cs for T is a thing that takes a stream of such Cs,
 * It will consume some prefix of that stream and then either
 * give you a T value, or an error value, as well as the remainder of the stream.
 */
fun interface Parsec<C, out T> {
    fun run(s: Stream<C>): Result<C, T>
}

// We are going to define many parsers, but we are going to define them
// in layers of abstraction.
//
// In the beginning you will see some primitives that directly act on streams.
// But as we move up the abstraction levels, this will quickly disappear
// and we will only interact with the api of lower abstraction levels.

/**
 * A parser that immediately succeeds with parse result [v]
 * without consuming any input.
 */
fun <C, T> pure(v: T): Parsec<C, T> = TODO()

/**
 * A parser that always fails with [msg] as error message,
 * without consuming any input.
 */
fun <C, T> fail(msg: String): Parsec<C, T> = TODO()

/**
 * This is a utility to define recursive parser,
 * without overflowing the stack immediately.
 *
 * You may ignore it for now.
 */
fun <C, T> rec(factory: () -> Parsec<C, T>) = Parsec<C, T> { s ->
    factory().run(s)
}

/**
 * A parser that consumes a single input element and succeeds if
 * that element satisfies the predicate [pred].
 * If it doesn't, then an error is produced by calling [onErr] on the consumed element.
 */
fun <C> match(pred: (C) -> Boolean, onErr: (C) -> String): Parsec<C, C> = TODO()

/**
 * Specialization of [match] that matches the end of the stream.
 */
fun <C> eos(): Parsec<C, Unit> = TODO()

/**
 * Tries to read [n] input elements and succeeds iff that works out.
 */
fun <C> readn(n: Int): Parsec<C, List<C>> =
    // nothing wrong with some imperative code
    // within a strong functional abstraction if that is more speedy...
    TODO()

/**
 * Functorial action
 */
fun <C, T, S> Parsec<C, T>.map(f: (T) -> S): Parsec<C, S> = TODO()

/**
 * Utility to give a more precise error message.
 */
fun <C, T> Parsec<C, T>.mapError(onErr: (String) -> String): Parsec<C, T> = Parsec { s ->
    when (val res = this@mapError.run(s)) {
        is Result.Err -> res.copy(message = onErr(res.message))
        is Result.Ok  -> res
    }
}

/**
 * Filter the result of [this] based on [pred].
 * If [pred] rejects a result, we produce the error [onErr].
 */
fun <C, T> Parsec<C,T>.filter(pred: (T) -> Boolean, onErr: (T) -> String): Parsec<C, T> = TODO()

/**
 * Monadic action: value-dependent sequencing of parsers.
 */
fun <C, T, S> Parsec<C,T>.flatMap(k: (T) -> Parsec<C, S>): Parsec<C, S> = TODO()

/**
 * Parser that sequences [this] and [that].
 * If either fails, the produced parser fails.
 * If both succeed, then both their results are given.
 */
infix fun <C,S,T> Parsec<C,S>.and(that: Parsec<C, T>): Parsec<C, Pair<S,T>> = TODO()

/**
 * Parse [p] but rewind the consumed input when [p] fails.
 */
fun <C,T> tryOrRewind(p: Parsec<C, T>): Parsec<C, T> = TODO()

/**
 * Parser that sequences [this] or [that] instead if [this] fails.
 */
infix fun <C,T> Parsec<C,T>.or(that: Parsec<C, T>): Parsec<C ,T> = TODO()

/**
 * Parse [this] as many times as possible, until it fails.
 * The input consumed in the failing attempt will be rewinded.
 */
fun <C,T> Parsec<C,T>.many(): Parsec<C, List<T>> = TODO()

// Useful combinators that are defined compositionally
// ---------------------------------------------------

fun <C> exactly(tk: C): Parsec<C, C> = match({ it == tk }) { "Expected $tk, got $it" }

fun <C> exactly(tks: List<C>): Parsec<C, List<C>> = readn<C>(tks.size)
    .mapError { "Expected $tks, but stream ended prematurely."}
    .filter({ it == tks }) { "Expected $tks, but got $it" }

/**
 * Parser that sequences [this] [and] [that] but only keep the result of [that].
 */
infix fun <C,S,T> Parsec<C,S>.skipAnd(that: Parsec<C, T>): Parsec<C, T> = TODO()

/**
 * Parser that sequences [this] [and] [that] but only keep the result of [this].
 */
infix fun <C,S,T> Parsec<C,S>.andSkip(that: Parsec<C, T>): Parsec<C, S> = TODO()

fun <C,T> Parsec<C, T>.plus() = this and this.many()


/**
 * Utility to turn the non-empty list (a pair of an element and a list) into a list.
 * Useful to simplify the output of [plus].
 */
fun <T> Pair<T, List<T>>.cons(): List<T> = first.prependTo(second)

fun <C,T> choice(parsers: List<Parsec<C, T>>, onErr: String = "No match"): Parsec<C, T> = TODO()

fun <C,T> choice(vararg parsers: Parsec<C, T>, onErr: String = "No match"): Parsec<C, T> =
    choice(parsers.toList(), onErr)

val <C,T> Parsec<C, T>.optional get(): Parsec<C, Option<T>> =
    this.map { t -> t.some() } or pure(none())

