package apoml

sealed interface ApoExp {
    data class IntLit(val value: Int): ApoExp
    data class UnaryMin(val exp: ApoExp): ApoExp
    data class Mult(val left: ApoExp, val right: ApoExp): ApoExp
    data class Plus(val left: ApoExp, val right: ApoExp): ApoExp
    data class Input(val from: Int = Int.MIN_VALUE, val to: Int = Int.MAX_VALUE): ApoExp
    data class LetIn(val name: String, val fst: ApoExp, val snd: ApoExp): ApoExp
    data class Var(val name: String): ApoExp
}

/**
 * Smart constructor for n-ary addition expressions.
 */
fun addition(head: ApoExp, tail: List<ApoExp>) =
    // addition is left-associative
    tail.fold(head) { acc, el -> ApoExp.Plus(acc, el) }

/**
 * Smart constructor for n-ary multiplication expressions.
 */
fun multiplication(head: ApoExp, tail: List<ApoExp>) =
    // multiplication is left-associative
    tail.fold(head) { acc, el -> ApoExp.Mult(acc, el) }
