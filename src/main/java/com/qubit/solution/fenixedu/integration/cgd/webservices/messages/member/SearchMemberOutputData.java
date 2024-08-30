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
package com.qubit.solution.fenixedu.integration.cgd.webservices.messages.member;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.TeacherCategory;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academictreasury.domain.customer.PersonCustomer;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.util.FiscalCodeValidation;
import org.fenixedu.treasury.util.TreasuryConstants;

import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.CgdMessageUtils;
import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.ISummaryMessage;
import com.qubit.solution.fenixedu.integration.cgd.webservices.resolver.memberid.IMemberIDAdapter;

public class SearchMemberOutputData implements Serializable, ISummaryMessage {

    // Single character to identify type of member
    // A - student
    // F - employee
    // D - teacher
    private String populationCode;

    // Member identification it will then be used
    // as the ID requested on other webServices
    private String memberID;

    // Full name
    private String name;

    // BEGIN: FOR STUDENTS ONLY
    private String studentNumber;
    private String degreeCode;
    private String degreeName;
    private String degreeType;
    private Integer degreeDuration;
    private Integer curricularYear;
    // END: FOR STUDENTS ONLY

    // BEGIN: TEACHERS ONLY
    private String teacherCategory;
    private String teacherNumber;
    // END: TEACHERS ONLY

    private String establishmentCode;
    private String establishmentName;
    private String stayingIndicator;

    private long fiscalCode;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getDegreeCode() {
        return degreeCode;
    }

    public void setDegreeCode(String degreeCode) {
        this.degreeCode = degreeCode;
    }

    public String getDegreeName() {
        return degreeName;
    }

    public void setDegreeName(String degreeName) {
        this.degreeName = degreeName;
    }

    public String getDegreeType() {
        return degreeType;
    }

    public void setDegreeType(String degreeType) {
        this.degreeType = degreeType;
    }

    public Integer getDegreeDuration() {
        return degreeDuration;
    }

    public void setDegreeDuration(Integer degreeDuration) {
        this.degreeDuration = degreeDuration;
    }

    public Integer getCurricularYear() {
        return curricularYear;
    }

    public void setCurricularYear(Integer curricularYear) {
        this.curricularYear = curricularYear;
    }

    public String getTeacherCategory() {
        return teacherCategory;
    }

    public void setTeacherCategory(String teacherCategory) {
        this.teacherCategory = teacherCategory;
    }

    public String getTeacherNumber() {
        return teacherNumber;
    }

    public void setTeacherNumber(String teacherNumber) {
        this.teacherNumber = teacherNumber;
    }

    public String getEstablishmentCode() {
        return establishmentCode;
    }

    public void setEstablishmentCode(String establishmentCode) {
        this.establishmentCode = establishmentCode;
    }

    public String getEstablishmentName() {
        return establishmentName;
    }

    public void setEstablishmentName(String establishmentName) {
        this.establishmentName = establishmentName;
    }

    public String getStayingIndicator() {
        return stayingIndicator;
    }

    public void setStayingIndicator(String stayingIndicator) {
        this.stayingIndicator = stayingIndicator;
    }

