# Scheme and Groovy

This repository contains several implementations of Scheme in Groovy. To run them you need

- Java 7+
- Maven

installed on your computer.

## Schevy

This is a Groovy port of Peter Norvig's [lispy](http://norvig.com/lispy.html).

To run it, use the following command

    $ mvn exec:java -Dexec.mainClass="com.ndpar.schevy.Scheme"

When it's up, you should see the REPL prompt where you can input Scheme expressions

    schevy> (define factorial (lambda (n) (if (= n 1) 1 (* (factorial (- n 1)) n))))
    ok
    schevy> (factorial 10)
    3628800

## Explicit Control Evaluator for Scheme

This is basically a Groovy solution for SICP [exercise 5.51](http://mitpress.mit.edu/sicp/full-text/book/book-Z-H-35.html#%_thm_5.51). It consists of a Register Machine with Assembler and assembly code for Scheme interpreter.

To run it, use the following command

    $ mvn exec:java -Dexec.mainClass="com.ndpar.eceval.Scheme"

It starts the REPL that evaluates Scheme expressions and prints Register Machine stats

    EC-Eval> (define factorial (lambda (n) (if (= n 1) 1 (* (factorial (- n 1)) n))))
    EC-Eval: ok
    STATS: [total-pushes:3, maximum-depth:3, instruction-executed:51]

    EC-Eval> (factorial 10)
    EC-Eval: 3628800
    STATS: [total-pushes:228, maximum-depth:53, instruction-executed:2892]

