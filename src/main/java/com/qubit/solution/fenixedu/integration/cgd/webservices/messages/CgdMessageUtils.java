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
package com.qubit.solution.fenixedu.integration.cgd.webservices.messages;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.student.Student;

import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdIntegrationConfiguration;
import com.qubit.solution.fenixedu.integration.cgd.webservices.resolver.memberid.IMemberIDAdapter;

public class CgdMessageUtils {

    public static int REPLY_CODE_OPERATION_OK = 0;
    public static int REPLY_CODE_INFORMATION_NOT_OK = 1;
    public static int REPLY_CODE_UNEXISTING_MEMBER = 9;

    public static Person readPersonByMemberCode(String populationCode, String memberCode) {
        Person requestedPerson = null;
        if (!StringUtils.isEmpty(memberCode) && !StringUtils.isEmpty(populationCode)) {
            switch (populationCode.charAt(0)) {
            case 'A':
                Student student = Student.readStudentByNumber(Integer.valueOf(memberCode));
                if (student != null) {
                    requestedPerson = student.getPerson();
                }
            case 'E':
                // NOT YET IMPLEMENTED
                break;
            case 'D':
                List<Teacher> readByNumbers = Teacher.readByNumbers(Collections.singleton(memberCode));
                Teacher teacher = readByNumbers.isEmpty() ? null : readByNumbers.iterator().next();
                if (teacher != null) {
                    requestedPerson = teacher.getPerson();
                }
            }
        }
        return requestedPerson;
    }

    public static boolean verifyMatch(Person person, String populationCode, String memberCode, String memberID) {
        boolean matchOk = StringUtils.isEmpty(populationCode) || StringUtils.isEmpty(memberCode);

        if (!matchOk) {
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
        }

        if (matchOk && !StringUtils.isEmpty(memberID)) {
            matchOk = memberID.equals(getMemberIDStrategy().retrieveMemberID(person));
        }

        return matchOk;
    }

    public static IMemberIDAdapter getMemberIDStrategy() {
        return CgdIntegrationConfiguration.getInstance().getMemberIDStrategy();
    }
}
