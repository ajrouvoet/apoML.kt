# Functional Programming

Many introductions to functional programming approach it as a style,
enumerating its symptoms. I like a different approach. If we start
with an emphasis on the value _functional correctness_, then the
characteristics of functional programming surface naturally.

Programming is about balancing different values.
Functional correctness is clearly one, but performance is another,
and availability of required capabilities and other resources is---although
easily overlooked---also an important value.

Imperative programming is a programming paradigm that emphasizes performances:
the paradigm evolved from the instruction-centric von Neumann model of computer hardware. We are
performing a stepwise computation on mutable memory. Over time, we could
abstract over more and more details of---for example---the allocation and
deallocation of memory, but the sequential nature stayed. The equality
symbol `=` is reserved for assignment.

Functional programming is a programming paradigm that emphasizes functional
correctness. It evolved from a mathematical model of computable functions.
The model stems from the 1930s (Alonzo Church), whereas the first high-level
implementation stems from the 1950s (Lisp, John McCarthy). In 1977, John
Backus presented a functional programming language FP during his Turing Award lecture
with the title "Can Programming be Liberated from the von Neumann Style".
In functional programs, the equality symbol represents actual, mathematical equality.
This is important! It means that you can reason about programs without the constant
fear of temporal change looming over you.

The FP _style_ is a consequence of the values that are underlying this mathematical model.
The absence of mutable data, for example, ensures that equality is really equality:

```kotlin
val xs: List<Int> = (1 until 10).toList()
val ys: List<Int> = xs.plus(11)

// The equation xs = (1 until 10).toList() is still true!
// In the remainder of the program we can always substitute a reference `xs`
// with the right-hand side of that equation, and vice versa, without 
// compromising the functional correctness of the program.
```
The absence of side effects also serves this purpose.

```kotlin
import java.nio.file.Path
import kotlin.io.path.readText

fun readFile(path: Path): String = path.readText()

val contents = readFile(Path.of("/tmp/myeffect"))
```

The above equation for `contents` is not an equation in the same sense as the xs equation in the
previous program: it holds only temporarily.

An effect is something for which is explicitly matters whether we do it 
once or twice. Hence, functional programmers feel that we have to take 
special care when we handle computations that have effects. In a strict
FP language, like Haskell, this means that we cannot write equations
where the lhs is a value and the rhs is an effectful program that computes it.

```haskell
-- std lib function
readFile :: FilePath -> IO String
    
contents :: String
contents = readFile "/tmp/myeffect"
```

This won't compile, because withEffects returns an `IO Bool`
which is not a value of type Bool, but an effectful program that computes a bool.
And those should not be confused.

This principal that equality is a lovely thing and should mean that things are
mathematically equal for now and forever is the value that is at the heart of FP.
If equality not temporary, then we can freely reason along the equations that we read from our screen.
This makes it decidably easier to relate specifications with their implementation and obtain functional
correctness. 

All the other things are just the consequences of the desire to maintain this value.

## About the word 'Functional'

Mathematicians spent their day thinking about relations between things.
One could think about the relation between Java's `Math.sqrt(x)` function
and the mathematical `$sqrt x$, for example. Unlike 'our' functions,
mathematical functions are oftentimes not defined computationally.
For example, $\sqrt x$ might be defined as the number such that $(\sqrt x)^2 = x$.
This does not tell you at all how you should go about deducing that number,
but only something about how to verify that you deduced it correctly.
It is defined as a _relation_ between the 'input' $x$ and the 'output' $\sqrt x$.

Relations are more general than functions. We say that a binary relation $R(i, o)$
is *functional* when the simultaneous $R(i, o_1)$ and $(i, o_2)$ imply that
$o_1 = o_2$. When we think of $i$ as the input and $o$ as the output, then we would
say that the output is uniquely determined by the input. In that sense, the mathematical
$\sqrt \_$ on integers does _not_ specify a _functional relation_, 
because we have both $\sqrt(4) = 2$ and $\sqrt(4) = -2$.

We now return to our effectful Kotlin program:

```kotlin
fun readFile(path: Path): String = path.readText()
```

Does this program describe a functional relation between the input and the output?
No, of course not: today `readFile("/tmp/myeffect")` may equal to `"Hello, side effect"`
and tomorrow it may be equal to `"DEADBEEF"`. 
For this reason, some people---somewhat pedantically---call this a 'procedure' rather than a 'function'.

We should be careful, however, to put a thick red line through the above program and
write it off as "IMPERATIVE! 0/10". Whether this program is functional or not has
everything to do with effects, but also everything with the types at play, because
the fact that `readFile(path)` today is not equal to `readFile(path)` tomorrow has everything
to do with our definition of equality. Strings have a definition of equality that is very strict,
but a different type may have a more complex notion of equality. Perhaps even complex enough
to accounts for the temporal difference... The same program in Haskell could be called functional
because it is typed differently:

```haskell
readFile :: FilePath -> IO String
```

The type `IO String` is such that the equality between the computations 
`readFile "/tmp/myeffect"` today and tomorrow does not promise us anything
about the value of strings read being equal! At first this may seem silly,
but take into account that the return type has a major influence on how
we interact with the effectful computation. The API on the type can enforce
an interaction that does justice to the distinction between a constant
string and a string to be read from a file.

## The Benefits

I will claim that it is only natural that a paradigm constructed around the core
value of functional correctness performs better at delivering functionally correct
programs than a paradigm constructed around the model of the hardware. In other words:
I strongly believe that FP will lead to programs with fewer functional bugs.

Conversely, a paradigm constructed around the hardware will naturally make it easier
to think about the execution of the program. This makes it easier to reason about
the performance of imperative programs than to reason about the performance of
FP. It does not mean that you cannot write performant code with FP; just that it may
be less plain to see what the performance of a functional program will be.

For the software that I work on, this makes for an easy choice. Functional correctness
tends to be a much greater challenge for most software than performance, because it
is inherently something that one cannot solve locally. Performance, on the other hand,
is often determined by a bottleneck and can thus be solved locally.

I will not try to prove my claim about the benefits of FP with more talk.
Instead, I want to challenge you to test my claim today with a programming project.
We will work on the challenging task of implementing a new programming language: ApoML.
Although you may find it a significant intellectual challenge to grok the unfamiliar paradigm,
my hope is that you will find that when you get your project to compile, it is more likely to
be functionally correct than what you are used to with imperative programs. If that is indeed
the case, I ask the court to accept this as evidence towards my claim.