package apoml

sealed interface ApoExp {
    data class IntLit(val value: Int): ApoExp

    data class UnaryMin(val exp: ApoExp): ApoExp

    data class Mult(val left: ApoExp, val right: ApoExp): ApoExp

    data class Plus(val left: ApoExp, val right: ApoExp): ApoExp
}