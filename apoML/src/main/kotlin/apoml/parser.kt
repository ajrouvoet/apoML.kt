package apoml

import parsec.*

val ws: Parsec<Char, Unit> = TODO()
val exp: Parsec<Char, ApoExp> = TODO()

/**
 * Top-level parser for an entire ApoML program:
 * an apoML expression, followed by trailing optional white-space.
 */
val apoML: Parsec<Char, ApoExp> = exp andSkip (ws and eos()) // match eos to ensure we consume the entire output

// With the above approach, you'll have to be explicit in your parser
// about where you expect whitespace to occur.
//
// You could also consider first tokenizing the character stream to produce a stream of strings.
// When you do that, you can drop the whitespace, because it is no longer significant.
// This then simplifies the parser considerably, which will be of type: Parsec<String, ApoExp>
//
// The only risk is that I haven't tried this myself yet.