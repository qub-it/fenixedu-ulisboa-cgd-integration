package com.qubit.solution.fenixedu.integration.cgd.services.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;

import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdIntegrationConfiguration;

public class MoreThanOneAssociatedUnitStrategy extends BaseIESCodeProviderStrategy implements CgdIESCodeProviderStrategyClass {

    @Override
    public Set<String> getIESCode(Registration registration) {
        Unit associatedUnit = registration.getDegree().getUnit();

        if (associatedUnit == null) {
            return super.getIESCode(registration);
        }

        return CgdIntegrationConfiguration.getInstance().getAllowsMultipleUnits() ? findAllUnitsCodes(registration,
                associatedUnit) : findFirstUnitFromConfiguration(registration, associatedUnit);
    }

    private Set<String> findAllUnitsCodes(Registration registration, Unit associatedUnit) {
        Set<Unit> allowedUnits = CgdIntegrationConfiguration.getInstance().getUnitsSet();
        Set<String> result = new HashSet<String>();

        if (allowedUnits.contains(associatedUnit)) {
            result.add(associatedUnit.getCode());
        }

        continueSearchUnitWithParentUnits(associatedUnit.getParentUnits(), allowedUnits, result);

        return result.isEmpty() ? super.getIESCode(registration) : result;
    }

    private void continueSearchUnitWithParentUnits(Collection<Unit> associatedParentUnits, Set<Unit> allowedUnitsForSearch,
            Set<String> setToReturn) {
        associatedParentUnits.forEach(unit -> {
            if (allowedUnitsForSearch.contains(unit)) {
                setToReturn.add(unit.getCode());
            }
            continueSearchUnitWithParentUnits(unit.getAllParentUnits(), allowedUnitsForSearch, setToReturn);
        });
    }

    private Set<String> findFirstUnitFromConfiguration(Registration registration, Unit associatedUnit) {
        Set<Unit> allowedUnits = CgdIntegrationConfiguration.getInstance().getUnitsSet();
        LinkedList<Unit> queue = new LinkedList<Unit>();
        queue.add(associatedUnit);

        while (!queue.isEmpty()) {
            Unit unit = queue.poll();

            if (allowedUnits.contains(unit)) {
                //We found the first unit in BFS that is a school unit.
                //We set the code variable with the unit code and break the cycle.
                return Set.of(unit.getCode());
            } else {
                //Polled unit is not a school unit so we add unit parents to continue the search
                queue.addAll(unit.getParentUnits());
            }

        }
        //We didn't find any school unit
        return super.getIESCode(registration);
    }

}
