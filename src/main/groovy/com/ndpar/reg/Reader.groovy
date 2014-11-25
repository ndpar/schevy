package com.ndpar.reg

class Reader {

    static {
        meta()
    }

    static meta() {
        String.metaClass.isBoolean << {
            delegate.toLowerCase() in ['true', 'false']
        }
    }

    static final BLANK = ' \t\n'

    def read(String s) {
        readFrom(tokenize(s).reverse())
    }

    def tokenize(s) {
        s = s.replace('(', ' ( ').replace(')', ' ) ')
        new StringTokenizer(s, BLANK, false).collect { it }
    }

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

    def atom(String token) {
        if (token.isInteger()) token.toBigInteger()
        else if (token.isDouble()) token.toBigDecimal()
        else if (token.isBoolean()) token.toBoolean()
        else token.intern()
    }
}
