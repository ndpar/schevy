package com.ndpar.eceval

import com.ndpar.reg.Machine
import com.ndpar.util.Reader
import com.ndpar.util.SchemeUtils as SU

/**
 * Explicit Control Evaluator for Scheme.
 * SICP, exercise 5.51, p.610
 */
class Scheme {

    private reader = new Reader()

    void start() {
        def regs = ['argl', 'continue', 'env', 'exp', 'proc', 'unev', 'val']
        def ops = [
                'adjoin-arg'               : adjoinArg,
                'announce-output'          : announceOutput,
                'application?'             : isApplication,
                'apply-primitive-procedure': applyPrimitiveProcedure,
                'assignment?'              : isAssignment,
                'assignment-value'         : assignmentValue,
                'assignment-variable'      : assignmentVariable,
                'begin?'                   : isBegin,
                'begin-actions'            : beginActions,
                'compound-procedure?'      : isCompoundProcedure,
                'cond?'                    : isCond,
                'cond-actions'             : condActions,
                'cond-clauses'             : condClauses,
                'cond-else-clause?'        : isCondElseClause,
                'cond-predicate'           : condPredicate,
                'define-variable!'         : defineVariable,
                'definition?'              : isDefinition,
                'definition-value'         : definitionValue,
                'definition-variable'      : definitionVariable,
                'get-global-environment'   : getGlobalEnvironment,
                'empty-arglist'            : emptyArglist,
                'eq?'                      : isEq,
                'extend-environment'       : extendEnvironment,
                'first-cond'               : firstCond,
                'first-exp'                : firstExp,
                'first-operand'            : firstOperand,
                'if?'                      : isIf,
                'if-alternative'           : ifAlternative,
                'if-consequent'            : ifConsequent,
                'if-predicate'             : ifPredicate,
                'lambda?'                  : isLambda,
                'lambda-body'              : lambdaBody,
                'lambda-parameters'        : lambdaParameters,
                'last-exp?'                : isLastExp,
                'last-operand?'            : isLastOperand,
                'let?'                     : isLet,
                'let->combination'         : letCombination,
                'lookup-variable-value'    : lookupVariableValue,
                'make-procedure'           : makeProcedure,
                'no-conds?'                : isNoConds,
                'no-operands?'             : isNoOperands,
                'operands'                 : operands,
                'operator'                 : operator,
                'primitive-procedure?'     : isPrimitiveProcedure,
                'procedure-body'           : procedureBody,
                'procedure-environment'    : procedureEnvironment,
                'procedure-parameters'     : procedureParameters,
                'prompt-for-input'         : promptForInput,
                'quoted?'                  : isQuoted,
                'read'                     : read,
                'rest-conds'               : restConds,
                'rest-exps'                : restExps,
                'rest-operands'            : restOperands,
                'self-evaluating?'         : isSelfEvaluating,
                'set-variable-value!'      : setVariableValue,
                'true?'                    : isTrue,
                'text-of-quotation'        : textOfQuotation,
                'user-print'               : userPrint,
                'variable?'                : isVariable
        ]
        def script = """(
            read-eval-print-loop
            (perform (op initialize-machine))
            (perform (op prompt-for-input) (const EC-Eval>))
            (assign exp (op read))
            (assign env (op get-global-environment))
            (assign continue (label print-result))
            (goto (label eval-dispatch))

            print-result
            (perform (op announce-output) (const EC-Eval:))
            (perform (op user-print) (reg val))
            (perform (op print-machine-statistics))
            (goto (label read-eval-print-loop))

            eval-dispatch
            (test (op self-evaluating?) (reg exp))
            (branch (label ev-self-eval))
            (test (op variable?) (reg exp))
            (branch (label ev-variable))
            (test (op quoted?) (reg exp))
            (branch (label ev-quoted))
            (test (op assignment?) (reg exp))
            (branch (label ev-assignment))
            (test (op definition?) (reg exp))
            (branch (label ev-definition))
            (test (op if?) (reg exp))
            (branch (label ev-if))
            (test (op cond?) (reg exp))
            (branch (label ev-cond))
            (test (op lambda?) (reg exp))
            (branch (label ev-lambda))
            (test (op let?) (reg exp))
            (branch (label ev-let))
            (test (op begin?) (reg exp))
            (branch (label ev-begin))
            (test (op application?) (reg exp))
            (branch (label ev-application))
            (goto (label unknown-expression-type))

            ev-self-eval
            (assign val (reg exp))
            (goto (reg continue))

            ev-variable
            (assign val (op lookup-variable-value) (reg exp) (reg env))
            (goto (reg continue))

            ev-quoted
            (assign val (op text-of-quotation) (reg exp))
            (goto (reg continue))

            ev-lambda
            (assign unev (op lambda-parameters) (reg exp))
            (assign exp (op lambda-body) (reg exp))
            (assign val (op make-procedure) (reg unev) (reg exp) (reg env))
            (goto (reg continue))

            ev-let
            (assign exp (op let->combination) (reg exp))
            (goto (label eval-dispatch))

            ev-application
            (assign unev (op operands) (reg exp))
            (assign exp (op operator) (reg exp))
            (save continue)
            (assign continue (label ev-appl-did-sym-operator))
            (test (op variable?) (reg exp))
            (branch (label ev-variable))
            (save env)
            (save unev)
            (assign continue (label ev-appl-did-operator))
            (goto (label eval-dispatch))

            ev-appl-did-operator
            (restore unev)
            (restore env)

            ev-appl-did-sym-operator
            (assign argl (op empty-arglist))
            (assign proc (reg val))
            (test (op no-operands?) (reg unev))
            (branch (label apply-dispatch))
            (save proc)

            ev-appl-operand-loop
            (save argl)
            (assign exp (op first-operand) (reg unev))
            (test (op last-operand?) (reg unev))
            (branch (label ev-appl-last-arg))
            (save env)
            (save unev)
            (assign continue (label ev-appl-accumulate-arg))
            (goto (label eval-dispatch))

            ev-appl-accumulate-arg
            (restore unev)
            (restore env)
            (restore argl)
            (assign argl (op adjoin-arg) (reg val) (reg argl))
            (assign unev (op rest-operands) (reg unev))
            (goto (label ev-appl-operand-loop))

            ev-appl-last-arg
            (assign continue (label ev-appl-accum-last-arg))
            (goto (label eval-dispatch))

            ev-appl-accum-last-arg
            (restore argl)
            (assign argl (op adjoin-arg) (reg val) (reg argl))
            (restore proc)
            (goto (label apply-dispatch))

            apply-dispatch
            (test (op primitive-procedure?) (reg proc))
            (branch (label primitive-apply))
            (test (op compound-procedure?) (reg proc))
            (branch (label compound-apply))
            (goto (label unknown-procedure-type))

            primitive-apply
            (assign val (op apply-primitive-procedure) (reg proc) (reg argl))
            (restore continue)
            (goto (reg continue))

            compound-apply
            (assign unev (op procedure-parameters) (reg proc))
            (assign env (op procedure-environment) (reg proc))
            (assign env (op extend-environment) (reg unev) (reg argl) (reg env))
            (assign unev (op procedure-body) (reg proc))
            (goto (label ev-sequence))

            ev-begin
            (assign unev (op begin-actions) (reg exp))
            (save continue)
            (goto (label ev-sequence))

            ev-sequence
            (assign exp (op first-exp) (reg unev))
            (test (op last-exp?) (reg unev))
            (branch (label ev-sequence-last-exp))
            (save unev)
            (save env)
            (assign continue (label ev-sequence-continue))
            (goto (label eval-dispatch))

            ev-sequence-continue
            (restore env)
            (restore unev)
            (assign unev (op rest-exps) (reg unev))
            (goto (label ev-sequence))

            ev-sequence-last-exp
            (restore continue)
            (goto (label eval-dispatch))

            ev-if
            (save exp)
            (save env)
            (save continue)
            (assign continue (label ev-if-decide))
            (assign exp (op if-predicate) (reg exp))
            (goto (label eval-dispatch))

            ev-if-decide
            (restore continue)
            (restore env)
            (restore exp)
            (test (op true?) (reg val))
            (branch (label ev-if-consequent))

            ev-if-alternative
            (assign exp (op if-alternative) (reg exp))
            (goto (label eval-dispatch))

            ev-if-consequent
            (assign exp (op if-consequent) (reg exp))
            (goto (label eval-dispatch))

            ev-cond
            (assign unev (op cond-clauses) (reg exp))
            (save unev)

            ev-cond-next
            (test (op no-conds?) (reg unev))
            (branch (label ev-cond-done))
            (assign exp (op first-cond) (reg unev))
            (test (op cond-else-clause?) (reg exp))
            (branch (label ev-cond-actions))
            (save exp)
            (save continue)
            (assign exp (op cond-predicate) (reg exp))
            (assign continue (label ev-cond-decide))
            (goto (label eval-dispatch))

            ev-cond-decide
            (restore continue)
            (restore exp)
            (test (op true?) (reg val))
            (branch (label ev-cond-actions))
            (restore unev)
            (assign unev (op rest-conds) (reg unev))
            (save unev)
            (goto (label ev-cond-next))

            ev-cond-actions
            (assign unev (op cond-actions) (reg exp))
            (save continue)
            (goto (label ev-sequence))

            ev-cond-done
            (restore unev)
            (goto (reg continue))

            ev-assignment
            (assign unev (op assignment-variable) (reg exp))
            (save unev)
            (assign exp (op assignment-value) (reg exp))
            (save env)
            (save continue)
            (assign continue (label ev-assignment-1))
            (goto (label eval-dispatch))

            ev-assignment-1
            (restore continue)
            (restore env)
            (restore unev)
            (perform
             (op set-variable-value!) (reg unev) (reg val) (reg env))
            (assign val (const ok))
            (goto (reg continue))

            ev-definition
            (assign unev (op definition-variable) (reg exp))
            (save unev)
            (assign exp (op definition-value) (reg exp))
            (save env)
            (save continue)
            (assign continue (label ev-definition-1))
            (goto (label eval-dispatch))

            ev-definition-1
            (restore continue)
            (restore env)
            (restore unev)
            (perform
             (op define-variable!) (reg unev) (reg val) (reg env))
            (assign val (const ok))
            (goto (reg continue))

            unknown-expression-type
            (assign val (const unknown-expression-type-error))
            (goto (label signal-error))

            unknown-procedure-type
            (restore continue)
            (assign val (const unknown-procedure-type-error))
            (goto (label signal-error))

            signal-error
            (perform (op user-print) (reg val))
            (goto (label read-eval-print-loop))
        )"""
        new Machine(regs, ops, script).with {
//            trace = true
            start()
        }
    }

