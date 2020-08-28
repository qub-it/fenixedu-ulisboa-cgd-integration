package com.qubit.solution.fenixedu.integration.cgd.services;

import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

public class CgdAddressProofProvider {

    private static final List<Class<? extends CgdAddressProofGenerator>> CGD_PROOF_PROVIDERS = new ArrayList<>();

    public static void addCgdEnrolmentProofProvider(Class<? extends CgdAddressProofGenerator> provider) {
        CGD_PROOF_PROVIDERS.add(provider);
    }

    public static List<Class<? extends CgdAddressProofGenerator>> getCgdEnrolmentProviders() {
        return Collections.unmodifiableList(CGD_PROOF_PROVIDERS);
    }
}
