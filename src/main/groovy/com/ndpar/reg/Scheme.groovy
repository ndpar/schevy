package com.ndpar.reg

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
                'cond-else-clause?'        : condElseClause,
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
            (perform (op initialize-stack))
            (perform (op prompt-for-input) (const EC-Eval>))
            (assign exp (op read))
            (assign env (op get-global-environment))
            (assign continue (label print-result))
            (goto (label eval-dispatch))

            print-result
            (perform (op print-stack-statistics))
            (perform (op announce-output) (const EC-Eval:))
            (perform (op user-print) (reg val))
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
            (test (op eq?) (reg val) (const _*unbound-variable*_))
            (branch (label unbound-variable))
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
            (test (op eq?) (reg val) (const _*illegal-argument*_))
            (branch (label illegal-argument))
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

            illegal-argument
            (perform (op user-print) (reg argl))
            (assign val (const illegal-argument))
            (goto (label signal-error))

            unbound-variable
            (perform (op user-print) (reg exp))
            (assign val (const unbound-variable))
            (goto (label signal-error))

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
        new Machine(regs, ops, script).start()
    }

    static void main(String[] args) {
        new Scheme().start()
    }

    // --------------------------------------------------------------
    // Operations
    // --------------------------------------------------------------

    def adjoinArg = {
        args ->
    }

    def announceOutput = { args ->
        def output = args[0]
        println "\n$output"
    }

    def applyPrimitiveProcedure = {
        args ->
    }

    def assignmentValue = {
        args ->
    }

    def assignmentVariable = {
        args ->
    }

    def beginActions = {
        args ->
    }

    def condActions = {
        args ->
    }

    def condClauses = {
        args ->
    }

    def condElseClause = {
        args ->
    }

    def condPredicate = {
        args ->
    }

    def defineVariable = {
        args ->
    }

    def definitionValue = {
        args ->
    }

    def definitionVariable = {
        args ->
    }

    def emptyArglist = {
        args ->
    }

    def extendEnvironment = {
        args ->
    }

    def firstCond = {
        args ->
    }

    def firstExp = {
        args ->
    }

    def firstOperand = {
        args ->
    }

    def getGlobalEnvironment = { _ ->
        globalEnvironment
    }

    def ifAlternative = {
        args ->
    }

    def ifConsequent = {
        args ->
    }

    def ifPredicate = {
        args ->
    }

    def isApplication = { exp ->
        exp instanceof List
    }

    def isAssignment = { exp ->
        exp[0] == 'set!'
    }

    def isBegin = { exp ->
        exp[0] == 'begin'
    }

    def isCompoundProcedure = {
        args ->
    }

    def isCond = { exp ->
        exp[0] == 'cond'
    }

    def isDefinition = { exp ->
        exp[0] == 'define'
    }

    def isEq = { args ->
        args[0] == args[1]
    }

    def isIf = { exp ->
        exp[0] == 'if'
    }

    def isLambda = { exp ->
        exp[0] == 'lambda'
    }

    def isLastExp = {
        args ->
    }

    def isLastOperand = {
        args ->
    }

    def isLet = { exp ->
        exp[0] == 'let'
    }

    def isNoConds = {
        args ->
    }

    def isNoOperands = {
        args ->
    }

    def isPrimitiveProcedure = {
        args ->
    }

    def isQuoted = { exp ->
        exp[0] == 'quote'
    }

    def isSelfEvaluating = { args ->
        def exp = args[0]
        exp instanceof Number || exp instanceof Boolean
    }

    def isTrue = {
        args ->
    }

    def isVariable = { exp ->
        exp instanceof String
    }

    def lambdaBody = { args ->
        args[2..-1]
    }

    def lambdaParameters = { args ->
        args[1]
    }

    def letCombination = {
        args ->
    }

    def lookupVariableValue = {
        throw new UnsupportedOperationException('FIXME')
    }

    def makeProcedure = {
        throw new UnsupportedOperationException('FIXME')
    }

    def operands = {
        args ->
    }

    def operator = {
        args ->
    }

    def procedureBody = {
        args ->
    }

    def procedureEnvironment = {
        args ->
    }

    def procedureParameters = {
        args ->
    }

    def promptForInput = { args ->
        def prompt = args[0]
        println "\n\n$prompt"
    }

    def read = { _ ->
        System.in.withReader { r -> reader.read(r.readLine()) }
    }

    def restConds = {
        args ->
    }

    def restExps = {
        args ->
    }

    def restOperands = {
        args ->
    }

    def setVariableValue = {
        args ->
    }

    def textOfQuotation = { args ->
        args[1]
    }

    def userPrint = { args ->
        def object = args[0]
        print object
    }

    // --------------------------------------------------------------
    // Environment
    // --------------------------------------------------------------

    def globalEnvironment = setupEnvironment()

    def setupEnvironment() {
        [
                'true'        : true,
                'false'       : false,
                '*unassigned*': '*unassigned*',
                'car'         : proc({
                    args -> args[0][0]
                }),
                'cdr'         : proc({
                    args -> args[1..-1]
                }),
                'cons'        : null,
                'null?'       : null,
                'eq?'         : null,
                '='           : null,
                '<'           : null,
                '+'           : null,
                '-'           : null,
                '*'           : null,
                '/'           : null
        ]
    }

    def proc(closure) {
        ['primitive', closure]
    }
}
