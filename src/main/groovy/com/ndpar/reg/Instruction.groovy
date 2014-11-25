package com.ndpar.reg

class Instruction {

    Object text
    List label
    Object breakpoint
    Closure procedure

//    Instruction(text, proc) {
//        this.text = text
//        this.procedure = proc
//    }

    boolean noLabel() {
        label.empty
    }
}
