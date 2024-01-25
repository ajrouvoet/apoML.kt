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

// Layer 1: leaf primitives

/**
 * A parser that immediately succeeds with parse result [v]
 * without consuming any input.
 */
fun <C, T> pure(v: T) = Parsec<C, T> { s ->
    Result.Ok(s, v)
}

/**
 * A parser that always fails with [msg] as error message,
 * without consuming any input.
 */
fun <C, T> fail(msg: String) = Parsec<C, T> { s ->
    Result.Err(s, msg)
}

/**
 * This is a utility to enable recursive parser definitions.
 * Without smashing the stack immediately.
 */
fun <C, T> rec(factory: () -> Parsec<C, T>) = Parsec<C, T> { s ->
    factory().run(s)
}

/**
 * A parser that consumes a single input element and succeeds if
 * that element satisfies the predicate [pred].
 * If it doesn't, then an error is produced by calling [onErr] on the consumed element.
 */
fun <C> match(pred: (C) -> Boolean, onErr: (C) -> String): Parsec<C, C> = Parsec { s ->
    when (val res = s.next()) {
        None -> Result.Err(s, "Unexpected end of stream")
        is Some -> {
            val (c, ns) = res.value
            if (pred(c)) {
                Result.Ok(ns, c)
            } else {
                Result.Err(ns, onErr(c))
            }
        }
    }
}

/**
 * Specialization of [match] that matches the end of the stream.
 */
fun <C> eos(): Parsec<C, Unit> = Parsec { s ->
    if (s.next().isNone()) Result.Ok(s, Unit)
    else Result.Err(s, "Expected end of stream, still have: $s")
}

/**
 * Tries to read [n] input elements and succeeds iff that works out.
 */
fun <C> readn(n: Int): Parsec<C, List<C>> = Parsec { s ->
    try {
        // nothing wrong with some imperative code
        // within a strong functional abstraction if that is more speedy...
        var stream = s
        val result = mutableListOf<C>()

        repeat(n) {
            val (c, nxt) = stream.next().getOrElse { throw UnexpectedEOS }
            stream = nxt
            result.add(c)
        }

        Result.Ok(stream, result)
    } catch (e: UnexpectedEOS) {
        Result.Err(s, "Expected $n more inputs, but stream ends before that.")
    }
}

// Layer 2: derived leafs
// These construct parsers using the primitives

fun <C> exactly(tk: C): Parsec<C, C> = match({ it == tk }) { "Expected $tk, got $it" }

fun <C> exactly(tks: List<C>): Parsec<C, List<C>> = readn<C>(tks.size)
    .mapError { "Expected $tks, but stream ended prematurely."}
    .filter({ it == tks }) { "Expected $tks, but got $it" }

// Layer 3: primitives that construct parsers from other parsers

/**
 * Functorial action
 */
fun <C, T, S> Parsec<C,T>.map(f: (T) -> S) = Parsec { s ->
    when (val res = this@map.run(s)) {
        is Result.Err -> res
        is Result.Ok  -> Result.Ok(res.remainder, f(res.value))
    }
}

fun <C, T> Parsec<C,T>.filter(pred: (T) -> Boolean, onErr: (T) -> String) = Parsec { s ->
    when (val res = this@filter.run(s)) {
        is Result.Err -> res
        is Result.Ok  ->
            if (pred(res.value)) res
            else Result.Err(res.remainder, onErr(res.value))
    }
}

/**
 * Monadic action
 */
fun <C, T, S> Parsec<C,T>.flatMap(k: (T) -> Parsec<C, S>): Parsec<C, S> = Parsec { s ->
    when (val res = this@flatMap.run(s)) {
        is Result.Err -> res
        is Result.Ok  -> k(res.value).run(res.remainder)
    }
}

/**
 * Parser that sequences [this] and [that].
 * If either fails, the produced parser fails.
 * If both succeed, then both their results are given.
 */
infix fun <C,S,T> Parsec<C,S>.and(that: Parsec<C, T>): Parsec<C, Pair<S,T>> = Parsec { str ->
    this.run(str).flatMap { s, rem ->
        that.run(rem).map { t ->
            Pair(s, t)
        }
    }
}

/**
 * Parse [p] but rewind the consumed input when [p] fails.
 */
fun <C,T> tryOrRewind(p: Parsec<C, T>) = Parsec { s ->
    when (val res = p.run(s)) {
        is Result.Ok -> res
        is Result.Err -> Result.Err(s, res.message) /* rewind on failure */
    }
}

/**
 * Parser that sequences [this] or [that] instead if [this] fails.
 */
infix fun <C,T> Parsec<C,T>.or(that: Parsec<C, T>) = Parsec { s ->
    when (val res = tryOrRewind(this).run(s)) {
        is Result.Err -> that.run(s)
        is Result.Ok  -> res
    }
}

/**
 * Parse [p] but don't consume any input.
 * If [p] errors, the error is propagated and input is consumed.
 */
fun <C,T> lookahead(p: Parsec<C, T>) = Parsec { s ->
    when (val res = p.run(s)) {
        is Result.Ok -> Result.Ok(s, res.value) /* rewind on success */
        is Result.Err -> res
    }
}


/**
 * Parse [this] as many times as possible, until it fails.
 * The input consumed in the failing attempt will be rewinded.
 */
fun <C,T> Parsec<C,T>.many() = object: Parsec<C, List<T>> {
    val p: Parsec<C, T> = this@many

    fun run(s: Stream<C>, accumulator: List<T> = listOf()): Result.Ok<C, List<T>> = tryOrRewind(p)
        .run(s)
        .let {
            when (it) {
                is Result.Err -> Result.Ok(it.remainder, accumulator)
                is Result.Ok  -> run(it.remainder, accumulator + listOf(it.value))
            }
        }

    // We have seen an imperative loop in [readn].
    // Here we see the functional variant, using recursion and an accumulator.
    override fun run(s: Stream<C>): Result<C, List<T>> = run(s, listOf())
}

// Level 4

/**
 * Parser that sequences [this] [and] [that] but only keep the result of [that].
 */
infix fun <C,S,T> Parsec<C,S>.skipAnd(that: Parsec<C, T>): Parsec<C, T> =
    (this and that)
        .map { it.second }

/**
 * Parser that sequences [this] [and] [that] but only keep the result of [this].
 */
infix fun <C,S,T> Parsec<C,S>.andSkip(that: Parsec<C, T>): Parsec<C, S> =
    (this and that)
        .map { it.first }

fun <C,T> Parsec<C, T>.plus() = this and this.many()

fun <C,T,S> Parsec<C, T>.manyTill(end: Parsec<C, S>): Parsec<C, List<T>> =
    end.map { listOf<T>() } or this.flatMap { x ->
        this.manyTill(end).map { xs ->
            x.prependTo(xs)
        }
    }

fun <C,T> choice(parsers: List<Parsec<C, T>>, onErr: String = "No match"): Parsec<C, T> =
    if (parsers.isEmpty()) fail(onErr) else {
        parsers.first() or choice(parsers.drop(1))
    }

fun <C,T> choice(vararg parsers: Parsec<C, T>, onErr: String = "No match"): Parsec<C, T> =
    choice(parsers.toList(), onErr)

val <C,T> Parsec<C, T>.optional get() = this.flatMap { t -> pure(t.some()) } or pure(none())

fun <C, T> Parsec<C, T>.mapError(onErr: (String) -> String): Parsec<C, T> = Parsec { s ->
    when (val res = this@mapError.run(s)) {
        is Result.Err -> res.copy(message = onErr(res.message))
        is Result.Ok  -> res
    }
}