    public long getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(long fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public static SearchMemberOutputData createDefault(IMemberIDAdapter strategy, Person person) {

        SearchMemberOutputData searchMemberOutputData = new SearchMemberOutputData();
        searchMemberOutputData.setMemberID(strategy.retrieveMemberID(person));
        searchMemberOutputData.setName(person.getName());
        String socialSecurityNumber = PersonCustomer.fiscalNumber(person);
        String fiscalCountry = PersonCustomer.addressCountryCode(person);
        if (socialSecurityNumber != null && TreasuryConstants.isDefaultCountry(fiscalCountry)
                && FiscalCodeValidation.isValidFiscalNumber(fiscalCountry, socialSecurityNumber)) {
            searchMemberOutputData.setFiscalCode(Long.valueOf(socialSecurityNumber));
        } else {
            searchMemberOutputData.setFiscalCode(0L);
        }

        Unit institutionUnit = Bennu.getInstance().getInstitutionUnit();

        searchMemberOutputData.setEstablishmentName(institutionUnit.getName());
        searchMemberOutputData.setEstablishmentCode(institutionUnit.getCode());

        Set<ExecutionInterval> executionIntervals = ExecutionYear.findCurrents().stream()
                .flatMap(e -> Stream.of(e, e.getPrevious())).filter(Objects::nonNull).filter(ExecutionYear.class::isInstance)
                .map(ExecutionYear.class::cast).flatMap(e -> e.getChildIntervals().stream()).collect(Collectors.toSet());

        String stayingIndicator = person.getStudent() != null && person.getStudent().hasActiveRegistrations()
                || person.getTeacher() != null && person.getTeacher().getTeacherAuthorizationStream()
                        .anyMatch(authorization -> executionIntervals.contains(authorization.getExecutionInterval())) ? "S" : "N";
        searchMemberOutputData.setStayingIndicator(stayingIndicator);
        return searchMemberOutputData;
    }

    public static SearchMemberOutputData createStudentBased(IMemberIDAdapter strategy, Registration registration) {
        Student student = registration.getStudent();
        Person person = registration.getPerson();

        SearchMemberOutputData searchMemberOutputData = createDefault(strategy, person);
        searchMemberOutputData.setPopulationCode("A");
        searchMemberOutputData.setStudentNumber(String.valueOf(student.getNumber()));

        searchMemberOutputData.setStudentNumber(String.valueOf(student.getNumber()));
        Degree degree = registration.getDegree();
        searchMemberOutputData.setDegreeCode(degree.getMinistryCode());
        searchMemberOutputData.setDegreeName(degree.getIdCardName());
        searchMemberOutputData.setCurricularYear(registration.getCurricularYear());
        List<DegreeCurricularPlan> degreeCurricularPlansForYear =
                degree.getDegreeCurricularPlansForYear(ExecutionYear.findCurrent(degree.getCalendar()));
        if (!degreeCurricularPlansForYear.isEmpty()) {
            searchMemberOutputData.setDegreeDuration(degreeCurricularPlansForYear.iterator().next().getDurationInYears());
        }
        searchMemberOutputData.setDegreeType(degree.getDegreeType().getName().getContent());

        return searchMemberOutputData;
    }

    public static SearchMemberOutputData createTeacherBased(IMemberIDAdapter strategy, Teacher teacher) {
        Person person = teacher.getPerson();
        SearchMemberOutputData searchMemberOutputData = createDefault(strategy, person);
        searchMemberOutputData.setPopulationCode("D");

        TeacherCategory category = teacher.getCategory();
        if (category != null) {
            String content = category.getName().getContent();
            if (content.length() > 23) {
                content = content.substring(0, 23);
            }
            searchMemberOutputData.setTeacherCategory(content);
        }
        searchMemberOutputData.setTeacherNumber(teacher.getTeacherId());

        return searchMemberOutputData;
    }

    public static SearchMemberOutputData createEmployeeBased(IMemberIDAdapter strategy, Person person) {
        SearchMemberOutputData searchMemberOutputData = createDefault(strategy, person);
        searchMemberOutputData.setPopulationCode("F");

        searchMemberOutputData.setTeacherCategory("");
        searchMemberOutputData.setTeacherNumber(person.getUsername());

        return searchMemberOutputData;
    }

    @Override
    public String getSummaryMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(populationCode != null ? populationCode : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
        sb.append(memberID != null ? memberID : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
//        sb.append(name != null ? name : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
//        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
        sb.append(studentNumber != null ? studentNumber : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
        sb.append(degreeCode != null ? degreeCode : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
//        sb.append(degreeName != null ? degreeName : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
//        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
//        sb.append(degreeType != null ? degreeType : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
//        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
//        sb.append(degreeDuration != null ? degreeDuration : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
//        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
//        sb.append(curricularYear != null ? curricularYear : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
//        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
//        sb.append(teacherCategory != null ? teacherCategory : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
//        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
        sb.append(teacherNumber != null ? teacherNumber : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
//        sb.append(establishmentCode != null ? establishmentCode : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
//        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
//        sb.append(establishmentName != null ? establishmentName : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
//        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
        sb.append(stayingIndicator != null ? stayingIndicator : CgdMessageUtils.SUMMARY_FIELD_COLUMN_NULL);
        sb.append(CgdMessageUtils.SUMMARY_FIELD_COLUMN_SEPARATOR);
//        sb.append(fiscalCode);
        return sb.toString();
    }

}
