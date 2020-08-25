package com.qubit.solution.fenixedu.integration.cgd.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.fenixedu.academic.domain.DomainObjectUtil;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.contacts.EmailAddress;
import org.fenixedu.academic.domain.contacts.MobilePhone;
import org.fenixedu.academic.domain.contacts.PartyContact;
import org.fenixedu.academic.domain.contacts.PartyContactType;
import org.fenixedu.academic.domain.contacts.Phone;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class CGDPdfFiller {

    public static Comparator<PartyContact> CONTACT_COMPARATOR_BY_MODIFIED_DATE = new Comparator<PartyContact>() {
        @Override
        public int compare(final PartyContact contact, final PartyContact otherContact) {
            int result = contact.getLastModifiedDate().compareTo(otherContact.getLastModifiedDate());
            return result == 0 ? DomainObjectUtil.COMPARATOR_BY_ID.compare(contact, otherContact) : result;
        }
    };

    private static final String MARITAL_STATUS_CIVIL_UNION = "União de facto";
    private static final String MARITAL_STATUS_DIVORCED = "Divorciado";
    private static final String MARITAL_STATUS_SEPARATED = "Sep.judicialmente";
    private static final String MARITAL_STATUS_SINGLE = "Solteiro";
    private static final String MARITAL_STATUS_WIDOWER = "Viúvo";

    /*
     * PdfFiller variables and methods
     * Can not extend PdfFiller, since this class doesn't belong in the first candidacy report
     * */
    private AcroFields form;

    private String getMail(final Person person) {
        if (person.hasInstitutionalEmailAddress()) {
            return person.getInstitutionalEmailAddressValue();
        }
        String emailForSendingEmails = person.getEmailForSendingEmails();
        if (emailForSendingEmails != null) {
            return emailForSendingEmails;
        }

        EmailAddress email = getDefaultPersonalContact(person, EmailAddress.class);
        return email != null ? email.getValue() : "";
    }

    private void setField(final String fieldName, final String fieldContent) throws IOException, DocumentException {
        if (fieldContent != null) {
            form.setField(fieldName, fieldContent);
        }
    }

    private <T extends PartyContact> T getDefaultPersonalContact(final Person person, final Class<T> partyContactClass) {
        T defaultContact = (T) person.getDefaultPartyContact(partyContactClass);
        if (defaultContact != null && defaultContact.getType().equals(PartyContactType.PERSONAL)) {
            return defaultContact;
        }

        Predicate<PartyContact> contactIsPersonal = address -> address.getType().equals(PartyContactType.PERSONAL);
        Predicate<PartyContact> contactIsToBeDefault =
                address -> !address.isActiveAndValid() && address.getPartyContactValidation().getToBeDefault();
        List<T> allContacts = (List<T>) person.getAllPartyContacts(partyContactClass);
        return allContacts.stream().filter(contactIsPersonal).filter(contactIsToBeDefault)
                .sorted(CONTACT_COMPARATOR_BY_MODIFIED_DATE).findFirst().orElse(null);
    }

    /*
     * End PdfFiller variables and methods
     * */

    public ByteArrayOutputStream getFilledPdf(final Person person, final InputStream pdfTemplateStream)
            throws IOException, DocumentException {
        return getFilledPdfCGDPersonalInformation(person, pdfTemplateStream);
    }

    private ByteArrayOutputStream getFilledPdfCGDPersonalInformation(final Person person, final InputStream pdfTemplateStream)
            throws IOException, DocumentException {

        PdfReader reader = new PdfReader(pdfTemplateStream);
        reader.getAcroForm().remove(PdfName.SIGFLAGS);
        reader.selectPages("1,3,4,5"); // The template we are using has a blank page after the front sheet.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, output);
        form = stamper.getAcroFields();

        //Add unicode font for the special characters
        BaseFont bf = BaseFont.createFont("fonts/arialuni.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        form.addSubstitutionFont(bf);

        setField("Nome completo", person.getName());
        setField("email", getMail(person));

        if (person.isFemale()) {
            setField("sexo_F", "Yes"); // female
        } else {
            setField("sexo_M", "Yes"); // male
        }

        if (person.getDateOfBirthYearMonthDay() != null) {
            setField("Data nascimento", person.getDateOfBirthYearMonthDay().toString(DateTimeFormat.forPattern("yyyy/MM/dd")));
        }

        setField("fill_12", person.getFiscalCountry().getName());
        setField("NIF", person.getSocialSecurityNumber());

        setField("fill_20", person.getDocumentIdNumber());

        switch (person.getMaritalStatus()) {
        case CIVIL_UNION:
            setField("Estado civil", MARITAL_STATUS_CIVIL_UNION);
            break;
        case DIVORCED:
            setField("Estado civil", MARITAL_STATUS_DIVORCED);
            break;
        case MARRIED:
            setField("Estado civil", "");
            break;
        case SEPARATED:
            setField("Estado civil", MARITAL_STATUS_SEPARATED);
            break;
        case SINGLE:
            setField("Estado civil", MARITAL_STATUS_SINGLE);
            break;
        case WIDOWER:
            setField("Estado civil", MARITAL_STATUS_WIDOWER);
            break;
        }

        YearMonthDay expirationDate = person.getExpirationDateOfDocumentIdYearMonthDay();
        if (expirationDate != null) {
            setField("Válido até", expirationDate.toString(DateTimeFormat.forPattern("yyyy/MM/dd")));
        }

        setField("FiliaçãoPai", person.getNameOfFather());
        setField("FiliaçãoMãe", person.getNameOfMother());

        if (person.getCountryOfBirth() != null) {
            setField("NaturalidadePaís", person.getCountryOfBirth().getName());
            setField("Distrito", person.getDistrictOfBirth());
            setField("Concelho", person.getDistrictSubdivisionOfBirth());
            setField("Freguesia", person.getParishOfBirth());
            setField("Nacionalidade", person.getCountryOfBirth().getCountryNationality().getContent());
        }

        if (person.getSecondNationality() != null) {
            setField("Sim qualais", person.getSecondNationality().getCountryNationality().getContent());
            setField("outra_nacionalidade2", "Yes");
        } else {
            setField("outra_nacionalidade1", "Yes");
        }

        setField("Morada para correspondência", person.getAddress());
        setField("Localidade", person.getAreaOfAreaCode());

        String postalCode = person.getPostalCode();
        int dashIndex = postalCode.indexOf('-');
        if (postalCode != null && postalCode.length() >= dashIndex + 4) {
            setField("Código postal", postalCode.substring(0, 4));
            String last3Numbers = postalCode.substring(dashIndex + 1, dashIndex + 4);
            setField("undefined_1", last3Numbers);
            setField("T_Localid02_1", person.getAreaOfAreaCode());
        }

        if (person.getCountryOfResidence() != null) {
            setField("Distrito_2", person.getDistrictOfResidence());
            setField("Concelho_2", person.getDistrictSubdivisionOfResidence());
            setField("Freguesia_2", person.getParishOfResidence());
            setField("País", person.getCountryOfResidence().getName());
        }

        Phone phone = getDefaultPersonalContact(person, Phone.class);
        if (phone != null) {
            //TODO how to ensure is portuguese phone
//            setField("Telefone", "+351");
            setField("undefined_2", phone.getNumber());
        }
        MobilePhone mobilePhone = getDefaultPersonalContact(person, MobilePhone.class);
        if (mobilePhone != null) {
            //TODO how to ensure is portuguese phone
//            setField("Telemóvel", "+351");
            setField("Trabalhador", mobilePhone.getNumber());
        }

        Unit institutionUnit = Bennu.getInstance().getInstitutionUnit();
        setField("Estabel ensino", institutionUnit.getAcronym());
        setField("undefined_4", institutionUnit.getName());

        setField("N aluno", "" + person.getStudent().getNumber());

        setField("undefined_6", person.getDocumentIdNumber());
        setField("fill_2", person.getName());

        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }
}
