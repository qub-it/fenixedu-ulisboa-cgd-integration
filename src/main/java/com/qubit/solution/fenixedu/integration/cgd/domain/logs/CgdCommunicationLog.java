package com.qubit.solution.fenixedu.integration.cgd.domain.logs;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

public class CgdCommunicationLog extends CgdCommunicationLog_Base {

    protected CgdCommunicationLog(Registration registration, boolean requestCard, boolean success, Person sender) {
        setRegistration(registration);
        setRequestCard(requestCard);
        setSuccess(success);
        setSender(sender);
        setSendDate(DateTime.now());
        setRootDomainObject(Bennu.getInstance());
    }

    @Atomic
    public static CgdCommunicationLog createCgdCommunicationLog(Registration registration, boolean requestCard, boolean success,
            Person sender, String message, String exceptionStackTrace) {
        CgdCommunicationLog communicationLog = new CgdCommunicationLog(registration, requestCard, success, sender);
        communicationLog.setMessage(message);
        communicationLog.setExceptionStackTrace(exceptionStackTrace);
        return communicationLog;
    }

    public static Collection<CgdCommunicationLog> findLogsByStudent(Person person) {
        return Bennu.getInstance().getCgdComunicationLogsSet().stream().filter(log -> log.getRegistration().getPerson() == person)
                .sorted(Comparator.comparing(CgdCommunicationLog::getSendDate).reversed()).collect(Collectors.toSet());
    }

    public static Optional<CgdCommunicationLog> findLatestStudentLog(Person person) {
        return findLogsByStudent(person).stream().findFirst();
    }

    public void delete() {
        setRegistration(null);
        setSender(null);
        setSendDate(null);
        setSearchDate(null);
        setUpdateMifareDate(null);
        setRootDomainObject(null);
    }

    public boolean isSuccess() {
        return getSuccess();
    }
}
