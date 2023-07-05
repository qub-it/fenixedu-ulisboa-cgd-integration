package com.qubit.solution.fenixedu.integration.cgd.mocks;

import java.util.Set;

import org.fenixedu.academic.domain.student.Registration;

import com.qubit.solution.fenixedu.integration.cgd.services.utils.CgdIESCodeProviderStrategyClass;

public class StrategyReturnsOnlyOneTest implements CgdIESCodeProviderStrategyClass {

    @Override
    public Set<String> getIESCode(Registration registration) {
        return Set.of("1");
    }

}
