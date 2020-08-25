package org.fenixedu.ulisboa.specifications.domain.idcards;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DeletionListener;

public class CgdCard extends CgdCard_Base {

    static {
        FenixFramework.getDomainModel().registerDeletionListener(Person.class, new DeletionListener<Person>() {

            @Override
            public void deleting(Person person) {
                for (; !person.getCgdCardsSet().isEmpty(); person.getCgdCardsSet().iterator().next().delete());
            }
        });
    }

    protected CgdCard() {
        super();
        setRootDomainObject(Bennu.getInstance());
    }

    public CgdCard(Person person, String mifareCode, boolean temporary) {
        this();
        setPerson(person);
        setMifareCode(mifareCode);
        setIssueDate(new LocalDate());
        setTemporary(temporary);
    }

    @Override
    public void setMifareCode(String mifareCode) {
        String code = mifareCode;
        if (!StringUtils.isEmpty(mifareCode) && mifareCode.length() > 4) {
            String possibleLength = mifareCode.substring(0, 4);
            Integer valueOf = Integer.valueOf(possibleLength);
            String possibleCode = mifareCode.substring(4);
            // Codes may be insered with the check digits if so we have 
            // to add them.
            //
            // 7 September 2015 - Paulo Abrantes
            if (possibleCode.length() != valueOf) {
                code = StringUtils.leftPad(String.valueOf(mifareCode.length()), 4, "0") + mifareCode;
            }
        }

        super.setMifareCode(code);
        setLastMifareModication(new LocalDate());
    }

    @Atomic
    public void delete() {
        setPerson(null);
        setRootDomainObject(null);
        super.deleteDomainObject();
    }

    public boolean isValid() {
        return getValidUntil() == null || getValidUntil().isAfter(new DateTime().toLocalDate());
    }

    public static Optional<CgdCard> findByPersonOptional(Person person) {
        return person.getCgdCardsSet().stream().filter(cgdCard -> cgdCard.isValid()).findFirst();
    }

    public static CgdCard findByPerson(Person person) {
        return findByPersonOptional(person).orElse(null);
    }

}
