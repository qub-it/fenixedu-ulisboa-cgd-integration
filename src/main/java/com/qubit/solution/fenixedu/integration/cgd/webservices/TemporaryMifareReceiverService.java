/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2016 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2016 Universidade de Lisboa (after any Go-Live phase)
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
import org.fenixedu.ulisboa.specifications.domain.idcards.CgdCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;

import com.qubit.solution.fenixedu.bennu.webservices.services.server.BennuWebService;
import com.qubit.solution.fenixedu.integration.cgd.services.form43.CgdForm43Sender;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.CgdMessageUtils;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.mifareReceiver.Response;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.mifareReceiver.Response.ErrorCode;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.mifareReceiver.Response.Status;

@WebService
public class TemporaryMifareReceiverService extends BennuWebService {

    private static Logger logger = LoggerFactory.getLogger(TemporaryMifareReceiverService.class);

    private static String IES_CODE = null;

    private String getIESCode() {
        if (IES_CODE == null) {
            IES_CODE = new CgdForm43Sender().getSchooldIESCode();
        }
        return IES_CODE;
    }

    @WebMethod
    public Response receiveMifare(String mifare, String memberNumber, String memberCategoryCode, String iesCode) {

        Response response = new Response();
        if (getIESCode() != null && !getIESCode().equals(iesCode)) {
            response.setStatus(Status.NOK);
            response.setErrorCode(ErrorCode.INVALID_DATA);
            response.setErrorDescription("Wrong iesCode, expected: " + getIESCode() + " received " + iesCode);
        } else {
            try {
                Person readPerson = CgdMessageUtils.getMemberIDStrategy().readPerson(memberNumber);
                if (readPerson == null) {
                    response.setStatus(Status.NOK);
                    response.setErrorCode(ErrorCode.INVALID_DATA);
                    response.setErrorDescription("No member with identification: " + memberNumber);
                } else {
                    CgdCard card = CgdCard.findByPerson(readPerson);
                    if (card != null) {
                        updateCgdCard(card, mifare);
                    } else {
                        createCgdCard(readPerson, mifare);
                    }
                }

            } catch (Throwable t) {
                logger.error("Problem while receiving temporary mifare", t);
                response.setStatus(Status.NOK);
                response.setErrorCode(ErrorCode.INTERNAL_ERROR);
                response.setErrorDescription(t.getMessage());
            }
        }
        return response;
    }

    @Atomic
    private void updateCgdCard(CgdCard card, String mifare) {
        card.setMifareCode(mifare);
    }

    @Atomic
    private void createCgdCard(Person readPerson, String mifare) {
        new CgdCard(readPerson, mifare, true);
    }
}
