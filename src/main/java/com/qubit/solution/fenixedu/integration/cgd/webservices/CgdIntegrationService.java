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
package com.qubit.solution.fenixedu.integration.cgd.webservices;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.fenixedu.academic.domain.Person;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import com.qubit.solution.fenixedu.bennu.webservices.services.server.BennuWebService;
import com.qubit.solution.fenixedu.integration.cgd.domain.logs.CgdCommunicationLog;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.CgdMessageUtils;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.member.SearchMemberInput;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.member.SearchMemberOutput;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.mifare.UpdateMifareInputMessage;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.mifare.UpdateMifareOutputMessage;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.photo.SearchMemberPhotoInputMessage;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.photo.SearchMemberPhotoOuputMessage;

@WebService
public class CgdIntegrationService extends BennuWebService {

    @WebMethod
    public SearchMemberOutput searchMember(SearchMemberInput message) {
        final String methodName = "searchMember";
        try {
            SearchMemberOutput outputMessage = new SearchMemberOutput();
            Person identifiedPerson = message.getIdentifiedPerson();
            if (identifiedPerson != null) {
                outputMessage.populate(identifiedPerson, message.getPopulationCode(), message.getMemberCode(),
                        message.getMemberID());
                CgdCommunicationLog.findLatestStudentLog(identifiedPerson).ifPresent(log -> log.updateSearchDate());
            }

            CgdMessageUtils.log(methodName, message, outputMessage);
            return outputMessage;
        } catch (Exception ex) {
            CgdMessageUtils.log(methodName, message, ex);
            throw ex;
        }
    }

    @WebMethod
    public SearchMemberPhotoOuputMessage searchPhoto(SearchMemberPhotoInputMessage message) {
        final String methodName = "searchPhoto";
        try {
            SearchMemberPhotoOuputMessage outputMessage = new SearchMemberPhotoOuputMessage();
            Person person = message.getIdentifiedPerson();
            if (person != null) {
                outputMessage.populate(person, message.getPopulationCode(), message.getMemberCode(), message.getMemberID());
            }

            CgdMessageUtils.log(methodName, message, outputMessage);
            return outputMessage;
        } catch (Exception ex) {
            CgdMessageUtils.log(methodName, message, ex);
            throw ex;
        }
    }

    @WebMethod
    public UpdateMifareOutputMessage updateChip(UpdateMifareInputMessage message) {
        final String methodName = "updateChip";
        try {
            UpdateMifareOutputMessage outputMessage = new UpdateMifareOutputMessage();
            Person person = message.getIdentifiedPerson();
            if (person != null) {
                outputMessage.populate(person, message.getPopulationCode(), message.getMemberCode(), message.getMemberID(),
                        message.getChipData(), message.getCardIdentification(),
                        LocalDate.parse(message.getPersonalizationDate(), DateTimeFormat.forPattern("YYYY-MM-dd")));
            }

            CgdMessageUtils.log(methodName, message, outputMessage);
            return outputMessage;
        } catch (Exception ex) {
            CgdMessageUtils.log(methodName, message, ex);
            throw ex;
        }
    }

}
