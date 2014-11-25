package com.ndpar.reg

class Stack {

    private java.util.Stack s = new java.util.Stack()

    private int numberPushes = 0, maxDepth = 0

    void push(v) {
        s.push(v)
        numberPushes++
        if (maxDepth < s.size()) maxDepth = s.size()
    }

    def pop() {
        s.pop()
    }

    def initialize() {
        s.clear()
        numberPushes = maxDepth = 0
        'done'
    }

    Map stackStats() {
        ['total-pushes': numberPushes, 'maximum-depth': maxDepth]
    }
}
