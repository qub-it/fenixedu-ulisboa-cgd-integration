package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import java.util.List;

import org.fenixedu.academic.domain.student.Registration;

public interface CgdIESCodeProviderStrategyClass {

    public List<String> getIESCode(Registration registration);
}
