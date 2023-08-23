package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import java.util.Set;

import org.fenixedu.academic.domain.student.Registration;

public interface CgdSchoolCodeProviderStrategyClass {

    public Set<String> getSchoolCodes(Registration registration);

    public boolean requiresUnits();
}
