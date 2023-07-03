package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import java.util.List;

import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.domain.Bennu;

public class BaseIESCodeProviderStrategy implements CgdIESCodeProviderStrategyClass {

    @Override
    public List<String> getIESCode(Registration registration) {
        return List.of(Bennu.getInstance().getInstitutionUnit().getCode());
    }

}
