package com.qubit.solution.fenixedu.integration.cgd;

import static org.fenixedu.academic.domain.person.identificationDocument.IdentificationDocumentTypeTest.createIdentificationDocumentTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionIntervalTest;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.academic.domain.curriculum.grade.GradeScale;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.degreeStructure.CurricularStage;
import org.fenixedu.academic.domain.organizationalStructure.AccountabilityType;
import org.fenixedu.academic.domain.organizationalStructure.PartyType;
import org.fenixedu.academic.domain.organizationalStructure.PartyTypeEnum;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.person.IdDocument;
import org.fenixedu.academic.domain.person.IdDocumentTypeObject;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationProtocol;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationStateType;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicPeriod;
import org.fenixedu.academic.dto.person.PersonBean;
import org.fenixedu.academic.util.LocaleUtils;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdIntegrationConfiguration;
import com.qubit.solution.fenixedu.integration.cgd.domain.logs.CgdCommunicationLog;
import com.qubit.solution.fenixedu.integration.cgd.services.memberid.StudentNumberAdapter;
import com.qubit.solution.fenixedu.integration.cgd.webservices.CgdIntegrationService;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.member.SearchMemberInput;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.member.SearchMemberOutput;

import pt.ist.fenixframework.FenixFramework;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CgdCommunicationLogTest {

    private static final int DOCUMENT_TYPE = 101;

    private static final String DOCUMENT_NUMBER = "37498786";//Randomly generated document number

    private static CgdIntegrationService service = new CgdIntegrationService();

    private static Person person;

    public void setupTest() {
        FenixFramework.getTransactionManager().withTransaction(() -> {
            Bennu.getInstance();
            ExecutionIntervalTest.initRootCalendarAndExecutionYears();
            createUnitsAndPartyTypes();
            CgdIntegrationConfiguration instance = CgdIntegrationConfiguration.getInstance();
            instance.setMemberIDResolverClass(StudentNumberAdapter.class.getName());
            IdDocumentTypeObject idDocumentTypeObject = IdDocumentTypeObject.create(IDDocumentType.IDENTITY_CARD);
            IdDocumentTypeObject.create(IDDocumentType.CITIZEN_CARD);
            createIdentificationDocumentTypes();
            PersonBean personBean = new PersonBean();
            personBean.setUsername("usernametest");
            personBean.setName("User for Test");
            personBean.setIdDocumentType(IDDocumentType.IDENTITY_CARD);
            personBean.setDocumentIdNumber(DOCUMENT_NUMBER);
            person = new Person(personBean);
            new IdDocument(person, DOCUMENT_NUMBER, idDocumentTypeObject);
            return null;
        });
    }

    @Test
    public void a_searchNonExistentMember() {
        setupTest();
        SearchMemberInput input = new SearchMemberInput();
        SearchMemberOutput searchMemberOutput = service.searchMember(input);
        assertEquals(0, searchMemberOutput.getMemberInfo().size());
    }

    @Test
    public void b_searchExitentMemberWithoutLogs() {
        FenixFramework.getTransactionManager().withTransaction(() -> {
            SearchMemberInput input = createSearchMemberInput();
            SearchMemberOutput searchMemberOutput = service.searchMember(input);
            assertTrue(searchMemberOutput.getMemberInfo().size() > 0);
            return null;
        });
    }

    @Test
    public void c_searchExitentMemberWithLogs() {
        FenixFramework.getTransactionManager().withTransaction(() -> {
            Registration registration = createTestRegistration();
            CgdCommunicationLog log = CgdCommunicationLog.createCgdCommunicationLog(registration, true, true, person, "", "", "");
            DateTime searchDateBeforeSearchMember = log.getSearchDate();
            SearchMemberInput input = new SearchMemberInput();
            input.setDocumentID(DOCUMENT_NUMBER);
            input.setDocumentType(DOCUMENT_TYPE);
            input.setMemberCode(DOCUMENT_NUMBER);
            service.searchMember(input);
            Optional<CgdCommunicationLog> findLatestStudentLog = CgdCommunicationLog.findLatestStudentLog(person);
            assertTrue(searchDateBeforeSearchMember == null);
            assertTrue(findLatestStudentLog.isPresent());
            assertTrue(findLatestStudentLog.get().getSearchDate() != null);
            return null;
        });
    }

    private SearchMemberInput createSearchMemberInput() {
        SearchMemberInput input = new SearchMemberInput();
        input.setDocumentID(DOCUMENT_NUMBER);
        input.setDocumentType(DOCUMENT_TYPE);
        input.setMemberCode(DOCUMENT_NUMBER);
        return input;
    }

    private Registration createTestRegistration() {
        Student student = new Student(person);
        ExecutionYear executionYear = ExecutionYear.findCurrents().iterator().next();
        Degree degree =
                new Degree("Degree", "Degree", "D", new DegreeType(new LocalizedString().with(LocaleUtils.EN, "Degree Type")),
                        GradeScale.create("NUMERIC_SCALE", new LocalizedString().with(LocaleUtils.EN, "Scale A"), BigDecimal.ZERO,
                                BigDecimal.ONE, BigDecimal.TEN, new BigDecimal(20), true, true),
                        GradeScale.create("SCALE", new LocalizedString().with(LocaleUtils.EN, "Scale B"), BigDecimal.ZERO,
                                BigDecimal.ONE, BigDecimal.TEN, new BigDecimal(20), true, true),
                        executionYear);
        final UserProfile userProfile =
                new UserProfile("Fenix", "Admin", "Fenix Admin", "fenix.admin@fenixedu.com", Locale.getDefault());
        new User("admin", userProfile);
        DegreeCurricularPlan dcp =
                degree.createDegreeCurricularPlan("Test Degree", new Person(userProfile), AcademicPeriod.FIVE_YEAR);
        dcp.setCurricularStage(CurricularStage.APPROVED);
        dcp.createExecutionDegree(executionYear);
        RegistrationProtocol registrationProtocol = RegistrationProtocol.create("REGISTRATION_PROTOCOL_CODE",
                new LocalizedString().with(LocaleUtils.EN, "Registration Protocol"));
        IngressionType ingressionType = IngressionType.createIngressionType("INGRESSION_TYPE",
                new LocalizedString().with(LocaleUtils.EN, "Ingression Type"));
        RegistrationStateType.create(RegistrationStateType.REGISTERED_CODE,
                new LocalizedString().with(LocaleUtils.EN, "Registration State Type"), false);
        Registration registration = Registration.create(student, dcp, executionYear, registrationProtocol, ingressionType);
        return registration;
    }

    private void createUnitsAndPartyTypes() {
        PartyType partyType = PartyType.create("P", new LocalizedString(LocaleUtils.PT, "Planeta"));
        partyType.setType(PartyTypeEnum.PLANET);
        Unit planet = Unit.createNewUnit(Optional.of(partyType), new LocalizedString().with(LocaleUtils.PT, "Terra"), "PLANET",
                null, null);
        AccountabilityType accountabilityType =
                AccountabilityType.create("Test accountability", new LocalizedString(LocaleUtils.PT, "Test Accountability"));
        Unit institutionUnit = Unit.createNewUnit(Optional.empty(), new LocalizedString().with(LocaleUtils.PT, "Instituição"),
                "ACRONYM_TEST", planet, accountabilityType);
        Bennu.getInstance().setInstitutionUnit(institutionUnit);
    }

}