    static void main(String[] args) {
        new Scheme().start()
    }

    // --------------------------------------------------------------
    // Operations
    // --------------------------------------------------------------

    def adjoinArg = {
        args -> args[1] + [args[0]]
    }

    def announceOutput = { args ->
        def output = args[0]
        print "$output "
    }

    def applyPrimitiveProcedure = { args ->
        def proc = args[0][1]
        def argl = args[1]
        proc.call(argl)
    }

    def assignmentValue = {
        args -> args[0][2]
    }

    def assignmentVariable = {
        args -> args[0][1]
    }

    def beginActions = {
        args -> args[0][1..-1]
    }

    def condActions = {
        args -> args[0][1..-1]
    }

    def condClauses = {
        args -> args[0][1..-1]
    }

    def isCondElseClause = {
        args -> args[0][0] == 'else'
    }

    def condPredicate = {
        args -> args[0][0]
    }

    def defineVariable = { args ->
        def var = args[0]
        def val = args[1]
        def env = args[2]
        def frame = env[0]
        frame[var] = val
    }

    def definitionValue = {
        args -> args[0][2]
    }

    def definitionVariable = {
        args -> args[0][1]
    }

    def emptyArglist = {
        []
    }

    def extendEnvironment = { args ->
        SU.extendEnv(args[0], args[1], args[2])
    }

