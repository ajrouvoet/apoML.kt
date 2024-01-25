package parsec

/**
 * Applicative action.
 */
@JvmName("app1")
operator fun <C,T,S> Parsec<C,(T) -> S>.times(that: Parsec<C, T>) =
    this.flatMap { f -> that.map { f(it) }}

/**
 * Uncurried applicative action, or something like that.
 */
@JvmName("app2")
operator fun <C,T1,T2,S> Parsec<C,(T1, T2) -> S>.times(that: Parsec<C, T1>): Parsec<C, (T2) -> S> =
    this.flatMap { f -> that.map { t1: T1 -> { t2: T2 -> f(t1, t2) }}}

/**
 * Uncurried applicative action, or something like that.
 */
@JvmName("app3")
operator fun <C,T1,T2,T3,S> Parsec<C,(T1, T2, T3) -> S>.times(that: Parsec<C, T1>): Parsec<C, (T2, T3) -> S> =
    this.flatMap { f -> that.map { t1: T1 -> { t2: T2, t3: T3 -> f(t1, t2, t3) }}}

/**
 * Uncurried applicative action, or something like that.
 */
@JvmName("app4")
operator fun <C,T1,T2,T3,T4,S> Parsec<C,(T1, T2, T3, T4) -> S>.times(that: Parsec<C, T1>): Parsec<C, (T2, T3, T4) -> S> =
    this.flatMap { f -> that.map { t1: T1 -> { t2: T2, t3: T3, t4: T4 -> f(t1, t2, t3, t4) }}}

/**
 * Uncurried applicative action, or something like that.
 */
@JvmName("app5")
operator fun <C,T1,T2,T3,T4,T5,S> Parsec<C,(T1, T2, T3, T4, T5) -> S>.times(that: Parsec<C, T1>): Parsec<C, (T2, T3, T4, T5) -> S> =
    this.flatMap { f -> that.map { t1: T1 -> { t2: T2, t3: T3, t4: T4, t5: T5 -> f(t1, t2, t3, t4, t5) }}}

/**
 * Uncurried applicative action, or something like that.
 */
@JvmName("app6")
operator fun <C,T1,T2,T3,T4,T5,T6,S> Parsec<C,(T1, T2, T3, T4, T5, T6) -> S>.times(that: Parsec<C, T1>): Parsec<C, (T2, T3, T4, T5, T6) -> S> =
    this.flatMap { f -> that.map { t1: T1 -> { t2: T2, t3: T3, t4: T4, t5: T5, t6: T6 -> f(t1, t2, t3, t4, t5, t6) }}}

