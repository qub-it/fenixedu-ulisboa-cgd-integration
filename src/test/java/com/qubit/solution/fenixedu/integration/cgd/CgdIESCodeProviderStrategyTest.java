package com.qubit.solution.fenixedu.integration.cgd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdIntegrationConfiguration;
import com.qubit.solution.fenixedu.integration.cgd.junit.runners.CgdFenixFrameworkTestRunner;
import com.qubit.solution.fenixedu.integration.cgd.mocks.StrategyReturnsOnlyOneTest;
import com.qubit.solution.fenixedu.integration.cgd.mocks.StrategyReturnsRandomNumericCode;

import pt.ist.fenixframework.FenixFramework;

@RunWith(CgdFenixFrameworkTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CgdIESCodeProviderStrategyTest {

    /*
     * We start by having the strategy that returns only one
     */
    @BeforeClass
    public static void setUpTests() {
        FenixFramework.getTransactionManager().withTransaction(() -> {
            CgdIntegrationConfiguration integration = CgdIntegrationConfiguration.getInstance();
            integration.setIesCodeProviderStrategyClass(StrategyReturnsOnlyOneTest.class.getName());
            return null;
        });

    }

    @Test
    public void a_assertIesCodeProviderReturnIsOne() {
        String iesCode = CgdIntegrationConfiguration.getInstance().getIESCodeProvider().getIESCode();
        assertEquals("1", iesCode);
    }

    @Test
    public void b_assertIesCodeProviderReturnsAnotherValue() {
        FenixFramework.getTransactionManager().withTransaction(() -> {
            CgdIntegrationConfiguration integration = CgdIntegrationConfiguration.getInstance();
            integration.setIesCodeProviderStrategyClass(StrategyReturnsRandomNumericCode.class.getName());
            return null;
        });
        String iesCode = CgdIntegrationConfiguration.getInstance().getIESCodeProvider().getIESCode();
        assertFalse(iesCode.equals("1"));
    }

    @Test
    public void c_assertProviderChange() {
        CgdIntegrationConfiguration integration = CgdIntegrationConfiguration.getInstance();
        assertFalse(integration.getIesCodeProviderStrategyClass().equals(StrategyReturnsOnlyOneTest.class.getName()));

    }

}
