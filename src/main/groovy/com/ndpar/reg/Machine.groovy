package com.ndpar.reg

import com.ndpar.util.PersistedList

import static com.ndpar.util.PersistedList.*

class Machine {

    private reader = new Reader()

    Register pc
    Register flag
    Stack stack
    PersistedList instructions
    Map<String, Closure> operations
    Map<String, Register> registers
    Map<String, PersistedList> labels

    long instructionsExecuted
    boolean trace

    // --------------------------------------------------------------
    // Constructors
    // --------------------------------------------------------------

    Machine(List regNames, Map ops) {
        makeNewMachine()
        regNames.each { allocateRegister(it) }
        installOperations(ops)
    }

    Machine(List regNames, Map ops, String controllerText) {
        this(regNames, ops)
        assemble(reader.read(controllerText) as List)
    }

    Machine(List regNames, Map ops, List controllerText) {
        this(regNames, ops)
        assemble(controllerText)
    }

    void makeNewMachine() {
        pc = new Register('pc')
        flag = new Register('flag')
        stack = new Stack()
        operations = ['initialize-stack': initializeStack, 'print-stack-statistics': printStackStatistics]
        registers = [pc: pc, flag: flag]
        labels = [:]
        instructionsExecuted = 0
        trace = false
    }

    // --------------------------------------------------------------
    // Published API
    // --------------------------------------------------------------

    def setRegisterContent(String regName, value) {
        registers[regName].contents = value
    }

    def getRegisterContent(String regName) {
        registers[regName].contents
    }

    def start() {
        pc.contents = instructions
        execute()
    }

    // --------------------------------------------------------------
    // Registers and Operations
    // --------------------------------------------------------------

    def allocateRegister(regName) {
        if (registers[regName]) {
            throw new IllegalArgumentException("Multiply define register $regName")
        } else {
            registers[regName] = new Register(regName)
        }
        'register-allocated'
    }

    def execute() {
        def insts = pc.contents
        if (insts == null) printStackStatistics.call()
        else {
            Instruction inst = insts.car
            if (trace) println("INST: ${inst.label} -> ${inst.text}")
            inst.procedure.call()
            instructionsExecuted++
            execute()
        }
    }

    def installOperations(ops) {
        operations += ops
    }

    def initializeStack = {
        stack.initialize()
        instructionsExecuted = 0
    }

    def printStackStatistics = {
        Map stats = stack.stackStats() + ['instruction-executed': instructionsExecuted]
        println "STATS: $stats"
    }

    // --------------------------------------------------------------
    // Assembler
    // --------------------------------------------------------------

    void assemble(List controllerText) {
        extractLabels(controllerText)
        installProcedures()
    }

    void extractLabels(List controllerText) {
        controllerText.reverse().each { inst ->
            if (inst instanceof String) {
                if (labels[inst]) throw new IllegalArgumentException("Duplicate label: $inst")
                labels[inst] = instructions
                setLabel(instructions, inst)
            } else if (inst instanceof List) {
                instructions = cons(new Instruction(text: inst), instructions)
            } else {
                throw new IllegalArgumentException("Unknown instruction type: $inst")
            }
        }
    }

    void setLabel(PersistedList insts, String label) {
        while (insts != null && insts.car.label == null) {
            insts.car.label = label
            insts = insts.cdr
        }
    }

    void installProcedures() {
        instructions.each { inst ->
            inst.procedure = makeExecutionProcedure(inst.text)
        }
    }

    Closure makeExecutionProcedure(List inst) {
        "make_${inst[0]}"(inst)
    }

    // --------------------------------------------------------------
    // Dynamically dispatched assembler clauses
    // --------------------------------------------------------------

    Closure make_assign(List inst) {
        Register target = registers[inst[1]]
        List valueExp = inst[2..-1]
        Closure valueProc = isOperationExp(valueExp) ? makeOperationExp(valueExp) : makePrimitiveExp(valueExp[0])
        return { ->
            target.contents = valueProc.call()
            advancePc()
        }
    }

    void advancePc() {
        pc.contents = pc.contents.cdr
    }

    Closure make_test(List inst) {
        List condition = inst[1..-1]
        if (isOperationExp(condition)) {
            Closure conditionProc = makeOperationExp(condition)
            return { ->
                flag.contents = conditionProc.call()
                advancePc()
            }
        } else {
            throw new IllegalArgumentException("Bad TEST instruction: $inst")
        }
    }

    Closure make_branch(List inst) {
        List dest = inst[1]
        if (isLabelExp(dest)) {
            PersistedList insts = labels[dest[1]]
            return { ->
                if (flag.contents) pc.contents = insts
                else advancePc()
            }
        } else {
            throw new IllegalArgumentException("Bad BRANCH instruction: $inst")
        }
    }

    Closure make_goto(List inst) {
        List dest = inst[1]
        if (isLabelExp(dest)) {
            PersistedList insts = labels[dest[1]]
            return { -> pc.contents = insts }
        } else if (isRegisterExp(dest)) {
            Register reg = registers[dest[1]]
            return { -> pc.contents = reg.contents }
        } else {
            throw new IllegalArgumentException("Bad GOTO instruction: $inst")
        }
    }

    Closure make_save(List inst) {
        Register reg = registers[inst[1]]
        return { ->
            stack.push(reg.contents)
            advancePc()
        }
    }

    Closure make_restore(List inst) {
        Register reg = registers[inst[1]]
        return { ->
            reg.contents = stack.pop()
            advancePc()
        }
    }

    Closure make_perform(List inst) {
        List action = inst[1..-1]
        if (isOperationExp(action)) {
            Closure actionProc = makeOperationExp(action)
            return { ->
                actionProc.call()
                advancePc()
            }
        } else {
            throw new IllegalArgumentException("Bad PERFORM instruction: $inst")
        }
    }

    Closure makePrimitiveExp(List exp) {
        if (isConstantExp(exp)) {
            def c = exp[1]
            return { -> c }
        } else if (isLabelExp(exp)) {
            PersistedList insts = labels[exp[1]]
            return { -> insts }
        } else if (isRegisterExp(exp)) {
            Register r = registers[exp[1]]
            return { -> r.contents }
        } else {
            throw new IllegalArgumentException("Unknown expression type: $exp")
        }
    }

    boolean isRegisterExp(List exp) {
        exp[0] == 'reg'
    }

    boolean isConstantExp(List exp) {
        exp[0] == 'const'
    }

    boolean isLabelExp(List exp) {
        exp[0] == 'label'
    }

    boolean isOperationExp(List exp) {
        exp[0] instanceof List && exp[0][0] == 'op'
    }

    Closure makeOperationExp(List exp) {
        Closure op = operations[exp[0][1]]
        if (op == null) throw new IllegalArgumentException("Undefined operation: $exp")
        List aprocs = exp.size() > 1 ? exp[1..-1].collect { makePrimitiveExp(it) } : []
        return { -> op.call(aprocs*.call()) }
    }
}
