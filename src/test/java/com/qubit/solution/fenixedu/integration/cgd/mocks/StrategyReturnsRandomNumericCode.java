package com.qubit.solution.fenixedu.integration.cgd.mocks;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.fenixedu.academic.domain.student.Registration;

import com.qubit.solution.fenixedu.integration.cgd.services.utils.CgdIESCodeProviderStrategyClass;

public class StrategyReturnsRandomNumericCode implements CgdIESCodeProviderStrategyClass {

    @Override
    public List<String> getIESCode(Registration registration) {
        return List.of(RandomStringUtils.randomNumeric(5));
    }

}