    def firstCond = {
        args -> args[0][0]
    }

    def firstExp = {
        args -> args[0][0]
    }

    def firstOperand = {
        args -> args[0][0]
    }

    def getGlobalEnvironment = {
        globalEnvironment
    }

    def ifAlternative = {
        args -> args[0][3]
    }

    def ifConsequent = {
        args -> args[0][2]
    }

    def ifPredicate = {
        args -> args[0][1]
    }

    def isApplication = {
        args -> args[0] instanceof List
    }

    def isAssignment = {
        args -> args[0][0] == 'set!'
    }

    def isBegin = {
        args -> args[0][0] == 'begin'
    }

    def isCompoundProcedure = {
        args -> args[0][0] == 'procedure'
    }

    def isCond = {
        args -> args[0][0] == 'cond'
    }

    def isDefinition = {
        args -> args[0][0] == 'define'
    }

    def isEq = {
        args -> args[0] == args[1]
    }

    def isIf = {
        args -> args[0][0] == 'if'
    }

    def isLambda = {
        args -> args[0][0] == 'lambda'
    }

    def isLastExp = {
        args -> args[0].size() == 1
    }

    def isLastOperand = {
        args -> args[0].size() == 1
    }

    def isLet = {
        args -> args[0][0] == 'let'
    }

