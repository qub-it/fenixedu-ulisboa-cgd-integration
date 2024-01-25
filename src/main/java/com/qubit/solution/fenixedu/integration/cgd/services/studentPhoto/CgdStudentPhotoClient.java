package com.qubit.solution.fenixedu.integration.cgd.services.studentPhoto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;

import org.fenixedu.academic.domain.Employee;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.qubit.solution.fenixedu.bennu.webservices.services.client.BennuWebServiceClient;

import services.caixaiu.cgd.wingman.studentPhotoService.IStudentPhotoService;
import services.caixaiu.cgd.wingman.studentPhotoService.Member;
import services.caixaiu.cgd.wingman.studentPhotoService.StudentPhoto;
import services.caixaiu.cgd.wingman.studentPhotoService.StudentPhotoService;

public class CgdStudentPhotoClient extends BennuWebServiceClient<IStudentPhotoService> {

    private static Logger logger = LoggerFactory.getLogger(CgdStudentPhotoClient.class);

    @Override
    protected BindingProvider getService() {
        return (BindingProvider) new StudentPhotoService().getBasicHttpBindingPhotoStudentService();
    }

    public static class MemberCGD {
        String category;
        String memberNumber;

        public boolean isStudent() {
            return "91".equals(category) && memberNumber.length() <= 5 && getMemberAsNumber() != null;
        }

        public Integer getMemberAsNumber() {
            try {
                return Integer.valueOf(this.memberNumber);
            } catch (NumberFormatException e) {
                logger.warn("Invalid student number '{}'", this.memberNumber);
                return null;
            }
        }

        public boolean isStaff() {
            return Arrays.asList("71", "81").contains(this.category);
        }

        public boolean isMemberNumberLikeIdDocumentNumber() {
            return this.memberNumber.length() > 5;
        }

        private MemberCGD(Member member) {
            this.category = member.getMemberCategoryCode().getValue();
            this.memberNumber = member.getMemberNumber().getValue();
        }

        private Person getPersonFromDocumentId() {
            Collection<Person> persons = Person.findPersonByDocumentID(this.memberNumber);
            if (persons.size() > 1) {
                logger.warn("Too many persons match the member number sent from CGD when using document id number.");
                return null;
            } else {
                return Iterables.getFirst(persons, null);
            }
        }

        public Person getPerson() {
            Person personFromDocumentId = getPersonFromDocumentId();
            if (isMemberNumberLikeIdDocumentNumber()) {
                return personFromDocumentId;
            } else if (isStudent()) {
                Student student = Student.readStudentByNumber(getMemberAsNumber());
                if (student != null) {
                    return student.getPerson();
                }
            } else if (isStaff()) {
                Optional<Employee> employeeOpt = Employee.findByNumber(this.memberNumber);
                if (employeeOpt.isPresent()) {
                    return employeeOpt.get().getPerson();
                }
                if (personFromDocumentId != null) {
                    return personFromDocumentId;
                }
            }
            return null;
        }

        public String getMemberNumber() {
            return this.memberNumber;
        }

        public String getCategory() {
            return category;
        }
    }

    public static class PhotoOnCGD {
        private byte[] photoContent;
        private String fileName;

        private PhotoOnCGD(byte[] ba, String fileName) {
            this.photoContent = ba;
            this.fileName = fileName;
        }

        public byte[] getPhotoContent() {
            return photoContent;
        }

        public String getFileName() {
            return fileName;
        }

    }

    public Collection<MemberCGD> getStudentsWithPhotos() {
        logger.debug("CgdPhotoWsClient.getStudentsWithPhotos()");
        IStudentPhotoService ws = (IStudentPhotoService) getClient();
        List<Member> members = ws.getAllStudentsByIES().getMember();

        List<MemberCGD> memberCGDs = new ArrayList<MemberCGD>();
        for (Member member : members) {
            memberCGDs.add(new MemberCGD(member));
        }
        return memberCGDs;
    }

    public PhotoOnCGD getPhoto(final MemberCGD member) {
        return getPhoto(member.getMemberNumber(), member.getCategory());
    }

    /*
     * memberCategory parameter is optional. It is used by CGD to disambiguate between members with
     * the same number and different category.
     */
    public PhotoOnCGD getPhoto(final String memberNumber, final String memberCategory) {
        logger.debug("CgdPhotoWsClient.getStudentsWithPhotos()");
        IStudentPhotoService ws = (IStudentPhotoService) getClient();
        StudentPhoto studentPhoto = ws.getPhoto(memberNumber, memberCategory);
        JAXBElement<byte[]> photoObj = studentPhoto.getPhoto();
        byte[] photoContent = photoObj.getValue();
        if (photoContent.length > 0) {
            JAXBElement<String> fileNameJB = studentPhoto.getFileName();
            String fileName = fileNameJB.getValue();
            return new PhotoOnCGD(photoContent, fileName);
        }
        return null;
    }
}
