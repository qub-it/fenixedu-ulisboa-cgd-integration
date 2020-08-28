/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: paulo.abrantes@qub-it.com
 *
 * 
 * This file is part of FenixEdu fenixedu-ulisboa-cgdIntegration.
 *
 * FenixEdu fenixedu-ulisboa-cgdIntegration is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu fenixedu-ulisboa-cgdIntegration is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu fenixedu-ulisboa-cgdIntegration.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qubit.solution.fenixedu.integration.cgd.ui.cgdConfiguration;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.StringNormalizer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdIntegrationConfiguration;
import com.qubit.solution.fenixedu.integration.cgd.domain.configuration.CgdMod43Template;
import com.qubit.solution.fenixedu.integration.cgd.services.CgdAddressProofProvider;
import com.qubit.solution.fenixedu.integration.cgd.ui.CgdBaseController;
import com.qubit.solution.fenixedu.integration.cgd.ui.CgdController;
import com.qubit.solution.fenixedu.integration.cgd.webservices.resolver.memberid.IMemberIDAdapter;

import pt.ist.fenixframework.Atomic;

@SpringFunctionality(app = CgdController.class, title = "label.title.cgdConfiguration", accessGroup = "#managers")
@RequestMapping("/cgd/cgdconfiguration/cgdintegrationconfiguration")
public class CgdIntegrationConfigurationController extends CgdBaseController {

    @RequestMapping
    public String home(Model model) {
        CgdIntegrationConfiguration instance = CgdIntegrationConfiguration.getInstance();
        return "forward:/cgd/cgdconfiguration/cgdintegrationconfiguration/read/" + instance.getExternalId();
    }

    private CgdIntegrationConfiguration getCgdIntegrationConfiguration(Model m) {
        return (CgdIntegrationConfiguration) m.asMap().get("cgdIntegrationConfiguration");
    }

    private void setCgdIntegrationConfiguration(CgdIntegrationConfiguration cgdIntegrationConfiguration, Model m) {
        m.addAttribute("cgdIntegrationConfiguration", cgdIntegrationConfiguration);
    }

    @RequestMapping(value = "/read/{oid}")
    public String read(@PathVariable("oid") CgdIntegrationConfiguration cgdIntegrationConfiguration, Model model) {
        setCgdIntegrationConfiguration(cgdIntegrationConfiguration, model);
        return "cgd/cgdconfiguration/cgdintegrationconfiguration/read";
    }

    @RequestMapping(value = "/update/{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") CgdIntegrationConfiguration cgdIntegrationConfiguration, Model model) {
        setCgdIntegrationConfiguration(cgdIntegrationConfiguration, model);
        return "cgd/cgdconfiguration/cgdintegrationconfiguration/update";
    }

    @RequestMapping(value = "/update/{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") CgdIntegrationConfiguration cgdIntegrationConfiguration,
            @RequestParam(value = "memberidresolverclass", required = false) java.lang.String memberIDResolverClass,
            @RequestParam(value = "addressProofGeneratorClass", required = false) java.lang.String addressProofGeneratorClass,
            @RequestParam(value = "cgdTemplateFile", required = true) final MultipartFile cgdTemplateFile, Model model) {

        setCgdIntegrationConfiguration(cgdIntegrationConfiguration, model);
        boolean validClass = false;
        try {
            Class clazz = Class.forName(memberIDResolverClass);
            validClass = IMemberIDAdapter.class.isAssignableFrom(clazz);
        } catch (Throwable t) {
        }

        if (!validClass) {
            addErrorMessage(
                    "Class " + memberIDResolverClass
                            + " not a valid class. Could it be it's mispelled? Or perhaps does not implement IMemberIDAdapter",
                    model);
            return update(cgdIntegrationConfiguration, model);
        }

        updateCgdIntegrationConfiguration(memberIDResolverClass, addressProofGeneratorClass, cgdTemplateFile, model);

        return "redirect:/cgd/cgdconfiguration/cgdintegrationconfiguration/read/"
                + getCgdIntegrationConfiguration(model).getExternalId();
    }

    @Atomic
    public void updateCgdIntegrationConfiguration(java.lang.String memberIDResolverClass,
            java.lang.String addressProofGeneratorClass, MultipartFile cgdTemplateFile, Model m) {
        CgdIntegrationConfiguration cgdIntegrationConfiguration = getCgdIntegrationConfiguration(m);
        cgdIntegrationConfiguration.setMemberIDResolverClass(memberIDResolverClass);
        cgdIntegrationConfiguration.setAddressProofGeneratorClass(addressProofGeneratorClass);
        if (!cgdTemplateFile.isEmpty()) {
            String fileName = cgdTemplateFile.getOriginalFilename();
            byte[] fileContent;
            try {
                fileContent = cgdTemplateFile.getBytes();
                cgdIntegrationConfiguration.uploadMod43Template(fileName, fileContent);
            } catch (IOException e) {
                throw new RuntimeException("Unable to upload file", e);
            }
        }

    }

    @RequestMapping(value = "/update/{oid}/addressProofGenerators", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @org.springframework.web.bind.annotation.ResponseBody List<String> requestAvailableAddressProofGenerators(
            @PathVariable("oid") CgdIntegrationConfiguration cgdIntegrationConfiguration, Model model) {

        List<String> results = new ArrayList<String>();
        CgdAddressProofProvider.getCgdEnrolmentProviders().forEach(clazz -> results.add(clazz.getName()));
        return results;
    }

    @RequestMapping(value = "/update/{oid}/strategies", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @org.springframework.web.bind.annotation.ResponseBody List<String> requestAvailableStrategies(
            @PathVariable("oid") CgdIntegrationConfiguration cgdIntegrationConfiguration, Model model) {

        List<String> results = new ArrayList<String>();
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(IMemberIDAdapter.class));
        for (BeanDefinition definition : provider
                .findCandidateComponents("/com/qubit/solution/fenixedu/integration/cgd/services/memberid")) {
            results.add(definition.getBeanClassName());
        }

        return results;
    }

    @RequestMapping(value = "/downloadTemplate/{oid}", method = RequestMethod.GET)
    public void downloadCGDTemplate(@PathVariable("oid") final CgdMod43Template template, final HttpServletResponse response) {
        try {
            if (template.isAccessible(Authenticate.getUser())) {
                response.setContentType(template.getContentType());
                String filename = URLEncoder.encode(
                        StringNormalizer.normalizePreservingCapitalizedLetters(template.getDisplayName()).replaceAll("\\s", "_"),
                        "UTF-8");
                response.setHeader("Content-disposition", "attachment; filename=" + filename);
                response.getOutputStream().write(template.getContent());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
