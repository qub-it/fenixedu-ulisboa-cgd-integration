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
package com.qubit.solution.fenixedu.integration.cgd.services.memberid;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.person.identificationDocument.IdentificationDocument;
import org.fenixedu.academic.domain.person.identificationDocument.IdentificationDocumentType;

import com.qubit.solution.fenixedu.integration.cgd.webservices.resolver.memberid.IMemberIDAdapter;

public class IDcardAdapter implements IMemberIDAdapter {

    @Override
    public String retrieveMemberID(Person person) {
        IdentificationDocument doc = person.getIdentificationDocumentsSet().stream()
                .filter(document -> document.getIdentificationDocumentType().getCode()
                        .equals(IdentificationDocumentType.IDENTITY_CARD_CODE)).findFirst()
                .orElse(null);
        return doc != null ? doc.getValue() : person.getDefaultIdentificationDocument().getValue();

    }

    @Override
    public Person readPerson(String memberID) {
        Person person =
                find(memberID, IdentificationDocumentType.findByCode(IdentificationDocumentType.IDENTITY_CARD_CODE).orElse(null));
        if (person == null) {
            Collection<Person> people = find(memberID);
            person = people.isEmpty() ? null : people.iterator().next();
        }
        return person;
    }

    // Instead of using domain's document finders we are recreating them here so we can 
    // perform the normalization process in the document numbers as well
    //
    // 13 September 2021 - Paulo Abrantes
    private Collection<Person> find(String idDocumentValue) {
        idDocumentValue = normalizeMemberID(idDocumentValue);

        return Person.findByDocumentIdentification(idDocumentValue).collect(Collectors.toSet());
    }

    private Person find(String idDocumentValue, final IdentificationDocumentType documentType) {
        idDocumentValue = normalizeMemberID(idDocumentValue);

        return Person.findByDocumentIdentification(idDocumentValue, documentType).orElse(null);
    }

    // According to the CGD specification for the memberID field when the value is numeric leading zeros 
    // have to be ignored while comparting as well as any white spaces. According to their documentation 
    // all the following strings match "123":
    //
    // - "123          "
    // - "0000000000123"
    // - "000123       "
    // - "          123"
    //
    // If, on the other hand, the memberID is not numeric then the value should be left unaltered (including
    // not trimming).
    //
    // 13 September 2021 - Paulo Abrantes
    private String normalizeMemberID(String idDocumentValue) {
        String trimmedValue = idDocumentValue.trim();
        return StringUtils.isNumeric(trimmedValue) ? trimmedValue.replaceFirst("^0+", "") : idDocumentValue;
    }

}
