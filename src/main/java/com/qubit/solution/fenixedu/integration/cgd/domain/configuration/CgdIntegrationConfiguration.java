/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: paulo.abrantes@qub-it.com
 *
 * 
 * This file is part of FenixEdu fenixedu-ulisboa-cgdIntegration.
 *
 * FenixEdu fenixedu-ulisboa-cgdIntegration is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu fenixedu-ulisboa-cgdIntegration is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu fenixedu-ulisboa-cgdIntegration.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qubit.solution.fenixedu.integration.cgd.domain.configuration;

import java.util.Set;

import org.fenixedu.bennu.core.domain.Bennu;

import pt.ist.fenixframework.Atomic;

import com.qubit.solution.fenixedu.integration.cgd.webservices.resolver.memberid.IMemberIDAdapter;

public class CgdIntegrationConfiguration extends CgdIntegrationConfiguration_Base {

    protected CgdIntegrationConfiguration() {
        super();
        if (!Bennu.getInstance().getCgdIntegrationConfigurationsSet().isEmpty()) {
            throw new IllegalStateException("can only exist one cgdIntegrationConfiguration");
        }
        setRootDomainObject(Bennu.getInstance());
    }

    public static CgdIntegrationConfiguration getInstance() {
        Set<CgdIntegrationConfiguration> cgdIntegrationConfigurationsSet =
                Bennu.getInstance().getCgdIntegrationConfigurationsSet();
        CgdIntegrationConfiguration configuration = null;
        if (cgdIntegrationConfigurationsSet.isEmpty()) {
            configuration = createConfiguration();
        } else {
            configuration = cgdIntegrationConfigurationsSet.iterator().next();
        }
        return configuration;
    }

    public <T extends IMemberIDAdapter> T getMemberIDStrategy() {
        try {
            Class<?> forName = Class.forName(getMemberIDResolverClass());
            return (T) forName.newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Atomic
    private static CgdIntegrationConfiguration createConfiguration() {
        return new CgdIntegrationConfiguration();
    }

}
