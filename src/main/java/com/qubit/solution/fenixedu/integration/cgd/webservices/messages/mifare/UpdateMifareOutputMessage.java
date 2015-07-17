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
import org.fenixedu.ulisboa.specifications.domain.idcards.CgdCard;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.CgdMessageUtils;

public class UpdateMifareOutputMessage implements Serializable {

    private int replyCode;

    public UpdateMifareOutputMessage() {
        super();
        setReplyCode(CgdMessageUtils.REPLY_CODE_UNEXISTING_MEMBER);
    }

    public int getReplyCode() {
        return replyCode;
    }

    public void setReplyCode(int replyCode) {
        this.replyCode = replyCode;
    }

    public void populate(Person person, String populationCode, String memberCode, String mifareCode, String cardId,
            LocalDate issueDate) {
        if (CgdMessageUtils.verifyMatch(person, populationCode, memberCode)) {
            modifyMifare(person, mifareCode, issueDate, cardId);
            setReplyCode(CgdMessageUtils.REPLY_CODE_OPERATION_OK);
        } else {
            setReplyCode(CgdMessageUtils.REPLY_CODE_INFORMATION_NOT_OK);
        }
    }

    @Atomic
    private void modifyMifare(Person person, String mifareCode, LocalDate issueDate, String cardId) {
        CgdCard card = CgdCard.findByPerson(person);
        if (card == null) {
            card = new CgdCard(person, mifareCode, false);
        }
        card.setMifareCode(mifareCode);
        card.setIssueDate(issueDate);
        card.setTemporary(false);
        card.setCardNumber(cardId);
    }

}
