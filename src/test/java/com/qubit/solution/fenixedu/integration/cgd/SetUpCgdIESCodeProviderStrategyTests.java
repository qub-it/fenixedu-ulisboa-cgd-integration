package com.qubit.solution.fenixedu.integration.cgd;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.CompetenceCourseType;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.academic.domain.curricularPeriod.CurricularPeriod;
import org.fenixedu.academic.domain.curriculum.grade.GradeScale;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseInformation;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseLevelType;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseLoad;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.academic.domain.degreeStructure.CourseLoadType;
import org.fenixedu.academic.domain.degreeStructure.CurricularStage;
import org.fenixedu.academic.domain.organizationalStructure.AccountabilityType;
import org.fenixedu.academic.domain.organizationalStructure.AccountabilityTypeEnum;
import org.fenixedu.academic.domain.organizationalStructure.PartyType;
import org.fenixedu.academic.domain.organizationalStructure.PartyTypeEnum;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationProtocol;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationStateType;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicCalendarRootEntry;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicIntervalCE;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicPeriod;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicYearCE;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.academic.util.PeriodState;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class SetUpCgdIESCodeProviderStrategyTests {
    private static final String INGRESSION_CODE = "I";
    private static final String PROTOCOL_CODE = "P";

    public static final String COURSE_A_CODE = "CA";
    public static final String COURSE_B_CODE = "CB";

    public static final String TEST_STUDENT_USERNAME = "test_student";
    private static final String DEGREE_CODE = "degreeTest";

    private static CompetenceCourse competenceCourseA;
    private static CompetenceCourse competenceCourseB;

    private static Student student;
    private static Registration registration;
    private static AcademicCalendarRootEntry rootEntry;

    static void generateDataForCgdTests() {
        createTestExecutionYear();
        createTestDegree();
        createTestDegreeCurricularPlan();
        createTestExecutions();
        createTestStudent();
    }

    private static void createTestExecutionYear() {
        if (rootEntry != null) { // if initialization was already executed
            return;
        }
        rootEntry = new AcademicCalendarRootEntry(new LocalizedString().with(Locale.getDefault(), "Root entry"), null);
        Bennu.getInstance().setDefaultAcademicCalendar(rootEntry);

        final int year = new LocalDate().getYear();

        AcademicYearCE academicYearEntryFirst = createYearInterval(year - 1);
        AcademicYearCE academicYearEntrySecond = createYearInterval(year);
        AcademicYearCE academicYearEntryThird = createYearInterval(year + 1);

        academicYearEntrySecond.getExecutionInterval().setState(PeriodState.CURRENT);

        createFirstSemesterInterval(academicYearEntryFirst);
        createSecondSemesterInterval(academicYearEntryFirst);

        createFirstSemesterInterval(academicYearEntrySecond).getExecutionInterval().setState(PeriodState.CURRENT);
        createSecondSemesterInterval(academicYearEntrySecond);

        createFirstSemesterInterval(academicYearEntryThird);
        createSecondSemesterInterval(academicYearEntryThird);

    }

    private static void createTestDegree() {
        DegreeType degreeType = new DegreeType(new LocalizedString.Builder().with(Locale.getDefault(), "Degree").build());

        final ExecutionYear executionYear = ExecutionYear.findCurrent(null);

        final Degree degree = new Degree("Degree test", "Degree Test", DEGREE_CODE, degreeType, new GradeScale(),
                new GradeScale(), executionYear);
        degree.setCode(DEGREE_CODE);
        degree.setCalendar(executionYear.getAcademicInterval().getAcademicCalendar());

    }

    private static void createTestDegreeCurricularPlan() {
        Degree degree = Degree.find(DEGREE_CODE);

        final UserProfile userProfile =
                new UserProfile("Fenix", "Admin", "Fenix Admin", "fenix.admin@fenixedu.com", Locale.getDefault());
        new User("admin", userProfile);
        Person person = new Person(userProfile);
        DegreeCurricularPlan degreeCurricularPlan =
                degree.createDegreeCurricularPlan("Version 2022", person, AcademicPeriod.THREE_YEAR);
        degreeCurricularPlan.setCurricularStage(CurricularStage.APPROVED);

        createCompetenceCourseData();
        final CompetenceCourse competenceCourse = CompetenceCourse.find(COURSE_A_CODE);

        CurricularCourse curricularCourse = new CurricularCourse();
        curricularCourse.setCompetenceCourse(competenceCourse);

        final CurricularPeriod yearPeriod =
                new CurricularPeriod(AcademicPeriod.YEAR, 1, degreeCurricularPlan.getDegreeStructure());
        final CurricularPeriod semesterPeriod = new CurricularPeriod(AcademicPeriod.SEMESTER, 1, yearPeriod);

        final ExecutionInterval executionInterval = ExecutionYear.findCurrent(null).getFirstExecutionPeriod();
        new Context(degreeCurricularPlan.getRoot(), curricularCourse, semesterPeriod, executionInterval, null);

    }

    private static void createTestExecutions() {
        final ExecutionYear executionYear = ExecutionYear.findCurrent(null);
        final ExecutionInterval executionInterval = executionYear.getFirstExecutionPeriod();

        // DegreeCurricularPlanTest.initDegreeCurricularPlan();
        final Degree degree = Degree.find(DEGREE_CODE);
        final DegreeCurricularPlan degreeCurricularPlan = degree.getDegreeCurricularPlansSet().iterator().next();
        final CurricularCourse curricularCourse = degreeCurricularPlan.getCurricularCourseByCode(COURSE_A_CODE);

        ExecutionDegree executionDegree = degreeCurricularPlan.createExecutionDegree(executionYear);

        final CompetenceCourse competenceCourse = CompetenceCourse.find(COURSE_A_CODE);

        ExecutionCourse executionCourse =
                new ExecutionCourse(competenceCourse.getName(), competenceCourse.getCode(), executionInterval);
        executionCourse.addAssociatedCurricularCourses(curricularCourse);

    }

    private static void createCompetenceCourseData() {
        initTypesOrganizationalStructureTest();
        Unit planetUnit = initPlanetUnit();
        LocalizedString unitName = new LocalizedString.Builder().with(Locale.getDefault(), "Courses Unit").build();
        Unit coursesUnit = Unit.createNewUnit(PartyType.of(PartyTypeEnum.COMPETENCE_COURSE_GROUP), unitName, "CC", planetUnit,
                AccountabilityType.readByType(AccountabilityTypeEnum.ORGANIZATIONAL_STRUCTURE));

        initCourseLoadTypes();

        createCompetenceCourseASemester(coursesUnit);
        createCompetenceCourseBAnnual(coursesUnit);
    }

    private static void createTestStudent() {
        initConfigEntities();

        final UserProfile userProfile =
                new UserProfile("Test", "Student", "Test Student", "test.student@fenixedu.com", Locale.getDefault());
        new User(TEST_STUDENT_USERNAME, userProfile);
        final Person person = new Person(userProfile);
        student = new Student(person);

        Degree degree = Degree.find(DEGREE_CODE);
        DegreeCurricularPlan degreeCurricularPlan = degree.getDegreeCurricularPlansSet().iterator().next();

        registration = Registration.create(student, degreeCurricularPlan, ExecutionYear.findCurrent(null),
                RegistrationProtocol.findByCode(PROTOCOL_CODE),
                IngressionType.findIngressionTypeByCode(INGRESSION_CODE).orElseThrow());
    }

    private static void initConfigEntities() {
        RegistrationProtocol.create(PROTOCOL_CODE, new LocalizedString.Builder().with(Locale.getDefault(), "Protocol").build());

        IngressionType.createIngressionType(INGRESSION_CODE,
                new LocalizedString.Builder().with(Locale.getDefault(), "Ingression").build());
        RegistrationStateType.create("REGISTERED", new LocalizedString.Builder().with(Locale.getDefault(), "Registered").build(),
                true, null);
    }

    private static AcademicYearCE createYearInterval(final int year) {
        return new AcademicYearCE(rootEntry, new LocalizedString().with(Locale.getDefault(), year + "/" + (year + 1)), null,
                new LocalDate(year, 9, 1).toDateTimeAtStartOfDay(), new LocalDate(year + 1, 8, 30).toDateTimeAtStartOfDay(),
                rootEntry);
    }

    private static AcademicIntervalCE createFirstSemesterInterval(AcademicYearCE academicYearEntry) {
        final int year = academicYearEntry.getBegin().getYear();
        final AcademicIntervalCE firstSemesterEntry = new AcademicIntervalCE(AcademicPeriod.SEMESTER, academicYearEntry,
                new LocalizedString().with(Locale.getDefault(), "1st Semester"), null, new DateTime(year, 9, 1, 0, 0, 0),
                new DateTime(year + 1, 1, 31, 23, 59, 59), rootEntry);

        firstSemesterEntry.getExecutionInterval().setState(PeriodState.OPEN);
        return firstSemesterEntry;
    }

    private static AcademicIntervalCE createSecondSemesterInterval(AcademicYearCE academicYearEntry) {
        final int year = academicYearEntry.getBegin().getYear();
        final AcademicIntervalCE secondSemesterEntry = new AcademicIntervalCE(AcademicPeriod.SEMESTER, academicYearEntry,
                new LocalizedString().with(Locale.getDefault(), "2nd Semester"), null, new DateTime(year + 1, 2, 1, 0, 0, 0),
                new DateTime(year + 1, 8, 31, 23, 59, 59), rootEntry);
        secondSemesterEntry.getExecutionInterval().setState(PeriodState.OPEN);
        return secondSemesterEntry;
    }

    private static void initTypesOrganizationalStructureTest() {
        Stream.of(PartyTypeEnum.values()).forEach(partyTypeEnum -> new PartyType(partyTypeEnum));

        Stream.of(AccountabilityTypeEnum.values())
                .forEach(accountabilityTypeEnum -> new AccountabilityType(accountabilityTypeEnum,
                        new LocalizedString(Locale.getDefault(), accountabilityTypeEnum.getLocalizedName())));
    }

    private static Unit initPlanetUnit() {
        LocalizedString name = new LocalizedString.Builder().with(Locale.getDefault(), "Earth").build();
        final Unit planetUnit = Unit.createNewUnit(PartyType.of(PartyTypeEnum.PLANET), name, "E", null, null);
        final Bennu rootDomainObject = Bennu.getInstance();
        rootDomainObject.setEarthUnit(planetUnit);
        return planetUnit;

    }

    private static void createCompetenceCourseASemester(Unit coursesUnit) {
        competenceCourseA = createCompetenceCourse("Course A", COURSE_A_CODE, AcademicPeriod.SEMESTER, coursesUnit);

        final CompetenceCourseInformation courseInformation =
                competenceCourseA.getCompetenceCourseInformationsSet().iterator().next();

        new CompetenceCourseLoad(courseInformation, 30d, 0d, 10d, 0d, 0d, 0d, 0d, 0d, 20d, 6d, 1, AcademicPeriod.SEMESTER);

        final CompetenceCourseInformation nextCourseInformation = new CompetenceCourseInformation(courseInformation);
        final ExecutionYear nextExecutionYear = (ExecutionYear) ExecutionYear.findCurrentAggregator(null).getNext();
        nextCourseInformation.setExecutionInterval(nextExecutionYear.getFirstExecutionPeriod());

        final CompetenceCourseLoad nextLoad = nextCourseInformation.getCompetenceCourseLoadsSet().iterator().next();
        nextLoad.setTheoreticalHours(0d);
        nextLoad.setProblemsHours(15d);
    }

    private static void createCompetenceCourseBAnnual(Unit coursesUnit) {
        competenceCourseB = createCompetenceCourse("Course B", COURSE_B_CODE, AcademicPeriod.YEAR, coursesUnit);

        final CompetenceCourseInformation courseInformation =
                competenceCourseB.getCompetenceCourseInformationsSet().iterator().next();

        new CompetenceCourseLoad(courseInformation, 30d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 6d, 1, AcademicPeriod.SEMESTER);
        new CompetenceCourseLoad(courseInformation, 10d, 0d, 5d, 0d, 0d, 0d, 0d, 0d, 0d, 9d, 2, AcademicPeriod.SEMESTER);
    }

    private static CompetenceCourse createCompetenceCourse(final String name, final String code, final AcademicPeriod duration,
            Unit coursesUnit) {
        final CompetenceCourse result = new CompetenceCourse(name, name, Boolean.TRUE, duration,
                CompetenceCourseLevelType.UNKNOWN().orElse(null), CompetenceCourseType.REGULAR, CurricularStage.APPROVED,
                coursesUnit, ExecutionInterval.findFirstCurrentChild(null), new GradeScale());
        result.setCode(code);

        return result;
    }

    private static void initCourseLoadTypes() {
        if (CourseLoadType.findAll().findAny().isEmpty()) {
            Function<String, LocalizedString> nameProvider = type -> BundleUtil.getLocalizedString(Bundle.ENUMERATION,
                    CourseLoadType.class.getName() + "." + type + ".name");

            Function<String, LocalizedString> initialsProvider = type -> BundleUtil.getLocalizedString(Bundle.ENUMERATION,
                    CourseLoadType.class.getName() + "." + type + ".initials");

            CourseLoadType.create(CourseLoadType.THEORETICAL, nameProvider.apply(CourseLoadType.THEORETICAL),
                    initialsProvider.apply(CourseLoadType.THEORETICAL), true);
            CourseLoadType.create(CourseLoadType.THEORETICAL_PRACTICAL, nameProvider.apply(CourseLoadType.THEORETICAL_PRACTICAL),
                    initialsProvider.apply(CourseLoadType.THEORETICAL_PRACTICAL), true);
            CourseLoadType.create(CourseLoadType.PRACTICAL_LABORATORY, nameProvider.apply(CourseLoadType.PRACTICAL_LABORATORY),
                    initialsProvider.apply(CourseLoadType.PRACTICAL_LABORATORY), true);
            CourseLoadType.create(CourseLoadType.FIELD_WORK, nameProvider.apply(CourseLoadType.FIELD_WORK),
                    initialsProvider.apply(CourseLoadType.FIELD_WORK), true);
            CourseLoadType.create(CourseLoadType.SEMINAR, nameProvider.apply(CourseLoadType.SEMINAR),
                    initialsProvider.apply(CourseLoadType.SEMINAR), true);
            CourseLoadType.create(CourseLoadType.INTERNSHIP, nameProvider.apply(CourseLoadType.INTERNSHIP),
                    initialsProvider.apply(CourseLoadType.INTERNSHIP), true);
            CourseLoadType.create(CourseLoadType.TUTORIAL_ORIENTATION, nameProvider.apply(CourseLoadType.TUTORIAL_ORIENTATION),
                    initialsProvider.apply(CourseLoadType.TUTORIAL_ORIENTATION), true);
            CourseLoadType.create(CourseLoadType.OTHER, nameProvider.apply(CourseLoadType.OTHER),
                    initialsProvider.apply(CourseLoadType.OTHER), true);

            CourseLoadType.create(CourseLoadType.AUTONOMOUS_WORK, nameProvider.apply(CourseLoadType.AUTONOMOUS_WORK),
                    initialsProvider.apply(CourseLoadType.AUTONOMOUS_WORK), false);
        }
    }

}
