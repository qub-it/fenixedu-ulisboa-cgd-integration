package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

public class CgdIESCodeProviderStrategyClassProvider {

    private static final List<Class<? extends CgdSchoolCodeProviderStrategyClass>> CGD_IES_CODE_PROVIDERS = new ArrayList<>();

    public static void addCgdIESCodeStrategyClassProvider(Class<? extends CgdSchoolCodeProviderStrategyClass> provider) {
        CGD_IES_CODE_PROVIDERS.add(provider);
    }

    public static List<Class<? extends CgdSchoolCodeProviderStrategyClass>> getCgdIESCodeProviderStrategyProviders() {
        return Collections.unmodifiableList(CGD_IES_CODE_PROVIDERS);
    }

}
