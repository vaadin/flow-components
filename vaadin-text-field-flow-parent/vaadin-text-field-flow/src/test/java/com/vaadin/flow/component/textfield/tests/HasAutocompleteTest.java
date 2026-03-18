/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.textfield.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.HasAutocomplete;

class HasAutocompleteTest {

    @Tag("div")
    public static class HasAutocompleteComponent extends Component
            implements HasAutocomplete {

    }

    @Test
    void defaultValue() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        Assertions.assertEquals(null, c.getAutocomplete());
    }

    @Test
    void emptyValue() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.getElement().setAttribute("autocomplete", "");
        Assertions.assertEquals(Autocomplete.OFF, c.getAutocomplete());
    }

    @Test
    void noAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.OFF);
        Assertions.assertEquals(Autocomplete.OFF, c.getAutocomplete());
    }

    @Test
    void onAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ON);
        Assertions.assertEquals(Autocomplete.ON, c.getAutocomplete());
    }

    @Test
    void nameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.NAME);
        Assertions.assertEquals(Autocomplete.NAME, c.getAutocomplete());
    }

    @Test
    void honorificPrefixAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.HONORIFIC_PREFIX);
        Assertions.assertEquals(Autocomplete.HONORIFIC_PREFIX,
                c.getAutocomplete());
    }

    @Test
    void givenNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.GIVEN_NAME);
        Assertions.assertEquals(Autocomplete.GIVEN_NAME, c.getAutocomplete());
    }

    @Test
    void additionalNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDITIONAL_NAME);
        Assertions.assertEquals(Autocomplete.ADDITIONAL_NAME,
                c.getAutocomplete());
    }

    @Test
    void familyNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.FAMILY_NAME);
        Assertions.assertEquals(Autocomplete.FAMILY_NAME, c.getAutocomplete());
    }

    @Test
    void honorificSuffixAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.HONORIFIC_SUFFIX);
        Assertions.assertEquals(Autocomplete.HONORIFIC_SUFFIX,
                c.getAutocomplete());
    }

    @Test
    void nicknameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.NICKNAME);
        Assertions.assertEquals(Autocomplete.NICKNAME, c.getAutocomplete());
    }

    @Test
    void emailAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.EMAIL);
        Assertions.assertEquals(Autocomplete.EMAIL, c.getAutocomplete());
    }

    @Test
    void usernameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.USERNAME);
        Assertions.assertEquals(Autocomplete.USERNAME, c.getAutocomplete());
    }

    @Test
    void newPasswordAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.NEW_PASSWORD);
        Assertions.assertEquals(Autocomplete.NEW_PASSWORD, c.getAutocomplete());
    }

    @Test
    void currentPasswordAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CURRENT_PASSWORD);
        Assertions.assertEquals(Autocomplete.CURRENT_PASSWORD,
                c.getAutocomplete());
    }

    @Test
    void organizationTitleAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ORGANIZATION_TITLE);
        Assertions.assertEquals(Autocomplete.ORGANIZATION_TITLE,
                c.getAutocomplete());
    }

    @Test
    void organizationAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ORGANIZATION);
        Assertions.assertEquals(Autocomplete.ORGANIZATION, c.getAutocomplete());
    }

    @Test
    void streetAddressAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.STREET_ADDRESS);
        Assertions.assertEquals(Autocomplete.STREET_ADDRESS,
                c.getAutocomplete());
    }

    @Test
    void addressLine1Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LINE1);
        Assertions.assertEquals(Autocomplete.ADDRESS_LINE1,
                c.getAutocomplete());
    }

    @Test
    void addressLine2Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LINE2);
        Assertions.assertEquals(Autocomplete.ADDRESS_LINE2,
                c.getAutocomplete());
    }

    @Test
    void addressLine3Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LINE3);
        Assertions.assertEquals(Autocomplete.ADDRESS_LINE3,
                c.getAutocomplete());
    }

    @Test
    void addressLevel1Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LEVEL1);
        Assertions.assertEquals(Autocomplete.ADDRESS_LEVEL1,
                c.getAutocomplete());
    }

    @Test
    void addressLevel2Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LEVEL2);
        Assertions.assertEquals(Autocomplete.ADDRESS_LEVEL2,
                c.getAutocomplete());
    }

    @Test
    void addressLevel3Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LEVEL3);
        Assertions.assertEquals(Autocomplete.ADDRESS_LEVEL3,
                c.getAutocomplete());
    }

    @Test
    void addressLevel4Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LEVEL4);
        Assertions.assertEquals(Autocomplete.ADDRESS_LEVEL4,
                c.getAutocomplete());
    }

    @Test
    void countryAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.COUNTRY);
        Assertions.assertEquals(Autocomplete.COUNTRY, c.getAutocomplete());
    }

    @Test
    void countryNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.COUNTRY_NAME);
        Assertions.assertEquals(Autocomplete.COUNTRY_NAME, c.getAutocomplete());
    }

    @Test
    void postalCodeAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.POSTAL_CODE);
        Assertions.assertEquals(Autocomplete.POSTAL_CODE, c.getAutocomplete());
    }

    @Test
    void ccNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_NAME);
        Assertions.assertEquals(Autocomplete.CC_NAME, c.getAutocomplete());
    }

    @Test
    void ccGivenNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_GIVEN_NAME);
        Assertions.assertEquals(Autocomplete.CC_GIVEN_NAME,
                c.getAutocomplete());
    }

    @Test
    void ccAdditionalNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_ADDITIONAL_NAME);
        Assertions.assertEquals(Autocomplete.CC_ADDITIONAL_NAME,
                c.getAutocomplete());
    }

    @Test
    void ccFamilyNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_FAMILY_NAME);
        Assertions.assertEquals(Autocomplete.CC_FAMILY_NAME,
                c.getAutocomplete());
    }

    @Test
    void ccNumberAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_NUMBER);
        Assertions.assertEquals(Autocomplete.CC_NUMBER, c.getAutocomplete());
    }

    @Test
    void ccExpAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_EXP);
        Assertions.assertEquals(Autocomplete.CC_EXP, c.getAutocomplete());
    }

    @Test
    void ccExpMonthAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_EXP_MONTH);
        Assertions.assertEquals(Autocomplete.CC_EXP_MONTH, c.getAutocomplete());
    }

    @Test
    void ccExpYearAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_EXP_YEAR);
        Assertions.assertEquals(Autocomplete.CC_EXP_YEAR, c.getAutocomplete());
    }

    @Test
    void ccCscAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_CSC);
        Assertions.assertEquals(Autocomplete.CC_CSC, c.getAutocomplete());
    }

    @Test
    void ccTypeAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_TYPE);
        Assertions.assertEquals(Autocomplete.CC_TYPE, c.getAutocomplete());
    }

    @Test
    void transactionCurrencyAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TRANSACTION_CURRENCY);
        Assertions.assertEquals(Autocomplete.TRANSACTION_CURRENCY,
                c.getAutocomplete());
    }

    @Test
    void transactionAmountAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TRANSACTION_AMOUNT);
        Assertions.assertEquals(Autocomplete.TRANSACTION_AMOUNT,
                c.getAutocomplete());
    }

    @Test
    void languageAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.LANGUAGE);
        Assertions.assertEquals(Autocomplete.LANGUAGE, c.getAutocomplete());
    }

    @Test
    void bdayAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.BDAY);
        Assertions.assertEquals(Autocomplete.BDAY, c.getAutocomplete());
    }

    @Test
    void bdayDayAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.BDAY_DAY);
        Assertions.assertEquals(Autocomplete.BDAY_DAY, c.getAutocomplete());
    }

    @Test
    void bdayMonthAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.BDAY_MONTH);
        Assertions.assertEquals(Autocomplete.BDAY_MONTH, c.getAutocomplete());
    }

    @Test
    void bdayYearAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.BDAY_YEAR);
        Assertions.assertEquals(Autocomplete.BDAY_YEAR, c.getAutocomplete());
    }

    @Test
    void sexAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.SEX);
        Assertions.assertEquals(Autocomplete.SEX, c.getAutocomplete());
    }

    @Test
    void telAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL);
        Assertions.assertEquals(Autocomplete.TEL, c.getAutocomplete());
    }

    @Test
    void telCountryCodeAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_COUNTRY_CODE);
        Assertions.assertEquals(Autocomplete.TEL_COUNTRY_CODE,
                c.getAutocomplete());
    }

    @Test
    void telNationalAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_NATIONAL);
        Assertions.assertEquals(Autocomplete.TEL_NATIONAL, c.getAutocomplete());
    }

    @Test
    void telAreaCodeAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_AREA_CODE);
        Assertions.assertEquals(Autocomplete.TEL_AREA_CODE,
                c.getAutocomplete());
    }

    @Test
    void telLocalAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_LOCAL);
        Assertions.assertEquals(Autocomplete.TEL_LOCAL, c.getAutocomplete());
    }

    @Test
    void telLocalPrefixAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_LOCAL_PREFIX);
        Assertions.assertEquals(Autocomplete.TEL_LOCAL_PREFIX,
                c.getAutocomplete());
    }

    @Test
    void telLocalSuffixAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_LOCAL_SUFFIX);
        Assertions.assertEquals(Autocomplete.TEL_LOCAL_SUFFIX,
                c.getAutocomplete());
    }

    @Test
    void telExtensionAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_EXTENSION);
        Assertions.assertEquals(Autocomplete.TEL_EXTENSION,
                c.getAutocomplete());
    }

    @Test
    void urlAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.URL);
        Assertions.assertEquals(Autocomplete.URL, c.getAutocomplete());
    }

    @Test
    void photoAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.PHOTO);
        Assertions.assertEquals(Autocomplete.PHOTO, c.getAutocomplete());
    }
}
