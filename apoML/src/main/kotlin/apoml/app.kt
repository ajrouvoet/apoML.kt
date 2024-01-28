package apoml

import parsec.Result
import parsec.stream
import java.nio.file.Path
import java.util.Scanner
import kotlin.io.path.bufferedReader
import kotlin.io.path.readText
import kotlin.system.exitProcess

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

    println(ast)
}