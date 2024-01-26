# ApoML

An industrial-strength calculator language
combining the flexibility of greek yoghurt
with the computing power of Aristotle himself
in the tradition of ML languages.

```ocaml
(* ask for the initial values *)
val eu_interest  = ?(0,100) (* ask for input n s.t. 0 <= n <= 100 *)
val loan         = ?
val years        = ?

(* compute what we need to pay back to the EU *)
loan * (100 + eu_interest) ^ years
```

## Getting Started 

Assuming you have ApoML installed, copy-paste the above script
in a file `./economy.apo`. Now run:

```console
$ apo ./economy.apo
> enter a number between 0-100
< 10
> enter a number
< 400
> enter a number
< 5
-------------------------------
400 * (100 + 10) ^ 5 = x
```

## Developing ApoML

See the [developer docs](./dev.md) for instructions on how
to get started as a developer.
