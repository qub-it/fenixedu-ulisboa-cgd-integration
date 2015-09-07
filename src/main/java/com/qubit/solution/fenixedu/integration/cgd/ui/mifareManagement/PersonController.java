package com.qubit.solution.fenixedu.integration.cgd.ui.mifareManagement;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.qubit.solution.fenixedu.integration.cgd.ui.CgdBaseController;
import com.qubit.solution.fenixedu.integration.cgd.ui.CgdController;

@SpringFunctionality(app = CgdController.class, title = "label.title.mifareManagement",
        accessGroup = "#managers | #cgdCollaborators")
@RequestMapping("/cgd/mifaremanagement/person")
public class PersonController extends CgdBaseController {

    @RequestMapping
    public String home(Model model) {
        return "forward:/cgd/mifaremanagement/person/";
    }

    private void setPerson(Person person, Model m) {
        m.addAttribute("person", person);
    }

    private Person getPerson(Model m) {
        return (Person) m.asMap().get("person");
    }

    @RequestMapping(value = "/")
    public String search(@RequestParam(value = "name", required = false, defaultValue = "") String name, @RequestParam(
            value = "username", required = false) String username,
            @RequestParam(value = "documentidnumber", required = false) String documentIdNumber, Model model) {

        List<Person> searchpersonResultsDataSet = filterSearchPerson(name, username, documentIdNumber);
        model.addAttribute("searchpersonResultsDataSet", searchpersonResultsDataSet);
        return "cgd/mifaremanagement/person/search";
    }

    private List<Person> filterSearchPerson(String name, String username, String documentIdNumber) {
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(username) && StringUtils.isEmpty(documentIdNumber)) {
            return Collections.emptyList();
        }

        Stream<Person> stream =
                StringUtils.isEmpty(name) ? Party.getPartysSet(Person.class).stream() : Person.findPersonStream(name,
                        Integer.MAX_VALUE);
        return stream.filter(person -> StringUtils.isEmpty(username) || username.equals(person.getUsername()))
                .filter(person -> StringUtils.isEmpty(documentIdNumber) || documentIdNumber.equals(person.getDocumentIdNumber()))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") Person person, Model model) {
        return "redirect:/cgd/mifaremanagement/person/readpersonmifare" + "/" + person.getExternalId();
    }

    @RequestMapping(value = "/readpersonmifare/{oid}")
    public String readpersonmifare(@PathVariable("oid") Person person, Model model) {
        setPerson(person, model);
        return "cgd/mifaremanagement/person/readpersonmifare";
    }

    @RequestMapping(value = "/readpersonmifare/{oid}/createmifarecard")
    public String processReadpersonmifareToCreateMifareCard(@PathVariable("oid") Person person, Model model) {
        setPerson(person, model);
        return "redirect:/cgd/mifaremanagement/cgdcard/create/" + getPerson(model).getExternalId();
    }
}
