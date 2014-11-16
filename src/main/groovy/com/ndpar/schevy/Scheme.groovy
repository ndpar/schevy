package com.ndpar.schevy

/**
 *
 */
class Scheme {

    static {
        meta()
    }

    static meta() {
        String.metaClass.isBoolean << {
            delegate.toLowerCase() in ['true', 'false']
        }
        List.metaClass.car << {
            delegate[0]
        }
        List.metaClass.cdr << {
            delegate.size() > 1 ? delegate[1..-1] : []
        }
    }

    // --------------------------------------------------------------
    // Parser
    // --------------------------------------------------------------

    static final BLANK = ' \t'

    /**
     * Read a Scheme expression from a string.
     */
    def read(s) {
        readFrom(tokenize(s).reverse())
    }

    /**
     * Convert a string into a list of tokens.
     */
    def tokenize(s) {
        s = s.replace('(', ' ( ').replace(')', ' ) ')
        new StringTokenizer(s, BLANK, false).collect { it }
    }

    /**
     * Read an expression from a sequence of tokens.
     */
    def readFrom(tokens) {
        def t = tokens.pop()
        switch (t) {
            case '(':
                def ls = []
                while (tokens[-1] != ')') ls << readFrom(tokens)
                tokens.pop() // pop off ')'
                return ls
            case ')':
                throw new IllegalArgumentException('Unexpected )')
            default:
                atom(t)
        }
    }

    /**
     * Numbers become numbers; every other token is a string.
     */
    def atom(String token) {
        if (token.isInteger()) token.toBigInteger()
        else if (token.isDouble()) token.toBigDecimal()
        else if (token.isBoolean()) token.toBoolean()
        else token.intern()
    }

    // --------------------------------------------------------------
    // REPL
    // --------------------------------------------------------------

    /**
     * Convert a Groovy object back into a Lisp-readable string.
     */
    def write(exp) {
        if (exp instanceof List) {
            if (isPrimitiveProc(exp)) '#<primitive>'
            else if (isCompountProc(exp)) '#<compound>'
            else '(' + exp.collect { write(it) }.join(' ') + ')'
        } else exp.toString()
    }

    /**
     * A read-eval-print loop.
     */
    def repl(prompt = 'schevy> ') {
        System.in.withReader { r ->
            while (true) {
                print(prompt)
                def val = eval(read(r.readLine()))
                if (val) println(write(val))
            }
        }
    }

    static void main(String[] args) {
        new Scheme().repl()
    }

    // --------------------------------------------------------------
    // Evaluator
    // --------------------------------------------------------------

    /**
     * Evaluate an expression in an environment.
     */
    def eval(exp, env = globalEnv) {
        if (isSelfEval(exp)) exp
        else if (isVariable(exp)) lookupVariableValue(exp, env)
        else if (isQuoted(exp)) evalQuotation(exp)
        else if (isAssignment(exp)) evalAssignment(exp, env)
        else if (isDefinition(exp)) evalDefinition(exp, env)
        else if (isIf(exp)) evalIf(exp, env)
        else if (isLambda(exp)) makeProcedure(exp[1], exp[2..-1], env)
        else if (isBegin(exp)) evalSequence(exp.cdr(), env)
        else if (isApplication(exp)) apply(exp[0], exp.cdr(), env)
        else throw new IllegalArgumentException("Unknown expression type $exp")
    }

    def isSelfEval(exp) {
        exp instanceof Number || exp instanceof Boolean
    }

    def isVariable(exp) {
        exp instanceof String
    }

    def isQuoted(exp) {
        exp[0] == 'quote'
    }

    def evalQuotation(exp) {
        if (exp.size() == 1) []
        else if (exp.size == 2) exp[1]
        else throw new IllegalArgumentException("Invalid quotation $exp")
    }

    def isAssignment(exp) {
        exp[0] == 'set!'
    }

    def evalAssignment(exp, env) {
        setVariableValue(exp[1], eval(exp[2], env), env)
    }

    def isDefinition(exp) {
        exp[0] == 'define'
    }

    def evalDefinition(exp, env) {
        defineVariable(exp[1], exp[2], env)
    }

    def isIf(exp) {
        exp[0] == 'if'
    }

    def evalIf(exp, env) {
        if (eval(exp[1], env)) {
            eval(exp[2], env)
        } else {
            exp.size() == 4 ? eval(exp[3], env) : false
        }
    }

    def isLambda(exp) {
        exp[0] in ['lambda', 'λ']
    }

    def makeProcedure(parameters, body, env) {
        ['procedure', parameters, body, env]
    }

    def isBegin(exp) {
        exp[0] == 'begin'
    }

    def evalSequence(exps, env) {
        exps.collect { eval(it, env) }[-1]
    }

    def isApplication(exp) {
        exp instanceof List
    }

    def apply(proc, args, env) {
        def p = eval(proc, env)
        def params = args.collect { eval(it, env) }
        if (isPrimitiveProc(p)) {
            applyPrimitiveProc(p, params)
        } else if (isCompountProc(p)) {
            evalSequence(p[2], extendEnv(p[1], params, env))
        } else throw new IllegalArgumentException("Unknown procedure type $proc")
    }

    def isPrimitiveProc(proc) {
        proc instanceof Closure
    }

    def applyPrimitiveProc(proc, args) {
        proc.call(args)
    }

    def isCompountProc(proc) {
        proc[0] == 'procedure'
    }

    // --------------------------------------------------------------
    // Environment
    // --------------------------------------------------------------

    def globalEnv = [addGlobals()]

    def addGlobals() {
        [
                'list'  : { args -> args },
                'null?' : { args -> args[0].empty },
                'cons'  : { args -> [args[0]] + args[1] },
                'car'   : { args -> args[0].car() },
                'cdr'   : { args -> args[0].cdr() },
                'equal?': { args -> args[0] == args[1] },
                '+'     : { args -> args.inject(0) { a, i -> a + i } },
                '-'     : { args -> args[0] - args[1] },
                '*'     : { args -> args.inject(1) { a, i -> a * i } },
                '/'     : { args -> args[0] / args[1] },
                '='     : { args -> args[0] == args[1] },
                '<'     : { args -> args[0] < args[1] }
        ]
    }

    def defineVariable(var, val, List<Map> env) {
        if (var instanceof String) env[0][var] = eval(val, env)
        else {
            assert var instanceof List
            env[0][var[0]] = makeProcedure(var.cdr(), [val], env)
        }
        'ok'
    }

    def setVariableValue(String var, val, List<Map> env) {
        for (Map frame : env) {
            if (frame.containsKey(var)) {
                frame[var] = val
                return 'ok'
            }
        }
        throw new IllegalArgumentException("Unbound variable $var")
    }

    def lookupVariableValue(String var, List<Map> env) {
        if (env.empty) {
            throw new IllegalArgumentException("Unbound variable $var")
        } else if (env[0].containsKey(var)) {
            env[0][var]
        } else {
            lookupVariableValue(var, env.cdr())
        }
    }

    def extendEnv(vars, vals, env) {
        assert vars.size() == vals.size()
        def frame = [vars, vals].transpose().collectEntries { [(it[0]): it[1]] }
        [frame] + env
    }
}
