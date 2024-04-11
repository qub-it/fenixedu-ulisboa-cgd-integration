package com.qubit.solution.fenixedu.integration.cgd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Country;
import org.fenixedu.academic.domain.CurricularYear;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.administrativeOffice.AdministrativeOffice;
import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.academic.domain.contacts.PartyContact;
import org.fenixedu.academic.domain.contacts.PartyContactType;
import org.fenixedu.academic.domain.contacts.PhysicalAddress;
import org.fenixedu.academic.domain.contacts.PhysicalAddressData;
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
import org.fenixedu.academic.domain.student.RegistrationDataByExecutionYear;
import org.fenixedu.academic.domain.student.RegistrationProtocol;
import org.fenixedu.academic.domain.student.RegistrationRegimeType;
import org.fenixedu.academic.domain.student.StatuteType;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationStateType;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationStateTypeEnum;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicCalendarRootEntry;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicIntervalCE;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicPeriod;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicYearCE;
import org.fenixedu.academic.dto.person.PersonBean;
import org.fenixedu.academic.util.LocaleUtils;
import org.fenixedu.academic.util.PeriodState;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.academictreasury.domain.event.AcademicTreasuryEvent;
import org.fenixedu.academictreasury.services.AcademicTreasuryPlataformDependentServicesFactory;
import org.fenixedu.academictreasury.services.IAcademicTreasuryPlatformDependentServices;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.collect.Sets;
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
            initRootCalendarAndExecutionYears();
            createUnitsAndPartyTypes();
            registerImplementationForAcademicTreasuryPlataformDependentServicesFactory();
            CgdIntegrationConfiguration instance = CgdIntegrationConfiguration.getInstance();
            instance.setMemberIDResolverClass(StudentNumberAdapter.class.getName());
            IdDocumentTypeObject idDocumentTypeObject = IdDocumentTypeObject.create(IDDocumentType.IDENTITY_CARD);
            IdDocumentTypeObject.create(IDDocumentType.CITIZEN_CARD);
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
        Student student = Student.createStudentWithCustomNumber(person, 0);
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
                new LocalizedString().with(LocaleUtils.EN, "Registration State Type"), false,
                RegistrationStateTypeEnum.REGISTERED);
        Registration registration = Registration.create(student, dcp, executionYear, registrationProtocol, ingressionType);
        return registration;
    }

    private void registerImplementationForAcademicTreasuryPlataformDependentServicesFactory() {
        AcademicTreasuryPlataformDependentServicesFactory
                .registerImplementation(new IAcademicTreasuryPlatformDependentServices() {

                    @Override
                    public Set<DegreeType> readAllDegreeTypes() {
                        return DegreeType.all().collect(Collectors.toSet());
                    }

                    @Override
                    public Set<DegreeCurricularPlan> readAllDegreeCurricularPlansSet() {
                        return DegreeCurricularPlan.readBolonhaDegreeCurricularPlans();
                    }

                    @Override
                    public Set<DegreeCurricularPlan> readDegreeCurricularPlansWithExecutionDegree(ExecutionYear executionYear,
                            DegreeType degreeType) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Set<CurricularYear> readAllCurricularYearsSet() {
                        return Collections.emptySet();
                    }

                    @Override
                    public Set<IngressionType> readAllIngressionTypesSet() {
                        return Collections.emptySet();
                    }

                    @Override
                    public Set<RegistrationProtocol> readAllRegistrationProtocol() {
                        return Collections.emptySet();
                    }

                    @Override
                    public Set<StatuteType> readAllStatuteTypesSet() {
                        return Collections.emptySet();
                    }

                    @Override
                    public Set<StatuteType> readAllStatuteTypesSet(boolean active) {
                        return Collections.emptySet();
                    }

                    @Override
                    public Set<Registration> readAllRegistrations(RegistrationProtocol registrationProtocol) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Set<Registration> readAllRegistrations(IngressionType ingressionType) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Set<Person> readAllPersonsSet() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public PersonCustomer personCustomer(Person person) {
                        return person.getPersonCustomer();
                    }

                    @Override
                    public Set<PersonCustomer> inactivePersonCustomers(Person person) {
                        return person.getInactivePersonCustomersSet();
                    }

                    @Override
                    public PhysicalAddress fiscalAddress(Person person) {
                        return person.getFiscalAddress();
                    }

                    @Override
                    public String iban(Person person) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Set<AcademicTreasuryEvent> academicTreasuryEventsSet(Person person) {
                        return person.getAcademicTreasuryEventSet();
                    }

                    @Override
                    public String defaultPhoneNumber(Person person) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String defaultMobilePhoneNumber(Person person) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public List<PhysicalAddress> pendingOrValidPhysicalAddresses(Person person) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public List<? extends PartyContact> pendingOrValidPartyContacts(Person person,
                            Class<? extends PartyContact> partyContactType) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void editSocialSecurityNumber(Person person, String fiscalNumber, PhysicalAddress fiscalAddress) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void setFiscalAddress(PhysicalAddress physicalAddress, boolean fiscalAddress) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public PhysicalAddress createPhysicalAddress(Person person, Country countryOfResidence,
                            String districtOfResidence, String districtSubdivisionOfResidence, String areaCode, String address) {
                        PhysicalAddressData data = new PhysicalAddressData();

                        data.setAddress(address);
                        data.setCountryOfResidence(countryOfResidence);
                        data.setDistrictOfResidence(districtOfResidence);
                        data.setDistrictSubdivisionOfResidence(districtSubdivisionOfResidence);
                        data.setAreaCode(areaCode);

                        final PhysicalAddress physicalAddress =
                                PhysicalAddress.createPhysicalAddress(person, data, PartyContactType.PERSONAL, false);

                        physicalAddress.setValid();

                        return physicalAddress;
                    }

                    @Override
                    public String fiscalCountry(Person person) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String fiscalNumber(Person person) {
                        return person.getSocialSecurityNumber();
                    }

                    @Override
                    public boolean isFrontOfficeMember(String username, FinantialEntity finantialEntity) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean isBackOfficeMember(String username, FinantialEntity finantialEntity) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean isAllowToModifySettlements(String username, FinantialEntity finantialEntity) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean isAllowToModifyInvoices(String username, FinantialEntity finantialEntity) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public Set<Degree> readDegrees(FinantialEntity finantialEntity) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public FinantialEntity finantialEntityOfDegree(Degree degree, LocalDate when) {
                        return FinantialEntity.findAll().iterator().next();
                    }

                    @Override
                    public Optional<FinantialEntity> finantialEntity(AdministrativeOffice administrativeOffice) {
                        // TODO Auto-generated method stub
                        return Optional.empty();
                    }

                    @Override
                    public Optional<FinantialEntity> finantialEntity(Unit unit) {
                        return FinantialEntity.findAll().findFirst();
                    }

                    @Override
                    public Set<String> getFrontOfficeMemberUsernames(FinantialEntity finantialEntity) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Set<String> getBackOfficeMemberUsernames(FinantialEntity finantialEntity) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String localizedNameOfDegreeType(DegreeType degreeType) {
                        return degreeType.getName().getContent();
                    }

                    @Override
                    public String localizedNameOfDegreeType(DegreeType degreeType, Locale locale) {
                        return degreeType.getName().getContent(locale);
                    }

                    @Override
                    public String localizedNameOfStatuteType(StatuteType statuteType) {
                        return statuteType.getName().getContent();
                    }

                    @Override
                    public String localizedNameOfStatuteType(StatuteType statuteType, Locale locale) {
                        return statuteType.getName().getContent(locale);
                    }

                    @Override
                    public String localizedNameOfEnrolment(Enrolment enrolment) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String localizedNameOfEnrolment(Enrolment enrolment, Locale locale) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String localizedNameOfAdministrativeOffice(AdministrativeOffice administrativeOffice) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String localizedNameOfAdministrativeOffice(AdministrativeOffice administrativeOffice, Locale locale) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public RegistrationDataByExecutionYear findRegistrationDataByExecutionYear(Registration registration,
                            ExecutionYear executionYear) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public IngressionType ingression(Registration registration) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public RegistrationProtocol registrationProtocol(Registration registration) {
                        return registration.getRegistrationProtocol();
                    }

                    @Override
                    public RegistrationRegimeType registrationRegimeType(Registration registration, ExecutionYear executionYear) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Set<StatuteType> statutesTypesValidOnAnyExecutionSemesterFor(Registration registration,
                            ExecutionInterval executionInterval) {
                        return Sets.newHashSet(findStatuteTypes(registration, executionInterval));
                    }

                    public Collection<StatuteType> findStatuteTypes(final Registration registration,
                            final ExecutionInterval executionInterval) {

                        if (executionInterval instanceof ExecutionYear) {
                            return findStatuteTypesByYear(registration, (ExecutionYear) executionInterval);
                        }

                        return findStatuteTypesByChildInterval(registration, executionInterval);
                    }

                    private Collection<StatuteType> findStatuteTypesByYear(final Registration registration,
                            final ExecutionYear executionYear) {

                        final Set<StatuteType> result = Sets.newHashSet();
                        for (final ExecutionInterval executionInterval : executionYear.getExecutionPeriodsSet()) {
                            result.addAll(findStatuteTypesByChildInterval(registration, executionInterval));
                        }

                        return result;
                    }

                    private Collection<StatuteType> findStatuteTypesByChildInterval(final Registration registration,
                            final ExecutionInterval executionInterval) {

                        return registration.getStudent().getStudentStatutesSet().stream()
                                .filter(s -> s.isValidInExecutionInterval(executionInterval)
                                        && (s.getRegistration() == null || s.getRegistration() == registration))
                                .map(s -> s.getType()).collect(Collectors.toSet());
                    }

                    public String getVisibleStatuteTypesDescription(final Registration registration,
                            final ExecutionInterval executionInterval) {
                        return findVisibleStatuteTypes(registration, executionInterval).stream()
                                .map(s -> s.getName().getContent()).distinct().collect(Collectors.joining(", "));
                    }

                    public Collection<StatuteType> findVisibleStatuteTypes(final Registration registration,
                            final ExecutionInterval executionInterval) {
                        return findStatuteTypes(registration, executionInterval).stream().filter(s -> s.getVisible())
                                .collect(Collectors.toSet());
                    }

                    @Override
                    public Stream<AdministrativeOffice> findAdministrativeOfficesByPredicate(
                            Predicate<AdministrativeOffice> predicate) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public ExecutionInterval executionSemester(Enrolment enrolment) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public ExecutionInterval executionSemester(EnrolmentEvaluation enrolmentEvaluation) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public ExecutionYear executionYearOfExecutionSemester(ExecutionInterval executionInterval) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Integer executionIntervalChildOrder(ExecutionInterval executionInterval) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public ExecutionInterval getExecutionIntervalByName(String s) {
                        // TODO Auto-generated method stub
                        return null;
                    }
                });
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

    private void initRootCalendarAndExecutionYears() {
        if (Bennu.getInstance().getDefaultAcademicCalendar() != null) {// if initialization was already executed
            return;
        }

        AcademicCalendarRootEntry rootEntry =
                new AcademicCalendarRootEntry(new LocalizedString().with(Locale.getDefault(), "Root entry"), null);
        Bennu.getInstance().setDefaultAcademicCalendar(rootEntry);

        final int year = 2020;

        AcademicYearCE academicYearEntryFirst = createStandardYearInterval(rootEntry, year - 1);
        AcademicYearCE academicYearEntrySecond = createStandardYearInterval(rootEntry, year);
        AcademicYearCE academicYearEntryThird = createStandardYearInterval(rootEntry, year + 1);
        AcademicYearCE academicYearEntryFourth = createStandardYearInterval(rootEntry, year + 2);

        academicYearEntrySecond.getExecutionInterval().setState(PeriodState.CURRENT);

        createFirstSemesterInterval(academicYearEntryFirst);
        createSecondSemesterInterval(academicYearEntryFirst);

        createFirstSemesterInterval(academicYearEntrySecond).getExecutionInterval().setState(PeriodState.CURRENT);
        createSecondSemesterInterval(academicYearEntrySecond);

        createFirstSemesterInterval(academicYearEntryThird);
        createSecondSemesterInterval(academicYearEntryThird);

        createFirstSemesterInterval(academicYearEntryFourth);
        createSecondSemesterInterval(academicYearEntryFourth);

        AcademicCalendarRootEntry civilCalendar =
                new AcademicCalendarRootEntry(new LocalizedString().with(Locale.getDefault(), "Civil Calendar"), null);

        createCivilYearIntervalAndMonths(civilCalendar, year - 1);
        createCivilYearIntervalAndMonths(civilCalendar, year);
        createCivilYearIntervalAndMonths(civilCalendar, year + 1);
        createCivilYearIntervalAndMonths(civilCalendar, year + 2);

    }

    private static AcademicYearCE createStandardYearInterval(final AcademicCalendarRootEntry calendar, final int year) {
        return createYearInterval(calendar, year + "/" + (year + 1), new LocalDate(year, 9, 1), new LocalDate(year + 1, 8, 30));
    }

    private static AcademicYearCE createYearInterval(AcademicCalendarRootEntry calendar, String name, LocalDate startDate,
            LocalDate endDate) {
        return new AcademicYearCE(calendar, new LocalizedString().with(Locale.getDefault(), name), null,
                startDate.toDateTimeAtStartOfDay(), endDate.toDateTimeAtStartOfDay(), calendar);
    }

    private static AcademicIntervalCE createFirstSemesterInterval(AcademicYearCE academicYearEntry) {
        final int year = academicYearEntry.getBegin().getYear();
        final AcademicIntervalCE firstSemesterEntry = new AcademicIntervalCE(AcademicPeriod.SEMESTER, academicYearEntry,
                new LocalizedString().with(Locale.getDefault(), "1st Semester"), null, new DateTime(year, 9, 1, 0, 0, 0),
                new DateTime(year + 1, 1, 31, 23, 59, 59), academicYearEntry.getRootEntry());

        firstSemesterEntry.getExecutionInterval().setState(PeriodState.OPEN);
        return firstSemesterEntry;
    }

    private static AcademicIntervalCE createSecondSemesterInterval(AcademicYearCE calendar) {
        final int year = calendar.getBegin().getYear();
        final AcademicIntervalCE secondSemesterEntry = new AcademicIntervalCE(AcademicPeriod.SEMESTER, calendar,
                new LocalizedString().with(Locale.getDefault(), "2nd Semester"), null, new DateTime(year + 1, 2, 1, 0, 0, 0),
                new DateTime(year + 1, 8, 31, 23, 59, 59), calendar.getRootEntry());
        secondSemesterEntry.getExecutionInterval().setState(PeriodState.OPEN);
        return secondSemesterEntry;
    }

    private static AcademicYearCE createCivilYearIntervalAndMonths(AcademicCalendarRootEntry calendar, final int year) {
        final AcademicYearCE yearEntry = createCivilYearInterval(calendar, year);
        for (int i = 1; i <= 12; i++) {
            createMonthInterval(yearEntry, i);
        }

        return yearEntry;
    }

    private static AcademicYearCE createCivilYearInterval(AcademicCalendarRootEntry calendar, final int year) {
        return createYearInterval(calendar, String.valueOf(year), new LocalDate(year, 1, 1), new LocalDate(year, 12, 31));
    }

    private static AcademicIntervalCE createMonthInterval(AcademicYearCE academicYearEntry, int month) {
        final int year = academicYearEntry.getBegin().getYear();
        final AcademicIntervalCE monthEntry = new AcademicIntervalCE(AcademicPeriod.MONTH, academicYearEntry,
                new LocalizedString().with(Locale.getDefault(), "Month " + month), null, new DateTime(year, month, 1, 0, 0, 0),
                new DateTime(year, month, YearMonth.of(year, month).lengthOfMonth(), 0, 0, 0), academicYearEntry.getRootEntry());
        monthEntry.getExecutionInterval().setState(PeriodState.OPEN);
        return monthEntry;
    }
}
