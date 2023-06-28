package com.qubit.solution.fenixedu.integration.cgd.mocks;

import org.apache.commons.lang3.RandomStringUtils;

import com.qubit.solution.fenixedu.integration.cgd.services.utils.CgdIESCodeProviderStrategyClass;

public class StrategyReturnsRandomNumericCode implements CgdIESCodeProviderStrategyClass {

    @Override
    public String getIESCode() {
        return RandomStringUtils.randomNumeric(5);
    }

}
