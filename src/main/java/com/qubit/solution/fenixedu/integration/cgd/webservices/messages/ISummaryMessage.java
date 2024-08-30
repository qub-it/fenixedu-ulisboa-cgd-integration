package com.qubit.solution.fenixedu.integration.cgd.webservices.messages;

/**
 * Interface to use in web methods input and output DTOs.
 * 
 * @author Antonio Casqueiro | Iscte
 */
public interface ISummaryMessage {

    /**
     * This method is to be called in a simplified log feature.
     * Instead of returning all the DTO data, return only the one relevant for supporting proposed.
     * 
     * @return summary relevant log data
     */
    String getSummaryMessage();

}
