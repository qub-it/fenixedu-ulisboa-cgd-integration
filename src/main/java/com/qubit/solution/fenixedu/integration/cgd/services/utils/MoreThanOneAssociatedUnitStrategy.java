package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;

import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdIntegrationConfiguration;

public class MoreThanOneAssociatedUnitStrategy extends BaseIESCodeProviderStrategy implements CgdSchoolCodeProviderStrategyClass {

    @Override
    public Set<String> getSchoolCodes(Registration registration) {
        CgdIntegrationConfiguration instance = CgdIntegrationConfiguration.getInstance();

        return ofNullable(registration.getDegree().getUnit())
                .map(degreeUnit -> breadthFirstSearch(degreeUnit, instance.getUnitsSet(), instance.getAllowsMultipleUnits()))
                .orElseGet(() -> super.getSchoolCodes(registration));

    }

    private Set<String> breadthFirstSearch(Unit startUnit, Set<Unit> acceptedUnits, boolean findFirst) {
        List<Unit> foundUnits = new ArrayList<>();
        Set<Unit> visited = new HashSet<>();
        Queue<Unit> queue = new LinkedList<>();

        queue.offer(startUnit);
        visited.add(startUnit);

        while (!queue.isEmpty()) {
            Unit currentUnit = queue.poll();

            if (acceptedUnits.contains(currentUnit)) {
                foundUnits.add(currentUnit);
                if (findFirst) {
                    break;
                }
            }

            if (!foundUnits.contains(currentUnit)) {
                currentUnit.getParentUnits().stream().filter(parent -> !visited.contains(parent)).forEach(parent -> {
                    queue.offer(parent);
                    visited.add(parent);
                });
            }
        }

        return foundUnits.stream().map(Unit::getCode).collect(Collectors.toSet());
    }

    @Override
    public boolean requiresUnits() {
        return true;
    }

}
