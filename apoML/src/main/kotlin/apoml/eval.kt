package apoml

typealias Env<Value> = Map<String, Value>
typealias C<Value>   = (Env<Value>) -> Value

fun interface InputProvider<Value> {
    fun input(from: Int, to: Int): Value
}

/**
 * Our API providing the abstract semantics of each operator.
 */
interface Semantics<Value>: InputProvider<Value> {
    fun ofInt(i: Int): Value
    operator fun Value.times(right: Value): Value
    operator fun Value.plus(right: Value): Value
    operator fun Value.unaryMinus(): Value
}

/**
 * From the semantics of each individual operator,
 * we can obtain the semantics of an entire expression.
 *
 * Because expressions are defined inductively,
 * we obtain the semantics of expression inductively as well!
 */
fun <Value> Semantics<Value>.eval(e: ApoExp, env: Env<Value> = mapOf()): Value =
    when (e) {
        is ApoExp.IntLit   -> ofInt(e.value)
        is ApoExp.Mult     -> (eval(e.left, env) * eval(e.right, env))
        is ApoExp.Plus     -> (eval(e.left, env) + eval(e.right, env))
        is ApoExp.UnaryMin -> - eval(e.exp, env)
        is ApoExp.Input    -> input(e.from, e.to)
        is ApoExp.LetIn    -> {
            val bound = eval(e.fst, env)
            eval(e.snd, env.plus(e.name to bound))
        }
        is ApoExp.Var -> env[e.name]!! // exception handling?
    }

class Concrete(val inputProvider: InputProvider<Int>)
    : Semantics<Int>
    , InputProvider<Int> by inputProvider {
    override fun ofInt(i: Int): Int = i
    override fun Int.unaryMinus(): Int = - this
    override fun Int.plus(right: Int): Int = this + right
    override fun Int.times(right: Int): Int = this * right
}

/**
 * The so-called 'initial' semantics is a trivial one that
 * does not actually do any work, but just echo's back the syntax.
 */
object initial: Semantics<ApoExp> {
    override fun ofInt(i: Int) = ApoExp.IntLit(i)
    override fun ApoExp.unaryMinus() = ApoExp.UnaryMin(this)
    override fun ApoExp.plus(right: ApoExp) = ApoExp.Plus(this, right)
    override fun ApoExp.times(right: ApoExp) = ApoExp.Mult(this, right)
    override fun input(from: Int, to: Int): ApoExp = ApoExp.Input(from, to)
}

typealias Interval = Pair<Int, Int>
fun interval(fst: Int, snd: Int) = if (fst < snd) Pair(fst, snd) else Pair(snd, fst)

/**
 * This is a so-called 'abstract interpretation' of the expression.
 * It gives an approximation of the concrete semantics, computing
 * a range (from, to) where the concrete semantics gives a value n s.t. from <= n <= to.
 *
 * This semantics is pure, because we do not have to actually prompt for an input
 * but can simply use its bounds to produce an accurate approximation.
 */
object intervalAnalysis: Semantics<Interval> {
    override fun ofInt(i: Int): Interval = Pair(i, i)
    override fun Interval.unaryMinus(): Interval =
        Pair(-this.second, -this.first)
    override fun Interval.plus(right: Interval): Interval =
        Pair(this.first + right.first, this.second + right.second)
    override fun Interval.times(right: Interval): Interval =
        interval(this.first * right.first, this.second * right.second)
    override fun input(from: Int, to: Int): Interval = Pair(from, to)

}