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
package com.qubit.solution.fenixedu.integration.cgd.webservices.messages.member;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.FenixEduAcademicConfiguration;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academic.domain.person.IDDocumentType;

import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdIntegrationConfiguration;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.CgdMessageUtils;

public class SearchMemberInput implements Serializable {

    private static int IDCARD_TYPE = 101;
    private static int TAXNUMBER_TYPE = 501;

    // Single character to identify type of member
    // A - student
    // F - employee
    // D - teacher
    private String populationCode;

    // Member identitification will be sent if the
    // user is already known
    private String memberID;

    // Identifies which kind of document will be sent
    // in documentID
    //
    // 101 - BI/CC
    // 501 - NIF
    private Integer documentType;

    // DocumentID 
    private String documentID;

    // The institution code for the member 
    // student code if populationCode = A
    // employee code if populationCode = F
    // teacher code if populationCode = D
    private String memberCode;

    public Integer getDocumentType() {
        return documentType;
    }

    public void setDocumentType(Integer documentType) {
        this.documentType = documentType;
    }

    public String getDocumentID() {
        return documentID != null ? documentID.trim() : documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getPopulationCode() {
        return populationCode != null ? populationCode.trim() : populationCode;
    }

    public void setPopulationCode(String populationCode) {
        this.populationCode = populationCode;
    }

    public String getMemberID() {
        return memberID != null ? memberID.trim() : memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public String getMemberCode() {
        return memberCode != null ? memberCode.trim() : memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public Person getIdentifiedPerson() {
        Person requestedPerson = null;

        // If CGD already knows the member will send the memberID. This is the unique
        // identification of the member inside the institution so we'll short-circuit 
        // the lookup using the memberID
        // 23 April 2015 - Paulo Abrantes
        //
        // Update after phone talk with Marcelino Lopes from CGD we've established that
        // in the SearchMemberService the resolution must be:
        // * Lookup by memberID
        // * If not found lookup by documentID
        // * If not found lookup by memberCode
        //
        // 23 July 2015 - Paulo Abrantes
        if (!StringUtils.isEmpty(getMemberID())) {
            requestedPerson = CgdIntegrationConfiguration.getInstance().getMemberIDStrategy().readPerson(getMemberID());
        }

        if (requestedPerson == null) {
            if (documentType != null && documentType == IDCARD_TYPE) {
                requestedPerson = Person.readByDocumentIdNumberAndIdDocumentType(getDocumentID(), IDDocumentType.CITIZEN_CARD);
                if (requestedPerson == null) {
                    requestedPerson =
                            Person.readByDocumentIdNumberAndIdDocumentType(getDocumentID(), IDDocumentType.IDENTITY_CARD);
                }
                if (requestedPerson == null) {
                    Collection<Person> people = Person.readByDocumentIdNumber(getDocumentID());
                    requestedPerson = people.isEmpty() ? null : people.iterator().next();

                }
            } else if (documentType != null && documentType == TAXNUMBER_TYPE) {
                String defaultSocialSecurityNumber =
                        FenixEduAcademicConfiguration.getConfiguration().getDefaultSocialSecurityNumber();
                if (defaultSocialSecurityNumber == null || !defaultSocialSecurityNumber.equals(documentID)) {
                    Party party = Person.readByContributorNumber(documentID);
                    requestedPerson = (party instanceof Person) ? (Person) party : null;
                }
            }
        }

        if (requestedPerson == null && !StringUtils.isEmpty(getMemberCode())) {
            requestedPerson = CgdMessageUtils.readPersonByMemberCode(getPopulationCode(), getMemberCode());
        }
        return requestedPerson;
    }
}
