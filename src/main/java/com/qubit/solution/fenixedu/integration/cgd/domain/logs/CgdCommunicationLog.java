package com.qubit.solution.fenixedu.integration.cgd.domain.logs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
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
            Person sender, String message, String exceptionStackTrace, String dataSent) {
        CgdCommunicationLog communicationLog = new CgdCommunicationLog(registration, requestCard, success, sender);
        communicationLog.setMessage(message);
        communicationLog.setExceptionStackTrace(exceptionStackTrace);
        communicationLog.setDataSent(dataSent == null || dataSent.isBlank() ? "" : compressData(dataSent));
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

    @Override
    public String getDataSent() {
        String result = "";
        String compressedData = super.getDataSent();
        try {
            result = compressedData.isBlank() ? result : uncompressData(Base64.getDecoder().decode(compressedData));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String compressData(String dataSent) {
        byte[] byteToEncode = new byte[0];
        try {
            byteToEncode = compress(dataSent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(byteToEncode);
    }

    private static byte[] compress(String dataSent) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(byteArrayOutputStream);
        gzip.write(dataSent.getBytes());
        gzip.close();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return byteArray;
    }

    private String uncompressData(byte[] compressedData) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
        GZIPInputStream gunzip = new GZIPInputStream(byteArrayInputStream);
        byte[] byteArray = IOUtils.toByteArray(gunzip);
        gunzip.close();
        byteArrayInputStream.close();
        return new String(byteArray);
    }

}
