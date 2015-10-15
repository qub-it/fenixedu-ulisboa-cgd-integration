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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.TeacherCategory;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.ulisboa.specifications.ui.firstTimeCandidacy.util.FiscalCodeValidation;

import com.qubit.solution.fenixedu.integration.cgd.webservices.resolver.memberid.IMemberIDAdapter;

public class SearchMemberOutputData implements Serializable {

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
        String socialSecurityNumber = person.getSocialSecurityNumber();
        if (socialSecurityNumber != null && FiscalCodeValidation.isValidcontrib(socialSecurityNumber)) {
            searchMemberOutputData.setFiscalCode(Long.valueOf(socialSecurityNumber));
        }else {
            searchMemberOutputData.setFiscalCode(0L);
        }

        Unit institutionUnit = Bennu.getInstance().getInstitutionUnit();

        searchMemberOutputData.setEstablishmentName(institutionUnit.getName());
        searchMemberOutputData.setEstablishmentCode(institutionUnit.getCode());

        ExecutionYear readCurrentExecutionYear = ExecutionYear.readCurrentExecutionYear();
        ExecutionYear previousYear = readCurrentExecutionYear.getPreviousExecutionYear();
        List<ExecutionSemester> semesters = new ArrayList<ExecutionSemester>();
        semesters.addAll(readCurrentExecutionYear.getExecutionPeriodsSet());
        semesters.addAll(previousYear.getExecutionPeriodsSet());

        String stayingIndicator =
                (person.getStudent() != null && person.getStudent().hasActiveRegistrations())
                        || (person.getTeacher() != null && person.getTeacher().getTeacherAuthorizationStream()
                                .anyMatch(authorization -> semesters.contains(authorization.getExecutionSemester()))) ? "S" : "N";
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
        searchMemberOutputData.setDegreeCode(registration.getDegree().getMinistryCode());
        searchMemberOutputData.setDegreeName(registration.getDegree().getIdCardName());
        searchMemberOutputData.setCurricularYear(registration.getCurricularYear());
        List<DegreeCurricularPlan> degreeCurricularPlansForYear =
                registration.getDegree().getDegreeCurricularPlansForYear(ExecutionYear.readCurrentExecutionYear());
        if (!degreeCurricularPlansForYear.isEmpty()) {
            searchMemberOutputData.setDegreeDuration(degreeCurricularPlansForYear.iterator().next().getDurationInYears());
        }
        searchMemberOutputData.setDegreeType(registration.getDegree().getDegreeType().getName().getContent());

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

}
