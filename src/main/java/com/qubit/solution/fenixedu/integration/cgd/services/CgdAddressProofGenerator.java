package com.qubit.solution.fenixedu.integration.cgd.services;

import java.util.function.Function;

import org.fenixedu.academic.domain.student.Registration;

public interface CgdAddressProofGenerator extends Function<Registration, byte[]> {

}
