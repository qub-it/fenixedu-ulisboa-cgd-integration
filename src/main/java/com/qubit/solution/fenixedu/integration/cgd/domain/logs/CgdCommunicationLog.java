package com.qubit.solution.fenixedu.integration.cgd.domain.logs;

import java.util.Collection;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DateTime;

public class CgdCommunicationLog extends CgdCommunicationLog_Base {

    public CgdCommunicationLog(Registration registration, boolean requestCard, boolean success, Person sender) {
        setRegistration(registration);
        setRequestCard(requestCard);
        setSuccess(success);
        setSender(sender);
        setSendDate(DateTime.now());
        setRootDomainObject(Bennu.getInstance());
    }

    public static Collection<CgdCommunicationLog> findLogsByStudent(Person person) {
        return Bennu.getInstance().getCgdComunicationLogsSet().stream()
                .filter(log -> log.getRegistration().getPerson().equals(person)).collect(Collectors.toSet());
    }

    public void delete() {
        setRegistration(null);
        setSender(null);
        setSendDate(null);
        setRootDomainObject(null);
    }

    public boolean isSuccess() {
        return getSuccess();
    }
}
