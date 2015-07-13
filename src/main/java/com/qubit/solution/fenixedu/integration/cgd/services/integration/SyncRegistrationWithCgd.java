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
package com.qubit.solution.fenixedu.integration.cgd.services.integration;

import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.ulisboa.specifications.domain.student.access.importation.external.SyncRegistrationWithExternalServices;

import com.qubit.solution.fenixedu.integration.cgd.services.form43.CgdForm43Sender;

public class SyncRegistrationWithCgd implements SyncRegistrationWithExternalServices {

    private CgdForm43Sender cgdForm43Sender;
    private Long timestamp;

    private CgdForm43Sender getSender() {
        // We cache a cgdForm43Sender for at least 60 seconds 
        // so we're not creating a form43 for each call
        //
        // 13 July 2015 - Paulo Abrantes
        if (cgdForm43Sender == null || (System.currentTimeMillis() - timestamp) > 1000 * 60) {
            cgdForm43Sender = new CgdForm43Sender();
            timestamp = System.currentTimeMillis();
        }
        return cgdForm43Sender;
    }

    @Override
    public boolean syncRegistrationToExternal(Registration registration) {
        return getSender().sendForm43For(registration);
    }

}
