package apoml

/**
 * The type of values that ApoExps can evaluate to
 */
typealias Value = Int

/**
 * An environment gives a value to every variable that is in
 * scope in an ApoExp.
 *
 * For example, consider:
 *
 * let x = 41 in (x + 1)
 *
 * The environment for the expression (x + 1) at evaluation time
 * will be mapOf(x to 41).
 */
typealias Env = Map<String, Value>

/**
 * The interpreter for ApoExps.
 *
 * The [env] parameter is the surrounding environment for [this] expression.
 */
fun ApoExp.eval(env: Env = mapOf()): Value = TODO()