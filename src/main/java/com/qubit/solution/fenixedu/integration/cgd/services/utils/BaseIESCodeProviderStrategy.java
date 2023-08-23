package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import java.util.Set;

import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.domain.Bennu;

public class BaseIESCodeProviderStrategy implements CgdSchoolCodeProviderStrategyClass {

    @Override
    public Set<String> getSchoolCodes(Registration registration) {
        return Set.of(Bennu.getInstance().getInstitutionUnit().getCode());
    }

    @Override
    public boolean requiresUnits() {
        return false;
    }

}
