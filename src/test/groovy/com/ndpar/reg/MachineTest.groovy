package com.ndpar.reg

import org.junit.Test

class MachineTest {

    @Test
    void testGCD() {
        def regs = ['a', 'b', 't']
        def ops = [
                'rem': { args -> args[0].mod(args[1]) },
                '='  : { args -> args[0] == args[1] }
        ]
        def script = [
                'test-b',
                ['test', ['op', '='], ['reg', 'b'], ['const', 0]],
                ['branch', ['label', 'gcd-done']],
                ['assign', 't', ['op', 'rem'], ['reg', 'a'], ['reg', 'b']],
                ['assign', 'a', ['reg', 'b']],
                ['assign', 'b', ['reg', 't']],
                ['goto', ['label', 'test-b']],
                'gcd-done'
        ]
        new Machine(regs, ops, script).with {
            setRegisterContent 'a', 206
            setRegisterContent 'b', 40
            start()
            assert getRegisterContent('a') == 2
        }
    }

    @Test
    void testFactorial() {
        def regs = ['continue', 'n', 'val']
        def ops = [
                '-': { args -> args[0] - args[1] },
                '*': { args -> args[0] * args[1] },
                '=': { args -> args[0] == args[1] }
        ]
        def script = [
                ['assign', 'continue', ['label', 'fact-done']],
                'fact-loop',
                ['test', ['op', '='], ['reg', 'n'], ['const', 1]],
                ['branch', ['label', 'base-case']],
                ['save', 'continue'],
                ['save', 'n'],
                ['assign', 'n', ['op', '-'], ['reg', 'n'], ['const', 1]],
                ['assign', 'continue', ['label', 'after-fact']],
                ['goto', ['label', 'fact-loop']],
                'after-fact',
                ['restore', 'n'],
                ['restore', 'continue'],
                ['assign', 'val', ['op', '*'], ['reg', 'n'], ['reg', 'val']],
                ['goto', ['reg', 'continue']],
                'base-case',
                ['assign', 'val', ['const', 1]],
                ['goto', ['reg', 'continue']],
                'fact-done'
        ]
        new Machine(regs, ops, script).with {
            setRegisterContent 'n', 6
            start()
            assert getRegisterContent('val') == 720
        }
    }

    @Test
    void testFibonacci() {
        def regs = ['continue', 'n', 'val']
        def ops = [
                '-': { args -> args[0] - args[1] },
                '+': { args -> args[0] + args[1] },
                '<': { args -> args[0] < args[1] }
        ]
        def script = [
                ['assign', 'continue', ['label', 'fib-done']],
                'fib-loop',
                ['test', ['op', '<'], ['reg', 'n'], ['const', 2]],
                ['branch', ['label', 'immediate-answer']],
                ['save', 'continue'],
                ['assign', 'continue', ['label', 'afterfib-n-1']],
                ['save', 'n'],
                ['assign', 'n', ['op', '-'], ['reg', 'n'], ['const', 1]],
                ['goto', ['label', 'fib-loop']],
                'afterfib-n-1',
                ['restore', 'n'],
                ['assign', 'n', ['op', '-'], ['reg', 'n'], ['const', 2]],
                ['assign', 'continue', ['label', 'afterfib-n-2']],
                ['save', 'val'],
                ['goto', ['label', 'fib-loop']],
                'afterfib-n-2',
                ['assign', 'n', ['reg', 'val']],
                ['restore', 'val'],
                ['restore', 'continue'],
                ['assign', 'val', ['op', '+'], ['reg', 'val'], ['reg', 'n']],
                ['goto', ['reg', 'continue']],
                'immediate-answer',
                ['assign', 'val', ['reg', 'n']],
                ['goto', ['reg', 'continue']],
                'fib-done'
        ]
        new Machine(regs, ops, script).with {
            setRegisterContent 'n', 6
            start()
            assert getRegisterContent('val') == 8
        }
    }
}
