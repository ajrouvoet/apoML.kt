# Hands-on: Parsing with Parser Combinators

Before we do anything else, let us define the syntax of ApoML in Kotlin.
In the previous lesson we mapped a grammar of lists to their inductive definition in Kotlin.

!!! question
    Now do the same for the grammar of ApoML.
    Git checkout the `course/day-1` branch and see `apoml/syntax.kt`.

!!! hint
    After defining the syntax, you may open `pretty.kt` and uncomment the pretty printer.
    You may need to align it with your naming of the expression fields.
    It may be convenient for debugging the parser.

## Parsing using Parse Combinators

Our task is now to define a function that takes a string that may or may not be a valid apoML
program, like:

```ocaml
let eu_interest  = ?(0,100) (* ask for input n s.t. 0 <= n <= 100 *) in
let loan         = ? in
let years        = ? in

loan * (100 + eu_interest) ^ years
```

and produce either an error or a `ApoExp` value.

!!! question
    Take a moment to consider how you would normally approach this problem.
    How would you partition the problem?
    
    Keep the other files closed for now if you want to avoid spoiling the solution.

There are many approaches to parsing. A nice functional approach is to use parser combinators.
This nicely demonstrates some FP principles. Although parsing seems something stateful,
we will define it such that its stateful action is not a __side__-effect. This is a matter
of choosing the right type for our parsers.

!!! note
    Synchronize with the trainees to together design our functional architecture.

We will now define many primitive parsers as instances of this type. This demonstrates
functional architecture: we rely on (function) composition and layering of abstractions.

!!! question
    You can now fill in the remainder of `parsec/parsec.kt`.
    You may use and extend `parsec/ParsecTest.kt` on the way.

    Think carefully about your definitions.
    The lowest level API is the stream API. We want to use it in as few parsers as possible,
    and move on to a higher level API whenever we can.

    For the rest: reduce your brain to the size of a peanut and follow the types.

!!! question
    Once you have defined the parser combinator framework,
    you can have a stab at the ApoML parser in `apoml/parser.kt`.
    You may use and extend `apoml/ApoExpParseTest` on the way.

    There are some gotchas, so feel free to ask for help.

    [Parsing ApoML with Combinators](./parsing-apoml.md){.md-button}
