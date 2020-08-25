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

import org.apache.commons.lang.StringUtils;
import org.fenixedu.bennu.core.domain.Bennu;

import com.qubit.solution.fenixedu.integration.cgd.webservices.resolver.memberid.IMemberIDAdapter;

import pt.ist.fenixframework.Atomic;

public class CgdIntegrationConfiguration extends CgdIntegrationConfiguration_Base {

    protected CgdIntegrationConfiguration() {
        super();
        if (Bennu.getInstance().getCgdIntegrationConfiguration() != null) {
            throw new IllegalStateException("can only exist one cgdIntegrationConfiguration");
        }
        setRootDomainObject(Bennu.getInstance());
    }

    public static CgdIntegrationConfiguration getInstance() {
        CgdIntegrationConfiguration configuration = Bennu.getInstance().getCgdIntegrationConfiguration();
        if (configuration == null) {
            configuration = createConfiguration();
        }
        return configuration;
    }

    public <T extends IMemberIDAdapter> T getMemberIDStrategy() {
        String memberIDResolverClass = getMemberIDResolverClass();
        if (StringUtils.isEmpty(memberIDResolverClass)) {
            throw new IllegalStateException("No memberIDResolverClass defined. Please define it in the application");
        }
        try {
            Class<?> forName = Class.forName(memberIDResolverClass);
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

    public void uploadMod43Template(final String filename, final byte[] content) {
        CgdMod43Template template = new CgdMod43Template(filename, content);
        cleanTemplate();
        setMod43Template(template);
    }

    public void cleanTemplate() {
        if (hasMod43Template()) {
            CgdMod43Template oldTemplate = getMod43Template();
            setMod43Template(null);
            oldTemplate.delete();
        }
    }

    public boolean hasMod43Template() {
        return getMod43Template() != null;
    }

}
