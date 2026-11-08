# Scala Laboratory Work 3

**Andrew Galashin**

Six tasks on Scala best practices and idioms, verified using Scala 2.13.16 and Java 21.

- [Submission PDF](Scala-Lab3-Submission.pdf): English report, complete code, actual output and compiler evidence.
- [Scala source](Lab3.scala): all six solutions and 16 passing checks.
- [Execution output](Output.txt).
- [Compiler evidence](evidence/): inferred return type and intentional compilation failures.
- [Compiler examples](compiler-examples/): isolated code for those diagnostics.

## Run

Install Scala 2.13 and a compatible Java runtime, then execute:

```sh
scalac -deprecation -feature -Werror Lab3.scala
scala Lab3
```

The application uses only the standard Scala library. Spark is not required.

Compile only `Lab3.scala` for the working application. `compiler-examples/Broken*.scala` intentionally fails compilation and is provided as evidence for the assignment. `TypeProbe.scala` can be checked with `scalac -Xprint:typer -Ystop-after:typer compiler-examples/TypeProbe.scala`.

## Tasks

1. Immutable, tail-recursive Fibonacci preserving `Seq[Int]`, with input and overflow checks.
2. Equivalent collection/Option expressions using `flatMap` and `map`.
3. Compiler-confirmed inferred type `Any` for the original mixed-branch division function.
4. Explanation of the repeated function, failing `flatten` and the corrected `Option[Int]` implementation.
5. Sign classification using pattern matching with a guard.
6. Nonblocking Future composition with success, pending-input and failure tests. `Await` is restricted to the terminal/test boundary.

Compiler binaries, class files and rendering previews are excluded.
