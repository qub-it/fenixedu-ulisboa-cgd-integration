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
package com.qubit.solution.fenixedu.integration.cgd.services.form43;

import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.BindingProvider;

import org.apache.commons.lang.StringUtils;
import org.datacontract.schemas._2004._07.wingman_cgd_caixaiu_datacontract.School;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.ProfessionalSituationConditionType;
import org.fenixedu.academic.domain.contacts.PhysicalAddress;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.person.Gender;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.student.PersonalIngressionData;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.YearMonthDay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.caixaiu.cgd.wingman.iesservice.Client;
import services.caixaiu.cgd.wingman.iesservice.Form43Digital;
import services.caixaiu.cgd.wingman.iesservice.IESService;
import services.caixaiu.cgd.wingman.iesservice.IIESService;
import services.caixaiu.cgd.wingman.iesservice.IdentificationCard;
import services.caixaiu.cgd.wingman.iesservice.ObjectFactory;
import services.caixaiu.cgd.wingman.iesservice.OperationResult;
import services.caixaiu.cgd.wingman.iesservice.Person;
import services.caixaiu.cgd.wingman.iesservice.Student;
import services.caixaiu.cgd.wingman.iesservice.ValidationResult;
import services.caixaiu.cgd.wingman.iesservice.Worker;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;
import com.qubit.solution.fenixedu.bennu.webservices.services.client.BennuWebServiceClient;
import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdIntegrationConfiguration;

public class CgdForm43Sender extends BennuWebServiceClient<IIESService> {

    @Override
    protected BindingProvider getService() {
        return (BindingProvider) new IESService().getBasicHttpBindingIIESService();
    }

    private static services.caixaiu.cgd.wingman.iesservice.ObjectFactory objectFactory = new ObjectFactory();
    private static Logger logger = LoggerFactory.getLogger(CgdForm43Sender.class);

    public boolean sendForm43For(Registration registration, boolean requestCard) {
        IIESService service = (IIESService) getClient();
        boolean success = false;
        try {
            org.fenixedu.academic.domain.Person person = registration.getStudent().getPerson();
            Client clientData = createClient(person, service);
            Person personData = createPerson(person);
            Worker workerData = createWorker(person);
            Student studentData = createStudent(registration);

            Form43Digital form43Digital = new Form43Digital();
            form43Digital.setClientData(clientData);
            form43Digital.setPersonData(personData);
            form43Digital.setProfessionalData(workerData);
            form43Digital.setStudentData(studentData);
            form43Digital.setIDCardProduction(requestCard);

            OperationResult setForm43DigitalData = service.setForm43DigitalData(form43Digital);
            success = !setForm43DigitalData.isError();
            if (!success) {
                logger.info("Problems while trying to send form 43 to student with number: "
                        + registration.getStudent().getNumber() + "with message: "
                        + setForm43DigitalData.getFriendlyMessage().getValue() + "\nCode id: " + setForm43DigitalData.getCodeId()
                        + "\n Unique Error ID: " + setForm43DigitalData.getUEC()
                        + "\n In case there are violations they'll be present bellow ");
                for (ValidationResult validation : setForm43DigitalData.getViolations().getValue().getValidationResult()) {
                    logger.error("Validation error : " + validation.getErrorMessage().getValue() + " [member: "
                            + validation.getMemberNames().getValue().getString().toString() + "]");
                }
            }
        } catch (Throwable t) {
            logger.warn("Problems while trying to send form43 for student with number: " + registration.getStudent().getNumber(),
                    t);
        }

        return success;
    }

    private static String getInstitutionCode() {
        String code = Bennu.getInstance().getInstitutionUnit().getCode();

        // TEST since code is still null
        if (code == null) {
            code = "801";
        }

        return code;
    }

    private static Client createClient(org.fenixedu.academic.domain.Person person, IIESService service) {
        Client client = new Client();
        String findIES = findIES(getInstitutionCode(), service);
        client.setIES(findIES);
        client.setGroup(objectFactory.createClientGroup("1")); // Fernando Nunes indicou que é o protocolo e neste caso será sempre 1
        client.setMemberCategoryCode("91"); // Resposta da Carla Récio a 19 do 6 indica que grande parte das escolas usam ALUNOS 
        String retrieveMemberID = CgdIntegrationConfiguration.getInstance().getMemberIDStrategy().retrieveMemberID(person);
        client.setMemberNumber(retrieveMemberID);

        return client;
    }

    private static Worker createWorker(org.fenixedu.academic.domain.Person person) {
        Worker worker = new Worker();
        worker.setIsWorker(person.getStudent().isWorkingStudent());
        PersonalIngressionData personalIngressionDataByExecutionYear =
                person.getStudent().getPersonalIngressionDataByExecutionYear(ExecutionYear.readCurrentExecutionYear());

        if (personalIngressionDataByExecutionYear != null) {
            // should we also skip this?
            worker.setSituationCode(objectFactory
                    .createWorkerSituationCode(getCodeForProfessionalCondition(personalIngressionDataByExecutionYear
                            .getProfessionalCondition())));
            // Skipping employeer
            // Skpping situationCode
            // Skipping fiscal country code
        }
        return worker;
    }

