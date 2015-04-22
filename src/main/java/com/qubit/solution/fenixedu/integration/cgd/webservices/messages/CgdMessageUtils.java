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
 * This file is part of FenixEdu cgdIntegration.
 *
 * FenixEdu cgdIntegration is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu cgdIntegration is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu cgdIntegration.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qubit.solution.fenixedu.integration.cgd.webservices.messages;

import org.fenixedu.academic.domain.Person;

import com.qubit.solution.fenixedu.integration.cgd.services.impl.UsernameAdapter;
import com.qubit.solution.fenixedu.integration.cgd.webservices.resolver.memberid.IMemberIDAdapter;

public class CgdMessageUtils {

    public static int REPLY_CODE_OPERATION_OK = 0;
    public static int REPLY_CODE_INFORMATION_NOT_OK = 1;
    public static int REPLY_CODE_UNEXISTING_MEMBER = 9;

    private static Class<? extends IMemberIDAdapter> clazz = UsernameAdapter.class;

    public static boolean verifyMatch(Person person, String populationCode, String memberCode) {
        boolean matchOk = false;
        switch (populationCode.charAt(0)) {
        case 'A':
            matchOk = person.getStudent() != null && String.valueOf(person.getStudent().getNumber()).equals(memberCode);
            break;
        case 'F':
            // YET TO BE IMPLEMENTED
            break;
        case 'D':
            matchOk = person.getTeacher() != null && person.getTeacher().getTeacherId().equals(memberCode);
            break;
        }

        return matchOk;
    }

    public static IMemberIDAdapter getMemberIDStrategy() {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
