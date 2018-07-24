package com.qubit.solution.fenixedu.integration.cgd.services;

public class CgdAuthorizationCodes {

    /*Instituição de Ensino Superior
    Grupo do membro
    Classe do membro
    Número do membro
    Nome Completo
    Data de nascimento
    Nacionalidade
    País de residência Fiscal
    Número de Identificação Fiscal
    Número do documento de identificação
    Estabelecimento de Ensino Superior
    Curso
    Nº aluno
    Ano curricular
    Grau de Ensino*/

//    E-mail - PersonData.email
//    Sexo   - PersonData.genderCode
//    Cód. finanças - PersonData.fiscalNumber
//    Outro país fiscal - ?
//    NIF Outro país fiscal - ?
//    Cód. finanças Outro país fiscal - ?
//    Estado civil - personData.MaritalStatus
//    Válido do documento de identificação - identificationCard.setExpirationDate
//    Entidade emissora/País do documento de identificação - ?
//    Filiação: Nome do Pai - personData.father
//    Filiação: Nome da Mãe - personData.mother
//    Naturalidade-País - personData.placeOfBirthCountryCode
//    Naturalidade-Distrito - personData.placeOfBirthDistrict
//    Naturalidade-Concelho - personData.placeOfBirthCounty
//    Naturalidade-Freguesia -personData.placeOfBirthParish
//    Outra nacionalidade - personData.nationalities
//    Morada para correspondência - personData.address
//    Localidade                  - personData.place
//    Código postal               - personData.postalCode
//    Distrito                    - personData.district
//    Concelho                    - personData.postalCounty
//    Freguesia                   - personData.postalParish
//    País                        - personData.countryOfResidenceCode
//    País Telefone   - ?
//    Telefone       - personData.phone
//    País Telemóvel  - ? 
//    Telemóvel      - personData.mobilePhone
// Só indicamos qual é o a professional condition 
//    Trabalhador
//    Por conta de outrem
//    CIRS
//    Profissão
//    Entidade Patronal
//    Situação sócio profissional
//    Cargo político ou público
//    Cargo político ou público Próprio 
//    Cargo político ou público Familiar próximo/Relacionamento comercial
//    Cargo
//    Entidade
//    Morada residência - ?
//    Morada            - ?
//    Localidade        - ?
//    Código postal     - ?
//    Distrito          - ?
//    Concelho          - ?
//    Freguesia         - ?
//    País              - ?
//    Morada fiscal     - ?
//    Morada            - ?
//    Localidade        - ? 
//    Código postal     - ? 
//    Distrito          - ?
//    Concelho          - ?
//    Freguesia         - ?
//    País              - ?

    public static String BASIC_INFO = "CGD_BASIC_INFO";

    public static String EXTENDED_INFO = "CGD_EXTENDED_INFO";

    public static String EXTENDED_INFO_EMAIL = "EXTENDED_INFO_EMAIL";

    public static String EXTENDED_INFO_GENDER = "EXTENDED_INFO_GENDER";

    public static String EXTENDED_INFO_FISCAL_NUMBER = "EXTENDED_INFO_FISCAL_NUMBER";

    public static String EXTENDED_INFO_MARITAL_STATUS = "EXTENDED_INFO_MARITAL_STATUS";

    public static String EXTENDED_INFO_ID_CARD_EXPIRATION_DATE = "EXTENDED_INFO_ID_CARD_EXPIRATION_DATE";

    public static String EXTENDED_INFO_FATHER_NAME = "EXTENDED_INFO_FATHER_NAME";

    public static String EXTENDED_INFO_MOTHER_NAME = "EXTENDED_INFO_MOTHER_NAME";

    public static String EXTENDED_INFO_BIRTH_COUNTRY = "EXTENDED_INFO_BIRTH_COUNTRY";
    public static String EXTENDED_INFO_BIRTH_DISTRICT = "EXTENDED_INFO_BIRTH_DISTRICT";
    public static String EXTENDED_INFO_BIRTH_COUNTY = "EXTENDED_INFO_BIRTH_COUNTY";
    public static String EXTENDED_INFO_BIRTH_PARISH = "EXTENDED_INFO_BIRTH_PARISH";

    public static String EXTENDED_INFO_OTHER_NATIONALITIES = "EXTENDED_INFO_OTHER_NATIONALITIES";

    public static String EXTENDED_INFO_ADDRESS_PLACE = "EXTENDED_INFO_ADDRESS_PLACE";
    public static String EXTENDED_INFO_ADDRESS_POSTAL_CODE = "EXTENDED_INFO_ADDRESS_POSTAL_CODE";
    public static String EXTENDED_INFO_ADDRESS_DISTRICT = "EXTENDED_INFO_ADDRESS_DISTRICT";
    public static String EXTENDED_INFO_ADDRESS_POSTAL_COUNTY = "EXTENDED_INFO_ADDRESS_POSTAL_COUNTY";
    public static String EXTENDED_INFO_ADDRESS_POSTAL_PARISH = "EXTENDED_INFO_ADDRESS_POSTAL_PARISH";
    public static String EXTENDED_INFO_ADDRESS_COUNTRY = "EXTENDED_INFO_ADDRESS_POSTAL_COUNTRY";

    public static String EXTENDED_INFO_PHONE = "EXTENDED_INFO_PHONE";
    public static String EXTENDED_INFO_MOBILE_PHONE = "EXTENDED_INFO_MOBILE_PHONE";

    public static String EXTENDED_INFO_WORKING_INFO = "EXTENDED_INFO_WORKING_INFO";

    public static final String EXTENDED_INFO_FISCAL_COUNTRY = "EXTENDED_INFO_FISCAL_COUNTRY";
    public static String PHOTO = "CGD_PHOTO";
}