    private static String getCodeForProfessionalCondition(ProfessionalSituationConditionType professionalCondition) {
        switch (professionalCondition) {
        case WORKS_FOR_OTHERS:
            return "1";
        case EMPLOYEER:
        case INDEPENDENT_WORKER:
            return "2";
        case WORKS_FOR_FAMILY_WITHOUT_PAYMENT:
        case RETIRED:
        case UNEMPLOYED:
        case HOUSEWIFE:
        case STUDENT:
        case MILITARY_SERVICE:
        case OTHER:
        case UNKNOWN:
        default:
            return "3";
        }

    }

    private static Person createPerson(org.fenixedu.academic.domain.Person person) {
        Person personData = new Person();
        personData.setName(person.getName());
        personData.setEmail(objectFactory.createPersonEmail(person.getInstitutionalEmailAddressValue()));
        personData.setGenderCode(objectFactory.createPersonGenderCode(getCodeForGender(person.getGender())));
        try {
            personData.setBirthDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(
                    person.getDateOfBirthYearMonthDay().toDateTimeAtMidnight().toGregorianCalendar()));
        } catch (DatatypeConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        personData.setFiscalNumber(objectFactory.createPersonFiscalNumber(person.getSocialSecurityNumber()));
        PersonalIngressionData personalIngressionDataByExecutionYear =
                person.getStudent().getPersonalIngressionDataByExecutionYear(ExecutionYear.readCurrentExecutionYear());
        if (personalIngressionDataByExecutionYear != null) {
            org.fenixedu.academic.domain.person.MaritalStatus maritalStatus =
                    personalIngressionDataByExecutionYear.getMaritalStatus();
            personData.setMaritalStatusCode(objectFactory.createPersonMaritalStatusCode(getCodeForMaritalStatus(maritalStatus)));
        }
        personData.setFather(objectFactory.createPersonFather(getShortNameFor(person.getNameOfFather())));
        personData.setMother(objectFactory.createPersonMother(getShortNameFor(person.getNameOfMother())));

        personData.setPlaceOfBirthCountryCode(objectFactory.createPersonPlaceOfBirthCountryCode(person.getCountryOfBirth()
                .getCode()));

        personData.setPlaceOfBirthDistrict(objectFactory.createPersonPlaceOfBirthDistrict(person.getDistrictOfBirth()));
        personData.setPlaceOfBirthCounty(objectFactory.createPersonPlaceOfBirthCounty(person.getDistrictSubdivisionOfBirth()));
        personData.setPlaceOfBirthParish(objectFactory.createPersonPlaceOfBirthParish(person.getParishOfBirth()));

        ArrayOfstring nationality = new ArrayOfstring();
        nationality.getString().add(person.getCountry().getCode());
        personData.setNationalities(nationality);

        PhysicalAddress defaultPhysicalAddress = person.getDefaultPhysicalAddress();
        personData.setAddress(objectFactory.createPersonAddress(defaultPhysicalAddress.getAddress()));
        personData.setPlace(objectFactory.createPersonPlace(defaultPhysicalAddress.getArea()));
        personData.setPostalCode(objectFactory.createPersonPostalCode(defaultPhysicalAddress.getAreaCode()));
        personData.setDistrict(objectFactory.createPersonDistrict(defaultPhysicalAddress.getDistrictOfResidence()));
        personData.setCounty(objectFactory.createPersonCounty(defaultPhysicalAddress.getDistrictSubdivisionOfResidence()));
        personData.setParish(objectFactory.createPersonParish(defaultPhysicalAddress.getParishOfResidence()));

        personData.setCountryOfResidenceCode(objectFactory.createPersonCountryOfResidenceCode(person.getCountryOfResidence()
                .getCode()));

        personData.setPhone(objectFactory.createPersonPhone(person.getDefaultPhoneNumber()));
        personData.setMobilePhone(objectFactory.createPersonMobilePhone(person.getDefaultMobilePhoneNumber()));

        IdentificationCard card = new IdentificationCard();

        // skipping issuer
        String codeForDocumentType = getCodeForDocumentType(person.getIdDocumentType());
        if (codeForDocumentType != null) {
            card.setTypeCode(objectFactory.createIdentificationCardTypeCode(codeForDocumentType));
        }
        card.setNumber(person.getDocumentIdNumber());
        // skipping issuer country code
        try {
            YearMonthDay expirationDateOfDocumentIdYearMonthDay = person.getExpirationDateOfDocumentIdYearMonthDay();
            if (expirationDateOfDocumentIdYearMonthDay != null) {
                card.setExpirationDate(objectFactory.createIdentificationCardExpirationDate(DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(
                                expirationDateOfDocumentIdYearMonthDay.toDateTimeAtMidnight().toGregorianCalendar())));
            }
        } catch (DatatypeConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        personData.setIdentificationCard(objectFactory.createIdentificationCard(card));

        return personData;
    }

    private static String getShortNameFor(String name) {
        String result = name;
        if (!StringUtils.isEmpty(name)) {
            String[] split = name.split(" ");
            result = split[0] + " " + split[split.length - 1];
        }
        return result;
    }

    private static String getCodeForDocumentType(IDDocumentType idDocumentType) {
        switch (idDocumentType) {
        case IDENTITY_CARD:
            return "101";
        case PASSPORT:
            return "302";
        case FOREIGNER_IDENTITY_CARD:
            return "301";
        case NATIVE_COUNTRY_IDENTITY_CARD:
            return "301";
        case NAVY_IDENTITY_CARD:
            return "203";
        case AIR_FORCE_IDENTITY_CARD:
            return "202";
        case OTHER:
            return null;
        case MILITARY_IDENTITY_CARD:
            return "201";
        case EXTERNAL:
            return null;
        case CITIZEN_CARD:
            return "801";
        case RESIDENCE_AUTHORIZATION:
            return "102";
        }
        return null;
    }

    private static String getCodeForGender(Gender gender) {
        switch (gender) {
        case MALE:
            return "M";
        case FEMALE:
            return "F";
        default:
            return "X";
        }
    }

    private static String getCodeForMaritalStatus(org.fenixedu.academic.domain.person.MaritalStatus maritalStatus) {
        switch (maritalStatus) {
        case SINGLE:
            return "001";
        case MARRIED:
            return "009";
        case DIVORCED:
            return "007";
        case WIDOWER:
            return "008";
        case SEPARATED:
            return "006";
        case CIVIL_UNION:
            return "005";
        case UNKNOWN:
            return "099";
        default:
            return "099";
        }
    }

    private static Student createStudent(Registration registration) {
        Student student = new Student();
        student.setSchoolCode(getInstitutionCode());
        student.setCourse(registration.getDegreeName());
        student.setStudentNumber(registration.getStudent().getNumber());
        student.setAcademicYear(registration.getCurricularYear());
        student.setAcademicDegreeCode(objectFactory.createStudentAcademicDegreeCode(getCodeForDegreeType(
                registration.getDegree().getDegreeType()).toString()));

        return student;
    }

    private static Integer getCodeForDegreeType(DegreeType degreeType) {

        Integer BASIC_STUDIES = 1;
        Integer SECUNDARY = 2;
        Integer BACHELHOR = 3;
        Integer DEGREE = 4;
        Integer MASTERS = 5;
        Integer PHD = 6;
        Integer NO_STUDIES = 7;

        String code = degreeType.getCode();

        // Deveria ser feito alinhamento programatico
        if ("BACHELOR".equals(code)) {
            return BACHELHOR;
        } else if ("DEGREE".equals(code)) {
            return DEGREE;
        } else if ("MASTER_DEGREE".equals(code)) {
            return MASTERS;
        } else if ("PHD".equals(code)) {
            return PHD;
        } else if ("SPECIALIZATION_DEGREE".equals(code)) {
            return MASTERS;
        } else if ("BOLONHA_DEGREE".equals(code)) {
            return DEGREE;
        } else if ("BOLONHA_MASTER_DEGREE".equals(code)) {
            return MASTERS;
        } else if ("BOLONHA_INTEGRATED_MASTER_DEGREE".equals(code)) {
            return MASTERS;
        } else if ("BOLONHA_PHD".equals(code)) {
            return PHD;
        } else if ("BOLONHA_ADVANCED_FORMATION_DIPLOMA".equals(code)) {
            return MASTERS;
        } else if ("BOLONHA_ADVANCED_SPECIALIZATION_DIPLOMA".equals(code)) {
            return MASTERS;
        } else if ("BOLONHA_SPECIALIZATION_DEGREE".equals(code)) {
            return MASTERS;
        } else if ("FREE_DEGREE".equals(code)) {
            return DEGREE;
        } else if ("BOLONHA_POST_DOCTORAL_DEGREE".equals(code)) {
            return PHD;
        } else if ("EMPTY".equals(code)) {
            return NO_STUDIES;
        }

        return DEGREE;
    }

    private static String findIES(String ministryCode, IIESService service) {
        List<School> schools = service.getSchools().getSchool();
        for (School school : schools) {
            if (ministryCode.equals(school.getCode().getValue())) {
                return school.getPartnerCode().getValue();
            }
        }
        return null;
    }
}
