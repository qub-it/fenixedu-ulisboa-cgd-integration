package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import java.util.Set;

import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.domain.Bennu;

public class BaseIESCodeProviderStrategy implements CgdIESCodeProviderStrategyClass {

    @Override
    public Set<String> getIESCode(Registration registration) {
        return Set.of(Bennu.getInstance().getInstitutionUnit().getCode());
    }

    @Override
    public boolean requiresUnits() {
        return false;
    }

}