    def isNoConds = {
        args -> args[0].empty
    }

    def isNoOperands = {
        args -> args[0].empty
    }

    def isPrimitiveProcedure = {
        args -> args[0][0] == 'primitive'
    }

    def isQuoted = {
        args -> args[0][0] == 'quote'
    }

    def isSelfEvaluating = { args ->
        def exp = args[0]
        exp instanceof Number || exp instanceof Boolean
    }

    def isTrue = {
        args -> args[0] == true
    }

    def isVariable = {
        args -> args[0] instanceof String
    }

    def lambdaBody = {
        args -> args[0][2..-1]
    }

    def lambdaParameters = {
        args -> args[0][1]
    }

    def letCombination = { args ->
        def bindings = args[0][1]
        def vars = bindings.collect { it[0] }
        def vals = bindings.collect { it[1] }
        def body = args[0][2..-1]
        [makeLambda(vars, body)] + vals
    }

    def makeLambda(params, body) {
        ['lambda', params] + body
    }

    def lookupVariableValue = { args ->
        def var = args[0]
        def env = args[1]
        for (Map frame : env) {
            if (frame.containsKey(var)) return frame[var]
        }
        throw new IllegalArgumentException("Unbound variable: $var")
    }

    def makeProcedure = { args ->
        def params = args[0]
        def body = args[1]
        def env = args[2]
        ['procedure', params, body, env]
    }

    def operands = { args ->
        def exp = args[0]
        exp.size() > 1 ? exp[1..-1] : []
    }

    def operator = {
        args -> args[0][0]
    }

    def procedureBody = {
        args -> args[0][2]
    }

    def procedureEnvironment = {
        args -> args[0][3]
    }

    def procedureParameters = {
        args -> args[0][1]
    }

    def promptForInput = { args ->
        def prompt = args[0]
        print "\n$prompt "
    }

    def read = {
        reader.read(System.in.newReader().readLine())
    }

    def restConds = {
        args -> args[0][1..-1]
    }

    def restExps = {
        args -> args[0][1..-1]
    }

    def restOperands = {
        args -> args[0][1..-1]
    }

    def setVariableValue = { args ->
        def var = args[0]
        def val = args[1]
        def env = args[2]
        for (Map frame : env) {
            if (frame[var]) {
                frame[var] = val
                return
            }
        }
        throw new IllegalArgumentException("Unbound variable: $var")
    }

    def textOfQuotation = {
        args -> args[0][1]
    }

    def userPrint = { args ->
        def object = args[0]
        println(stringify(object))
    }

    def stringify(object) {
        if (object instanceof List) {
            if (object.empty) '()'
            else if (object[0] == 'procedure') '#<procedure>'
            else object.toString().replace('[', '(').replace(']', ')').replace(',', '')
        } else object
    }

    // --------------------------------------------------------------
    // Environment
    // --------------------------------------------------------------

    def globalEnvironment = setupEnvironment()

    def setupEnvironment() {
        [primitiveVars() + primitiveOperations()]
    }

    def primitiveVars() {
        [
                'true'        : true,
                'false'       : false,
                '*unassigned*': '*unassigned*'
        ]
    }

    def primitiveOperations() {
        [
                'assert': proc({
                    args -> assert args[0]; 'ok'
                }),
                'car'   : proc({
                    args -> args[0][0]
                }),
                'cdr'   : proc({
                    args -> args[0] != null && args[0].size() > 1 ? args[0][1..-1] : null
                }),
                'cons'  : proc({
                    args -> [args[0]] + args[1]
                }),
                'null?' : proc({
                    args -> args[0] == null
                }),
                'eq?'   : proc({
                    args -> args[0] == args[1]
                }),
                '='     : proc({
                    args -> args[0] == args[1]
                }),
                '<'     : proc({
                    args -> args[0] < args[1]
                }),
                '+'     : proc({
                    args -> args[0] + args[1]
                }),
                '-'     : proc({
                    args -> args[0] - args[1]
                }),
                '*'     : proc({
                    args -> args[0] * args[1]
                }),
                '/'     : proc({
                    args -> args[0] / args[1]
                })
        ]
    }

    def proc(closure) {
        ['primitive', closure]
    }
}
