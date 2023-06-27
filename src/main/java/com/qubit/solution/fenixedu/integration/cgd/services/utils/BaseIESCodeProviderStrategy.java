package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import org.fenixedu.bennu.core.domain.Bennu;

public class BaseIESCodeProviderStrategy implements CgdIESCodeProviderStrategyClass {

    @Override
    public String getIESCode() {
        return Bennu.getInstance().getInstitutionUnit().getCode();
    }

}
