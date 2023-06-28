package com.qubit.solution.fenixedu.integration.cgd.mocks;

import com.qubit.solution.fenixedu.integration.cgd.services.utils.CgdIESCodeProviderStrategyClass;

public class StrategyReturnsOnlyOneTest implements CgdIESCodeProviderStrategyClass {

    @Override
    public String getIESCode() {
        return "1";
    }

}
