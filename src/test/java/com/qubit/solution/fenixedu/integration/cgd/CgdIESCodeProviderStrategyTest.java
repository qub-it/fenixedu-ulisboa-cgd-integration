package com.qubit.solution.fenixedu.integration.cgd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.organizationalStructure.AccountabilityType;
import org.fenixedu.academic.domain.organizationalStructure.AccountabilityTypeEnum;
import org.fenixedu.academic.domain.organizationalStructure.PartyType;
import org.fenixedu.academic.domain.organizationalStructure.PartyTypeEnum;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.commons.i18n.LocalizedString;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdIntegrationConfiguration;
import com.qubit.solution.fenixedu.integration.cgd.junit.runners.CgdFenixFrameworkTestRunner;
import com.qubit.solution.fenixedu.integration.cgd.mocks.StrategyReturnsOnlyOneTest;
import com.qubit.solution.fenixedu.integration.cgd.services.utils.MoreThanOneAssociatedUnitStrategy;

import pt.ist.fenixframework.FenixFramework;

@RunWith(CgdFenixFrameworkTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CgdIESCodeProviderStrategyTest {

    private static final String UNIVERSITY_CODE = "1111";

    private static final String SCHOOL_CODE = "12345";

    private static Unit scientificUnit1;
    private static Unit scientificUnit2;
    private static Unit scientificUnit3;
    private static Unit earthUnit;
    private static Unit universityUnit;

    @BeforeClass
    public static void setUpTests() {
        /*
         * We start by having the strategy that returns only one
         */
        FenixFramework.getTransactionManager().withTransaction(() -> {
            SetUpCgdIESCodeProviderStrategyTests.generateDataForCgdTests();
            createStartUnits();
            CgdIntegrationConfiguration integration = CgdIntegrationConfiguration.getInstance();
            integration.setAllowsMultipleUnits(false);
            integration.setIesCodeProviderStrategyClass(StrategyReturnsOnlyOneTest.class.getName());
            return null;
        });

    }

    private static void createStartUnits() {
        //Defined in SetUpCgdIESCodeProviderStrategyTests
        earthUnit = Unit.readAllUnits().stream().filter(u -> "E".equals(u.getAcronym())).findFirst().get();
        universityUnit = Unit.createNewUnit(PartyType.of(PartyTypeEnum.UNIVERSITY),
                new LocalizedString.Builder().with(Locale.getDefault(), "UniversityName").build(), "UN", earthUnit,
                AccountabilityType.readByType(AccountabilityTypeEnum.ORGANIZATIONAL_STRUCTURE));
        universityUnit.setCode(UNIVERSITY_CODE);
        Unit schoolUnit = Unit.createNewUnit(PartyType.of(PartyTypeEnum.SCHOOL),
                new LocalizedString.Builder().with(Locale.getDefault(), "SchoolName").build(), "UN", universityUnit,
                AccountabilityType.readByType(AccountabilityTypeEnum.ORGANIZATIONAL_STRUCTURE));
        Bennu.getInstance().setInstitutionUnit(schoolUnit);
        schoolUnit.setCode(SCHOOL_CODE);

        scientificUnit1 = Unit.createNewUnit(PartyType.of(PartyTypeEnum.SCIENTIFIC_AREA),
                new LocalizedString.Builder().with(Locale.getDefault(), "ScientificAreaUnit1").build(), "SAU1", schoolUnit,
                AccountabilityType.readByType(AccountabilityTypeEnum.ORGANIZATIONAL_STRUCTURE));
        scientificUnit1.setCode("13");
        scientificUnit2 = Unit.createNewUnit(PartyType.of(PartyTypeEnum.SCIENTIFIC_AREA),
                new LocalizedString.Builder().with(Locale.getDefault(), "ScientificAreaUnit2").build(), "SAU2", schoolUnit,
                AccountabilityType.readByType(AccountabilityTypeEnum.ORGANIZATIONAL_STRUCTURE));
        scientificUnit2.setCode("23");
        scientificUnit3 = Unit.createNewUnit(PartyType.of(PartyTypeEnum.SCIENTIFIC_AREA),
                new LocalizedString.Builder().with(Locale.getDefault(), "ScientificAreaUnit3").build(), "SAU3", schoolUnit,
                AccountabilityType.readByType(AccountabilityTypeEnum.ORGANIZATIONAL_STRUCTURE));
        scientificUnit3.setCode("33");

        Degree degree = Registration.readByNumber(1).get(0).getDegree();
        degree.setUnit(scientificUnit3);
    }

    @Test
    public void a_assertItWasCreatedRegistrationForStudent() {
        List<Registration> readByNumber = Registration.readByNumber(1);
        assertTrue(readByNumber.size() == 1);
    }

    @Test
    public void b_assertIesCodeProviderReturnsOnlyOne() {
        Registration registration = Registration.readByNumber(1).get(0);
        String iesCode = CgdIntegrationConfiguration.getInstance().getIESCodeProvider().getIESCode(registration).get(0);
        assertEquals("1", iesCode);
    }

    @Test
    public void c_assertProviderChangeWithoutHavingAssociatedUnits() {
        CgdIntegrationConfiguration instance = CgdIntegrationConfiguration.getInstance();
        Registration registration = Registration.readByNumber(1).get(0);
        FenixFramework.getTransactionManager().withTransaction(() -> {
            /*
             * We change the strategy to a real strategy
             * However we don't make other changes - We don't add associated units and we only want one code
             */
            instance.setIESCodProviderStrategyClass(MoreThanOneAssociatedUnitStrategy.class);
            return null;
        });
        /*
         * With these changes we expect to get the school code, since there is no associated unit
         */
        List<String> iesCodeList = instance.getIESCodeProvider().getIESCode(registration);
        assertTrue(iesCodeList.size() == 1);
        /*
         * Since we don't have any associated unit we will find the institutionalUnit (Base strategy)
         */
        assertEquals(SCHOOL_CODE, iesCodeList.get(0));
    }

    @Test
    public void d_addUnitsWithoutChangingAllowedMultipleUnits() {
        //allowedMultipleUnits it's false since it was not changed
        CgdIntegrationConfiguration instance = CgdIntegrationConfiguration.getInstance();
        Registration registration = Registration.readByNumber(1).get(0);
        FenixFramework.getTransactionManager().withTransaction(() -> {
            instance.addUnits(earthUnit); //Parent of university
            instance.addUnits(universityUnit); // Parent of school
            //School is parent of scientific area
            return null;
        });

        List<String> iesCodeList = instance.getIESCodeProvider().getIESCode(registration);
        assertTrue(iesCodeList.size() == 1);
        assertEquals(UNIVERSITY_CODE, iesCodeList.get(0));
    }

    public void e_changeAllowedMultipleUnits() {
        CgdIntegrationConfiguration instance = CgdIntegrationConfiguration.getInstance();
        Registration registration = Registration.readByNumber(1).get(0);
        FenixFramework.getTransactionManager().withTransaction(() -> {
            instance.setAllowsMultipleUnits(true);
            return null;
        });
        List<String> iesCodeList = instance.getIESCodeProvider().getIESCode(registration);
        assertTrue(iesCodeList.size() > 1);

        assertTrue(iesCodeList.contains(UNIVERSITY_CODE));
        assertTrue(iesCodeList.contains(SCHOOL_CODE));
    }

}
