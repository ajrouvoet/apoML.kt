package parsec

import arrow.core.Option
import arrow.core.none
import arrow.core.some

object UnexpectedEOS: Exception("Unexpected end of stream")

/**
 * Immutable Stream abstraction
 */
interface Stream<out C> {
    fun next(): Option<Pair<C, Stream<C>>>
}

data class StringView(val seq: CharSequence, val cursor: Int = 0): Stream<Char> {
    override fun next(): Option<Pair<Char, Stream<Char>>> =
            try {
                val n = seq[cursor]
                Pair(n, StringView(seq, cursor + 1)).some()
            }
            catch (e: IndexOutOfBoundsException) { none() }

    override fun toString(): String = seq.drop(cursor).toString()
}

val String.stream get() = StringView(this)