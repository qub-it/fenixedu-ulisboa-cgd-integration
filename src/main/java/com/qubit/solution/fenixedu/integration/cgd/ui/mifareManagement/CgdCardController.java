package com.qubit.solution.fenixedu.integration.cgd.ui.mifareManagement;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.ulisboa.specifications.domain.idcards.CgdCard;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pt.ist.fenixframework.Atomic;

import com.qubit.solution.fenixedu.integration.cgd.ui.CgdBaseController;

@BennuSpringController(value = PersonController.class)
@RequestMapping("/cgd/mifaremanagement/cgdcard")
public class CgdCardController extends CgdBaseController {

    @RequestMapping
    public String home(Model model) {
        return "forward:/cgd/mifaremanagement/cgdcard/";
    }

    private CgdCard getCgdCard(Model m) {
        return (CgdCard) m.asMap().get("cgdCard");
    }

    private void setCgdCard(CgdCard cgdCard, Model m) {
        m.addAttribute("cgdCard", cgdCard);
    }

    @RequestMapping(value = "/update/{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") CgdCard cgdCard, Model model) {
        setCgdCard(cgdCard, model);
        return "cgd/mifaremanagement/cgdcard/update";
    }

    @RequestMapping(value = "/update/{oid}", method = RequestMethod.POST)
    public String update(
            @PathVariable("oid") CgdCard cgdCard,
            @RequestParam(value = "mifarecode", required = true) java.lang.String mifareCode,
            @RequestParam(value = "cardNumber", required = false) java.lang.String cardNumber,
            @RequestParam(value = "validuntil", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") org.joda.time.LocalDate validUntil,
            @RequestParam(value = "temporary", required = true) boolean temporary, Model model) {

        setCgdCard(cgdCard, model);
        updateCgdCard(mifareCode, cardNumber, validUntil, temporary, model);
        return "redirect:/cgd/mifaremanagement/person/readpersonmifare/" + getCgdCard(model).getPerson().getExternalId();
    }

    @Atomic
    public void updateCgdCard(java.lang.String mifareCode, String cardNumber, org.joda.time.LocalDate validUntil,
            boolean temporary, Model m) {
        CgdCard cgdCard = getCgdCard(m);
        cgdCard.setMifareCode(mifareCode);
        cgdCard.setCardNumber(cardNumber);
        cgdCard.setValidUntil(validUntil);
        cgdCard.setTemporary(temporary);
    }

    @RequestMapping(value = "/create/{oid}", method = RequestMethod.GET)
    public String create(@PathVariable("oid") Person person, Model model) {
        model.addAttribute("person", person);
        return "cgd/mifaremanagement/cgdcard/create";
    }

    @RequestMapping(value = "/create/{oid}", method = RequestMethod.POST)
    public String create(
            @PathVariable("oid") Person person,
            @RequestParam(value = "mifarecode", required = false) java.lang.String mifareCode,
            @RequestParam(value = "validuntil", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") org.joda.time.LocalDate validUntil,
            @RequestParam(value = "temporary", required = true) boolean temporary, Model model) {

        CgdCard cgdCard = createCgdCard(person, mifareCode, validUntil, temporary);
        model.addAttribute("cgdCard", cgdCard);

        return "redirect:/cgd/mifaremanagement/person/readpersonmifare/" + person.getExternalId();
    }

    @Atomic
    public CgdCard createCgdCard(Person person, java.lang.String mifareCode, org.joda.time.LocalDate validUntil, boolean temporary) {
        CgdCard cgdCard = new CgdCard(person, mifareCode, temporary);
        cgdCard.setValidUntil(validUntil);
        return cgdCard;
    }
}
