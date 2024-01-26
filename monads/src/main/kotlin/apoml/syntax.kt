package apoml

sealed interface ApoExp {
    data class IntLit(val value: Int): ApoExp

    data class UnaryMin(val exp: ApoExp): ApoExp

    data class Mult(val left: ApoExp, val right: ApoExp): ApoExp

    data class Plus(val left: ApoExp, val right: ApoExp): ApoExp

    data class Input(val from: Int = Int.MIN_VALUE, val to: Int = Int.MAX_VALUE): ApoExp
}

//sealed interface ApoStmt {
//    data class Exp(val exp: ApoExp)
//    data class LetIn(val name: String, val fst: ApoStmt, val snd: ApoStmt)
//}