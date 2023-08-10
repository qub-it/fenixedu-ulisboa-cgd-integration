package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import java.util.Set;

import org.fenixedu.academic.domain.student.Registration;

public interface CgdIESCodeProviderStrategyClass {

    public Set<String> getIESCode(Registration registration);

    public boolean requiresUnits();
}
