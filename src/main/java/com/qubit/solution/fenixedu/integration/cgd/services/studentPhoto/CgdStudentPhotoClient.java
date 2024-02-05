package com.qubit.solution.fenixedu.integration.cgd.services.studentPhoto;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;

import org.fenixedu.academic.domain.Employee;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qubit.solution.fenixedu.bennu.webservices.services.client.BennuWebServiceClient;

import services.caixaiu.cgd.wingman.studentPhotoService.IStudentPhotoService;
import services.caixaiu.cgd.wingman.studentPhotoService.Member;
import services.caixaiu.cgd.wingman.studentPhotoService.StudentPhoto;
import services.caixaiu.cgd.wingman.studentPhotoService.StudentPhotoService;

public class CgdStudentPhotoClient extends BennuWebServiceClient<IStudentPhotoService> {

    private static Logger logger = LoggerFactory.getLogger(CgdStudentPhotoClient.class);

    public static final String STUDENT_IDENTIFICATION_CODE = "91";
    public static final String EMPLOYEE_IDENTIFICATION_CODE = "71";
    public static final String TEACHER_IDENTIFICATION_CODE = "81";

    @Override
    protected BindingProvider getService() {
        return (BindingProvider) new StudentPhotoService().getBasicHttpBindingPhotoStudentService();
    }

    public static class MemberCGD {
        private String category;
        private String memberNumber;

        private MemberCGD(Member member) {
            this.category = Optional.ofNullable(member.getMemberCategoryCode()).map(mcc -> mcc.getValue()).orElse(null);
            this.memberNumber = Optional.ofNullable(member.getMemberNumber()).map(mn -> mn.getValue()).orElse(null);
        }

        public boolean isStudent() {
            return STUDENT_IDENTIFICATION_CODE.equals(category)
                    && Optional.ofNullable(memberNumber).map(memberNumber -> memberNumber.length() <= 5).orElse(false)
                    && getMemberAsNumber() != null;
        }

        public Integer getMemberAsNumber() {
            return Optional.ofNullable(memberNumber).map(memberNumber -> Integer.valueOf(memberNumber)).orElse(null);
        }

        public boolean isStaff() {
            return Arrays.asList(EMPLOYEE_IDENTIFICATION_CODE, TEACHER_IDENTIFICATION_CODE).contains(this.category);
        }

        public boolean isMemberNumberLikeIdDocumentNumber() {
            return Optional.ofNullable(memberNumber).map(memberNumber -> memberNumber.length() > 5).orElse(false);
        }

        private Optional<Person> getPersonFromDocumentId() {
            Collection<Person> persons = Person.findPersonByDocumentID(this.memberNumber);
            if (persons.isEmpty() || persons.size() > 1) {
                return Optional.empty();
            } else {
                return Optional.of(persons.iterator().next());
            }
        }

        public Optional<Person> getPerson() {
            Optional<Person> personFromDocumentId = getPersonFromDocumentId();
            if (isMemberNumberLikeIdDocumentNumber()) {
                return personFromDocumentId;
            } else if (isStudent()) {
                Student student = Student.readStudentByNumber(getMemberAsNumber());
                if (student != null) {
                    return Optional.of(student.getPerson());
                }
            } else if (isStaff()) {
                Optional<Employee> employeeOpt = Employee.findByNumber(this.memberNumber);
                if (employeeOpt.isPresent()) {
                    return Optional.of(employeeOpt.get().getPerson());
                }
                if (personFromDocumentId != null) {
                    return personFromDocumentId;
                }
            }
            return Optional.empty();
        }

        public String getMemberNumber() {
            return this.memberNumber;
        }

        public String getCategory() {
            return this.category;
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
        final IStudentPhotoService ws = (IStudentPhotoService) getClient();
        final Set<MemberCGD> membersCGD =
                ws.getAllStudentsByIES().getMember().stream().map(member -> new MemberCGD(member)).collect(Collectors.toSet());
        return membersCGD;
    }

    public Optional<PhotoOnCGD> getPhoto(final MemberCGD member) {
        return getPhoto(member.getMemberNumber(), member.getCategory());
    }

    /*
     * memberCategory parameter is optional. It is used by CGD to disambiguate between members with
     * the same number and different category.
     */
    public Optional<PhotoOnCGD> getPhoto(final String memberNumber, final String memberCategory) {
        logger.debug("CgdPhotoWsClient.getStudentsWithPhotos()");
        IStudentPhotoService ws = (IStudentPhotoService) getClient();
        StudentPhoto studentPhoto = ws.getPhoto(memberNumber, memberCategory);
        JAXBElement<byte[]> photoObj = studentPhoto.getPhoto();
        byte[] photoContent = photoObj.getValue();
        if (photoContent.length > 0) {
            JAXBElement<String> fileNameJB = studentPhoto.getFileName();
            String fileName = fileNameJB.getValue();
            return Optional.of(new PhotoOnCGD(photoContent, fileName));
        }
        return Optional.empty();
    }
}
