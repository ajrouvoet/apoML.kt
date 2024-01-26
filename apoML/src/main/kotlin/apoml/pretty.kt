package apoml

enum class Assoc {
    LR, RL, None
}

val ApoExp.associativity get() = when (this) {
    is ApoExp.Input -> Assoc.None
    is ApoExp.IntLit -> Assoc.None
    is ApoExp.LetIn -> Assoc.None
    is ApoExp.Mult -> Assoc.LR
    is ApoExp.Plus -> Assoc.LR
    is ApoExp.UnaryMin -> Assoc.RL
    is ApoExp.Var -> Assoc.None
}

val ApoExp.prec get() = when (this) {
    is ApoExp.LetIn  -> 1

    is ApoExp.Plus   -> 2

    is ApoExp.Mult   -> 3

    is ApoExp.Input  -> 4
    is ApoExp.IntLit -> 4
    is ApoExp.UnaryMin -> 4
    is ApoExp.Var    -> 4
}

fun ApoExp.pretty(level: Int = 0): String {
    val (precL, precR) =
        if (this.associativity == Assoc.LR) {
            Pair(this.prec, this.prec + 1)
        } else {
            Pair(this.prec + 1, this.prec)
        }

    val e = when (this) {
        is ApoExp.Input  -> "?($from, $to)"
        is ApoExp.IntLit -> "$value"
        is ApoExp.LetIn -> "let $name = ${fst.pretty(precL)} in ${snd.pretty(precR)}"
        is ApoExp.Mult -> "${left.pretty(precL)} * ${right.pretty(precR)}"
        is ApoExp.Plus -> "${left.pretty(precL)} + ${right.pretty(precR)}"
        is ApoExp.UnaryMin -> "- ${exp.pretty(prec)}"
        is ApoExp.Var -> name
    }

    return if (prec < level) "($e)" else e
}