package com.qubit.solution.fenixedu.integration.cgd.webservices.messages.member;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.Student;

import com.qubit.solution.fenixedu.integration.cgd.webservices.messages.CgdMessageUtils;
import com.qubit.solution.fenixedu.integration.cgd.webservices.resolver.memberid.IMemberIDAdapter;

public class SearchMemberOutput implements Serializable {

    private int replyCode;
    private List<SearchMemberOutputData> memberInfo;

    public SearchMemberOutput() {
        super();
        setMemberInfo(Collections.EMPTY_LIST);
        setReplyCode(CgdMessageUtils.REPLY_CODE_UNEXISTING_MEMBER);
    }

    public int getReplyCode() {
        return replyCode;
    }

    public void setReplyCode(int replyCode) {
        this.replyCode = replyCode;
    }

    public List<SearchMemberOutputData> getMemberInfo() {
        return memberInfo;
    }

    public void setMemberInfo(List<SearchMemberOutputData> memberInfo) {
        this.memberInfo = memberInfo;
    }

    public void populate(Person person, String populationCode, String memberCode, String memberID) {
        boolean verifyMatch = CgdMessageUtils.verifyMatch(person, populationCode, memberCode, memberID);
        if (verifyMatch) {
            setReplyCode(CgdMessageUtils.REPLY_CODE_OPERATION_OK);
            IMemberIDAdapter memberIDStrategy = CgdMessageUtils.getMemberIDStrategy();
            List<SearchMemberOutputData> list = new ArrayList<SearchMemberOutputData>();
            ExecutionYear readCurrentExecutionYear = ExecutionYear.readCurrentExecutionYear();
            if (person.getStudent() != null && !person.getStudent().getActiveRegistrations().isEmpty()) {
                for (Registration registration : person.getStudent().getActiveRegistrations()) {
                    if (!registration.getEnrolments(readCurrentExecutionYear).isEmpty()) {
                        list.add(SearchMemberOutputData.createStudentBased(memberIDStrategy, registration));
                    }
                }
            }
            if (person.getTeacher() != null && person.getTeacher() != null) {
                list.add(SearchMemberOutputData.createTeacherBased(memberIDStrategy, person.getTeacher()));
            }

            if (list.isEmpty()) {
                // Even though the person was found no active students nor teachers were found. So send at least
                // some information about the person we'll create a default package and then check which kind of
                // population was requested. If the person has that entity we'll fill the basic information (basically
                // the id number which was already sent by the cgd)
                //
                // 23 April 2015 - Paulo Abrantes
                SearchMemberOutputData createDefault = SearchMemberOutputData.createDefault(memberIDStrategy, person);
                if (!StringUtils.isEmpty(populationCode)) {
                    switch (populationCode.charAt(0)) {
                    case 'A':
                        Student student = person.getStudent();
                        if (student != null) {
                            createDefault.setPopulationCode("A");
                            createDefault.setStudentNumber(String.valueOf(student.getNumber()));
                        }
                        break;
                    case 'D':
                        Teacher teacher = person.getTeacher();
                        if (teacher != null) {
                            createDefault.setPopulationCode("T");
                            createDefault.setStudentNumber(teacher.getTeacherId());
                        }
                        break;
                    case 'F':
                        // Not yet implemented
                        break;
                    }
                }
                list.add(createDefault);
            }
            setMemberInfo(list);
        } else {
            setReplyCode(CgdMessageUtils.REPLY_CODE_INFORMATION_NOT_OK);
        }
    }
}
