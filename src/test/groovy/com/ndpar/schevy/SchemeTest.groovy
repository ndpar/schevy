package com.ndpar.schevy

import org.junit.Before
import org.junit.Test

class SchemeTest {

    def SOURCE = "(define (f x) (+ x 5))"

    Scheme scheme

    @Before
    void setup() {
        scheme = new Scheme()
    }

    @Test
    void tokenize_test() {
        def result = scheme.tokenize(SOURCE)
        assert result == ['(', 'define', '(', 'f', 'x', ')', '(', '+', 'x', '5', ')', ')']
    }

    @Test
    void read_test() {
        def result = scheme.read(SOURCE)
        assert result == ['define', ['f', 'x'], ['+', 'x', 5]]
    }

    @Test
    void write_test() {
        scheme.with {
            assert SOURCE == write(read(SOURCE))
        }
    }

    @Test
    void lookupVariableValue_test() {
        assert scheme.lookupVariableValue('x', [[x: 1], [x: 2]]) == 1
    }

    @Test(expected = IllegalArgumentException)
    void lookupVariableValue_test_unbound() {
        scheme.lookupVariableValue('y', [[x: 1], [x: 2]])
    }

    def eval(exp) {
        scheme.with { eval(read(exp)) }
    }

    def eval(exp, env) {
        scheme.with { eval(read(exp), env) }
    }

    @Test
    void eval_number() {
        assert eval('5') == 5
        assert eval('3.14') == 3.14
    }

    @Test
    void eval_var() {
        assert eval('x', [['x': 42]]) == 42
    }

    @Test
    void eval_quoted() {
        assert eval('(quote)') == []
        assert eval('(quote two)') == 'two'
        assert eval('(quote (1 two 3))') == [1, 'two', 3]
    }

    @Test
    void eval_assignment() {
        def env = [['x': 1], ['y': 2]]
        assert eval('(set! y 3)', env) == 'ok'
        assert env == [['x': 1], ['y': 3]]
    }

    @Test(expected = IllegalArgumentException)
    void eval_assignment_unbound() {
        scheme.lookupVariableValue('a', [[x: 1], [x: 2]])
    }

    @Test
    void eval_definition() {
        def env = [['x': 1], ['y': 2]]
        assert eval('(define y 3)', env) == 'ok'
        assert env == [['x': 1, 'y': 3], ['y': 2]]
    }

    @Test
    void eval_if() {
        assert eval('(if true 42)') == 42
        assert eval('(if true 42 5)') == 42
        assert eval('(if false 42 5)') == 5
    }

    @Test
    void apply_arithmetic() {
        assert eval('(+ 1 2 3)') == 6
        assert eval('(- 3 2)') == 1
        assert eval('(* 2 3 4)') == 24
        assert eval('(/ 4 2)') == 2
        assert eval('(= 2 2)') == true
        assert eval('(= 2 3)') == false
        assert eval('(< 2 3)') == true
    }

    @Test
    void appl_list() {
        assert eval('(list 1 2 3)') == [1, 2, 3]
        assert eval('(null? (quote))') == true
        assert eval('(null? (cons 1 (quote)))') == false
        assert eval('(cons 1 (cons 2 (quote)))') == [1, 2]
        assert eval('(car (cons 1 (cons 2 (quote))))') == 1
        assert eval('(cdr (cons 1 (cons 2 (quote))))') == [2]
    }

    @Test
    void factorial_test() {
        eval('(define factorial (lambda (n) (if (= n 1) 1 (* (factorial (- n 1)) n))))')
        assert eval('(factorial 5)') == 120
    }

    @Test
    void define_procedure() {
        assert eval('(define (fact n) (if (= n 1) 1 (* (fact (- n 1)) n)))') == 'ok'
        assert eval('(fact 5)') == 120
    }

    @Test
    void norvig_test() {
        assert eval('(define first car)') == 'ok'
        assert eval('(define rest cdr)') == 'ok'
        assert eval('(define count (λ (item L) (if (null? L) 0 (+ (if (equal? item (first L)) 1 0) (count item (rest L))))))') == 'ok'
        assert eval('(count 0 (list 0 1 2 3 0 0))') == 3
        assert eval('(count (quote the) (quote (the more the merrier the bigger the better)))') == 4
    }
}
