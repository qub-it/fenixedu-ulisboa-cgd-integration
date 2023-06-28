package com.qubit.solution.fenixedu.integration.cgd.junit.runners;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class CgdFenixFrameworkTestRunner extends BlockJUnit4ClassRunner {

    public CgdFenixFrameworkTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    @Atomic(mode = TxMode.WRITE)
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        super.runChild(method, notifier);
    }
}
