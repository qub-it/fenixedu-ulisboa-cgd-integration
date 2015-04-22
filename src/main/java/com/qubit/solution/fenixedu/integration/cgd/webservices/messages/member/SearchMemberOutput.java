package com.qubit.solution.fenixedu.integration.cgd.webservices.messages.member;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;

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

    public void populate(Person person) {
        setReplyCode(CgdMessageUtils.REPLY_CODE_OPERATION_OK);
        IMemberIDAdapter memberIDStrategy = CgdMessageUtils.getMemberIDStrategy();
        List<SearchMemberOutputData> list = new ArrayList<SearchMemberOutputData>();
        if (person.getStudent() != null && !person.getStudent().getActiveRegistrations().isEmpty()) {
            for (Registration registration : person.getStudent().getActiveRegistrations()) {
                list.add(SearchMemberOutputData.createStudentBased(memberIDStrategy, registration));
            }
        }
        if (person.getTeacher() != null && person.getTeacher().isActiveContractedTeacher()) {
            list.add(SearchMemberOutputData.createTeacherBased(memberIDStrategy, person.getTeacher()));
        }

        if (list.isEmpty()) {
            list.add(SearchMemberOutputData.createDefault(memberIDStrategy, person));
        }
        setMemberInfo(list);
    }
}
