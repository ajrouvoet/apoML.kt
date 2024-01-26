package apoml

/**
 * Our API providing the abstract semantics of each operator.
 */
interface Semantics<Value> {
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
fun <Value> Semantics<Value>.eval(e: ApoExp): Value =
    when (e) {
        is ApoExp.IntLit   -> ofInt(e.value)
        is ApoExp.Mult     -> eval(e.left) * eval(e.right)
        is ApoExp.Plus     -> eval(e.left) + eval(e.right)
        is ApoExp.UnaryMin -> - eval(e.exp)
    }

object concrete: Semantics<Int> {
    override fun ofInt(i: Int): Int = i
    override fun Int.unaryMinus(): Int = - this
    override fun Int.plus(right: Int): Int = this + right
    override fun Int.times(right: Int): Int = this * right
}

/**
 * Convenience method to interpret [ApoExp]s with its concrete semantics.
 */
fun ApoExp.eval(): Int = concrete.eval(this)

/**
 * The so-called 'initial' semantics is a trivial one that
 * does not actually do any work, but just echo's back the syntax.
 */
object initial: Semantics<ApoExp> {
    override fun ofInt(i: Int) = ApoExp.IntLit(i)
    override fun ApoExp.unaryMinus() = ApoExp.UnaryMin(this)
    override fun ApoExp.plus(right: ApoExp) = ApoExp.Plus(this, right)
    override fun ApoExp.times(right: ApoExp) = ApoExp.Mult(this, right)
}
