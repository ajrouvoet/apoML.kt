package apoml

import parsec.Result
import parsec.stream
import java.nio.file.Path
import java.util.Scanner
import kotlin.io.path.bufferedReader
import kotlin.io.path.readText
import kotlin.system.exitProcess

class CLIInputProvider: InputProvider<Int> {
    private val scanner = Scanner(System.`in`)

    override fun input(from: Int, to: Int): Int {
        println("> Enter a number between $from and $to")
        print("< ")

        // TODO error handling?
        return scanner.nextInt(10)
    }

}

fun main(args: Array<String>) {
    // check arguments
    if (args.size < 1) {
        println("Too few arguments provided. Expected a path.")
        exitProcess(1)
    }

    // read the file
    val path = Path.of(args[0])
    val content = path.readText()

    // parse this thing
    val ast = when (val res = apoML.run(content.stream)) {
        is Result.Err -> {
            println("Invalid ApoML program:")
            println(res.message.prependIndent("\t"))
            exitProcess(1)
        }

        is Result.Ok -> res.value
    }

    // interpret it!
    val result = Concrete(CLIInputProvider()).run {
        eval(ast)
    }

    // inline the let expressions to get a simple symbolic expression
    val inlined = initial.run {
        eval(ast)
    }

    println("-".repeat(80))
    println("${inlined.pretty()} = $result")
}