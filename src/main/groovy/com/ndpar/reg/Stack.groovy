package com.ndpar.reg

class Stack {

    private java.util.Stack s = new java.util.Stack()

    def push(v) {
        s.push(v)
    }

    def pop() {
        s.pop()
    }

    def initialize() {
        s.clear()
        'done'
    }
}
