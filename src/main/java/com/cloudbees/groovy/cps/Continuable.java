package com.cloudbees.groovy.cps;

import com.cloudbees.groovy.cps.impl.CpsCallableInvocation;
import com.cloudbees.groovy.cps.impl.CpsFunction;
import com.cloudbees.groovy.cps.impl.FunctionCallEnv;
import com.cloudbees.groovy.cps.impl.YieldBlock;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Mutable representation of the program.
 *
 * @author Kohsuke Kawaguchi
 */
public class Continuable implements Serializable {
    private Continuation program;

    public Continuable(Continuation program) {
        this.program = program;
    }

    public Continuable(Next program) {
        this(program.asContinuation());
    }

    /**
     * Creates a {@link Continuable} that executes the block of code.
     */
    public Continuable(Block block) {
        this(new Next(block,
                new FunctionCallEnv(null,null,Continuation.HALT),
                Continuation.HALT).asContinuation());
    }

    public Continuable clone() {
        return new Continuable(program);
    }

    /**
     * Runs this program until it suspends the next time.
     */
    public Object run(Object arg) {
        Next n = program.receive(arg).resume();
        // when yielding, we resume from the continuation so that we can pass in the value.
        // see Next#yield
        program = n.k;
        return n.yieldedValue();
    }

    public boolean isResumable() {
        return program!=Continuation.HALT;
    }

    /**
     * Called from within CPS transformed program to suspends the execution,
     * then have the caller of {@link Next#resume()} return with the object given to this method.
     * When the execution is resumed,
     */
    public static Object suspend(final Object v) {
        throw new CpsCallableInvocation(new CpsFunction(Arrays.asList("v"), new YieldBlock(v)),null,v);
    }

    private static final long serialVersionUID = 1L;
}