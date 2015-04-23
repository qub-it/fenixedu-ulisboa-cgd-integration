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
package com.qubit.solution.fenixedu.integration.cgd.webservices.messages.photo;

import java.io.Serializable;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Photograph;
import org.fenixedu.academic.domain.photograph.PictureMode;

import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.CgdMessageUtils;

public class SearchMemberPhotoOuputMessage implements Serializable {

    public static int UNAVAILABLE_PHOTO = 1;

    private int replyCode;
    private String name;
    private byte[] photo;

    public SearchMemberPhotoOuputMessage() {
        super();
        setReplyCode(CgdMessageUtils.REPLY_CODE_UNEXISTING_MEMBER);
    }

    public int getReplyCode() {
        return replyCode;
    }

    public void setReplyCode(int replyCode) {
        this.replyCode = replyCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public void populate(Person person, String populationCode, String memberCode) {
        boolean verifyMatch = CgdMessageUtils.verifyMatch(person, populationCode, memberCode);
        if (verifyMatch) {
            Photograph personalPhoto = person.getPersonalPhoto();
            if (personalPhoto == null) {
                setReplyCode(UNAVAILABLE_PHOTO);
                setName(person.getName());
            } else {
                setReplyCode(CgdMessageUtils.REPLY_CODE_OPERATION_OK);
                setName(person.getName());
                byte[] customAvatar = personalPhoto.getCustomAvatar(180, 180, PictureMode.FIT);
                setPhoto(personalPhoto.exportAsJPEG(customAvatar));
            }
        } else {
            setReplyCode(CgdMessageUtils.REPLY_CODE_INFORMATION_NOT_OK);
        }
    }

}
