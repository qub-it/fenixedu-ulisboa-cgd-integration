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
package com.qubit.solution.fenixedu.integration.cgd.webservices.messages.mifare;

import java.io.Serializable;

import org.fenixedu.academic.domain.Person;

import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.CgdMessageUtils;

public class UpdateMifareInputMessage implements Serializable {

    // Single character to identify type of member
    // A - student
    // F - employee
    // D - teacher
    private String populationCode;

    // Member identitification the result that was returned
    // by the searchMember service
    private String memberID;

    // The institution code for the member 
    // student code if populationCode = A
    // employee code if populationCode = F
    // teacher code if populationCode = D
    private String memberCode;

    // Format: YYYY-MM-DD
    private String personalizationDate;

    private int chipDataSize;
    private String chipData;
    private String cardIdentification;

    public String getCardIdentification() {
        return cardIdentification;
    }

    public void setCardIdentification(String cardIdentification) {
        this.cardIdentification = cardIdentification;
    }

    public String getPopulationCode() {
        return populationCode;
    }

    public void setPopulationCode(String populationCode) {
        this.populationCode = populationCode;
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getPersonalizationDate() {
        return personalizationDate;
    }

    public void setPersonalizationDate(String personalizationDate) {
        this.personalizationDate = personalizationDate;
    }

    public int getChipDataSize() {
        return chipDataSize;
    }

    public void setChipDataSize(int chipDataSize) {
        this.chipDataSize = chipDataSize;
    }

    public String getChipData() {
        return chipData;
    }

    public void setChipData(String chipData) {
        this.chipData = chipData;
    }

    public Person getIdentifiedPerson() {
        Person person = CgdMessageUtils.getMemberIDStrategy().readPerson(getMemberID());
        if (person == null) {
            person = CgdMessageUtils.readPersonByMemberCode(this.populationCode, this.memberCode);
        }
        return person;
    }
}
