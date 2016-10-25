package com.qubit.solution.fenixedu.integration.cgd.ui.mifareManagement;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
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

    @RequestMapping(value = "/delete/{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") CgdCard cgdCard, Model model) {
        Person person = cgdCard.getPerson();
        cgdCard.delete();
        return "redirect:/cgd/mifaremanagement/person/readpersonmifare/" + person.getExternalId();
    }

    @RequestMapping(value = "/update/{oid}", method = RequestMethod.POST)
    public String update(
            @PathVariable("oid") CgdCard cgdCard,
            @RequestParam(value = "mifarecode", required = true) java.lang.String mifareCode,
            @RequestParam(value = "cardNumber", required = false) java.lang.String cardNumber,
            @RequestParam(value = "validuntil", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") org.joda.time.LocalDate validUntil,
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
            @RequestParam(value = "validuntil", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") org.joda.time.LocalDate validUntil,
            @RequestParam(value = "temporary", required = true) boolean temporary, Model model) {

        String correctMifareCode = mifareCode;
        if (!StringUtils.isBlank(mifareCode) && mifareCode.length() > 4) {
            String possibleLength = mifareCode.substring(0, 4);
            Integer valueOf = Integer.valueOf(possibleLength);
            String possibleCode = mifareCode.substring(4);
            // Codes may be insered with the check digits if so we have 
            // to add them.
            //
            // 7 September 2015 - Paulo Abrantes
            if (possibleCode.length() != valueOf) {
                correctMifareCode = StringUtils.leftPad(String.valueOf(mifareCode.length()), 4, "0") + mifareCode;
            }
        }

        if (StringUtils.isBlank(mifareCode)) {
            addErrorMessage(BundleUtil.getString(BUNDLE, "label.mifareManagment.cgdCard.cannotBeBlank"), model);
            model.addAttribute("person", person);
            return "cgd/mifaremanagement/cgdcard/create";
        }
        final String mifareCodeToCheck = correctMifareCode;
        if (Bennu.getInstance().getCgdCardsSet().stream().anyMatch(card -> mifareCodeToCheck.equals(card.getMifareCode()))) {
            addErrorMessage(BundleUtil.getString(BUNDLE, "label.mifareManagment.cgdCard.duplicate", mifareCodeToCheck), model);
            model.addAttribute("person", person);
            return "cgd/mifaremanagement/cgdcard/create";
        } else {

            CgdCard cgdCard = createCgdCard(person, mifareCode, validUntil, temporary);
            model.addAttribute("cgdCard", cgdCard);

            return "redirect:/cgd/mifaremanagement/person/readpersonmifare/" + person.getExternalId();
        }
    }

    @Atomic
    public CgdCard createCgdCard(Person person, java.lang.String mifareCode, org.joda.time.LocalDate validUntil, boolean temporary) {
        CgdCard cgdCard = new CgdCard(person, mifareCode, temporary);
        cgdCard.setValidUntil(validUntil);
        return cgdCard;
    }
}
