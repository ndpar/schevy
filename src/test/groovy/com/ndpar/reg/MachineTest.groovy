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
}
