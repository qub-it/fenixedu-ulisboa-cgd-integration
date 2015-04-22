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
package com.qubit.solution.fenixedu.integration.cgd.webservices.messages.member;

import java.io.Serializable;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.student.Student;

public class SearchMemberInput implements Serializable {

    // Identifies which kind of document will be sent
    // in documentID
    //
    // 101 - BI/CC
    // 501 - NIF
    private int documentType;

    // DocumentID 
    private String documentID;

    // Employee code/Student code when there is no
    // identification provided 
    private int code;

    public int getDocumentType() {
        return documentType;
    }

    public void setDocumentType(int documentType) {
        this.documentType = documentType;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Person getIdentifiedPerson() {
        Person requestedPerson = null;
        if (documentType == 101) {
            requestedPerson = Person.readByDocumentIdNumberAndIdDocumentType(documentID, IDDocumentType.CITIZEN_CARD);
            if (requestedPerson == null) {
                requestedPerson = Person.readByDocumentIdNumberAndIdDocumentType(documentID, IDDocumentType.IDENTITY_CARD);
            }
        } else if (documentType == 501) {
            Party party = requestedPerson.readByContributorNumber(documentID);
            requestedPerson = (party instanceof Person) ? (Person) party : null;
        }

        if (requestedPerson == null) {
            // We'll fall back to the student code search.
            // ONLY SUPPORTING student code for now
            Student student = Student.readStudentByNumber(code);
            requestedPerson = student != null ? student.getPerson() : null;
        }

        return requestedPerson;
    }

}
