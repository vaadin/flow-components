/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.HasAutocomplete;
import org.junit.Assert;
import org.junit.Test;

public class HasAutocompleteTest {

    @Tag("div")
    public static class HasAutocompleteComponent extends Component
            implements HasAutocomplete {

    }

    @Test
    public void defaultValue() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        Assert.assertEquals(null, c.getAutocomplete());
    }

    @Test
    public void emptyValue() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.getElement().setAttribute("autocomplete", "");
        Assert.assertEquals(Autocomplete.OFF, c.getAutocomplete());
    }

    @Test
    public void noAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.OFF);
        Assert.assertEquals(Autocomplete.OFF, c.getAutocomplete());
    }

    @Test
    public void onAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ON);
        Assert.assertEquals(Autocomplete.ON, c.getAutocomplete());
    }

    @Test
    public void nameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.NAME);
        Assert.assertEquals(Autocomplete.NAME, c.getAutocomplete());
    }

    @Test
    public void honorificPrefixAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.HONORIFIC_PREFIX);
        Assert.assertEquals(Autocomplete.HONORIFIC_PREFIX, c.getAutocomplete());
    }

    @Test
    public void givenNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.GIVEN_NAME);
        Assert.assertEquals(Autocomplete.GIVEN_NAME, c.getAutocomplete());
    }

    @Test
    public void additionalNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDITIONAL_NAME);
        Assert.assertEquals(Autocomplete.ADDITIONAL_NAME, c.getAutocomplete());
    }

    @Test
    public void familyNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.FAMILY_NAME);
        Assert.assertEquals(Autocomplete.FAMILY_NAME, c.getAutocomplete());
    }

    @Test
    public void honorificSuffixAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.HONORIFIC_SUFFIX);
        Assert.assertEquals(Autocomplete.HONORIFIC_SUFFIX, c.getAutocomplete());
    }

    @Test
    public void nicknameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.NICKNAME);
        Assert.assertEquals(Autocomplete.NICKNAME, c.getAutocomplete());
    }

    @Test
    public void emailAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.EMAIL);
        Assert.assertEquals(Autocomplete.EMAIL, c.getAutocomplete());
    }

    @Test
    public void usernameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.USERNAME);
        Assert.assertEquals(Autocomplete.USERNAME, c.getAutocomplete());
    }

    @Test
    public void newPasswordAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.NEW_PASSWORD);
        Assert.assertEquals(Autocomplete.NEW_PASSWORD, c.getAutocomplete());
    }

    @Test
    public void currentPasswordAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CURRENT_PASSWORD);
        Assert.assertEquals(Autocomplete.CURRENT_PASSWORD, c.getAutocomplete());
    }

    @Test
    public void organizationTitleAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ORGANIZATION_TITLE);
        Assert.assertEquals(Autocomplete.ORGANIZATION_TITLE,
                c.getAutocomplete());
    }

    @Test
    public void organizationAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ORGANIZATION);
        Assert.assertEquals(Autocomplete.ORGANIZATION, c.getAutocomplete());
    }

    @Test
    public void streetAddressAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.STREET_ADDRESS);
        Assert.assertEquals(Autocomplete.STREET_ADDRESS, c.getAutocomplete());
    }

    @Test
    public void addressLine1Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LINE1);
        Assert.assertEquals(Autocomplete.ADDRESS_LINE1, c.getAutocomplete());
    }

    @Test
    public void addressLine2Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LINE2);
        Assert.assertEquals(Autocomplete.ADDRESS_LINE2, c.getAutocomplete());
    }

    @Test
    public void addressLine3Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LINE3);
        Assert.assertEquals(Autocomplete.ADDRESS_LINE3, c.getAutocomplete());
    }

    @Test
    public void addressLevel1Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LEVEL1);
        Assert.assertEquals(Autocomplete.ADDRESS_LEVEL1, c.getAutocomplete());
    }

    @Test
    public void addressLevel2Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LEVEL2);
        Assert.assertEquals(Autocomplete.ADDRESS_LEVEL2, c.getAutocomplete());
    }

    @Test
    public void addressLevel3Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LEVEL3);
        Assert.assertEquals(Autocomplete.ADDRESS_LEVEL3, c.getAutocomplete());
    }

    @Test
    public void addressLevel4Autocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.ADDRESS_LEVEL4);
        Assert.assertEquals(Autocomplete.ADDRESS_LEVEL4, c.getAutocomplete());
    }

    @Test
    public void countryAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.COUNTRY);
        Assert.assertEquals(Autocomplete.COUNTRY, c.getAutocomplete());
    }

    @Test
    public void countryNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.COUNTRY_NAME);
        Assert.assertEquals(Autocomplete.COUNTRY_NAME, c.getAutocomplete());
    }

    @Test
    public void postalCodeAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.POSTAL_CODE);
        Assert.assertEquals(Autocomplete.POSTAL_CODE, c.getAutocomplete());
    }

    @Test
    public void ccNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_NAME);
        Assert.assertEquals(Autocomplete.CC_NAME, c.getAutocomplete());
    }

    @Test
    public void ccGivenNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_GIVEN_NAME);
        Assert.assertEquals(Autocomplete.CC_GIVEN_NAME, c.getAutocomplete());
    }

    @Test
    public void ccAdditionalNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_ADDITIONAL_NAME);
        Assert.assertEquals(Autocomplete.CC_ADDITIONAL_NAME,
                c.getAutocomplete());
    }

    @Test
    public void ccFamilyNameAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_FAMILY_NAME);
        Assert.assertEquals(Autocomplete.CC_FAMILY_NAME, c.getAutocomplete());
    }

    @Test
    public void ccNumberAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_NUMBER);
        Assert.assertEquals(Autocomplete.CC_NUMBER, c.getAutocomplete());
    }

    @Test
    public void ccExpAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_EXP);
        Assert.assertEquals(Autocomplete.CC_EXP, c.getAutocomplete());
    }

    @Test
    public void ccExpMonthAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_EXP_MONTH);
        Assert.assertEquals(Autocomplete.CC_EXP_MONTH, c.getAutocomplete());
    }

    @Test
    public void ccExpYearAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_EXP_YEAR);
        Assert.assertEquals(Autocomplete.CC_EXP_YEAR, c.getAutocomplete());
    }

    @Test
    public void ccCscAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_CSC);
        Assert.assertEquals(Autocomplete.CC_CSC, c.getAutocomplete());
    }

    @Test
    public void ccTypeAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.CC_TYPE);
        Assert.assertEquals(Autocomplete.CC_TYPE, c.getAutocomplete());
    }

    @Test
    public void transactionCurrencyAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TRANSACTION_CURRENCY);
        Assert.assertEquals(Autocomplete.TRANSACTION_CURRENCY,
                c.getAutocomplete());
    }

    @Test
    public void transactionAmountAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TRANSACTION_AMOUNT);
        Assert.assertEquals(Autocomplete.TRANSACTION_AMOUNT,
                c.getAutocomplete());
    }

    @Test
    public void languageAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.LANGUAGE);
        Assert.assertEquals(Autocomplete.LANGUAGE, c.getAutocomplete());
    }

    @Test
    public void bdayAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.BDAY);
        Assert.assertEquals(Autocomplete.BDAY, c.getAutocomplete());
    }

    @Test
    public void bdayDayAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.BDAY_DAY);
        Assert.assertEquals(Autocomplete.BDAY_DAY, c.getAutocomplete());
    }

    @Test
    public void bdayMonthAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.BDAY_MONTH);
        Assert.assertEquals(Autocomplete.BDAY_MONTH, c.getAutocomplete());
    }

    @Test
    public void bdayYearAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.BDAY_YEAR);
        Assert.assertEquals(Autocomplete.BDAY_YEAR, c.getAutocomplete());
    }

    @Test
    public void sexAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.SEX);
        Assert.assertEquals(Autocomplete.SEX, c.getAutocomplete());
    }

    @Test
    public void telAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL);
        Assert.assertEquals(Autocomplete.TEL, c.getAutocomplete());
    }

    @Test
    public void telCountryCodeAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_COUNTRY_CODE);
        Assert.assertEquals(Autocomplete.TEL_COUNTRY_CODE, c.getAutocomplete());
    }

    @Test
    public void telNationalAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_NATIONAL);
        Assert.assertEquals(Autocomplete.TEL_NATIONAL, c.getAutocomplete());
    }

    @Test
    public void telAreaCodeAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_AREA_CODE);
        Assert.assertEquals(Autocomplete.TEL_AREA_CODE, c.getAutocomplete());
    }

    @Test
    public void telLocalAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_LOCAL);
        Assert.assertEquals(Autocomplete.TEL_LOCAL, c.getAutocomplete());
    }

    @Test
    public void telLocalPrefixAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_LOCAL_PREFIX);
        Assert.assertEquals(Autocomplete.TEL_LOCAL_PREFIX, c.getAutocomplete());
    }

    @Test
    public void telLocalSuffixAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_LOCAL_SUFFIX);
        Assert.assertEquals(Autocomplete.TEL_LOCAL_SUFFIX, c.getAutocomplete());
    }

    @Test
    public void telExtensionAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.TEL_EXTENSION);
        Assert.assertEquals(Autocomplete.TEL_EXTENSION, c.getAutocomplete());
    }

    @Test
    public void urlAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.URL);
        Assert.assertEquals(Autocomplete.URL, c.getAutocomplete());
    }

    @Test
    public void photoAutocomplete() {
        HasAutocompleteComponent c = new HasAutocompleteComponent();
        c.setAutocomplete(Autocomplete.PHOTO);
        Assert.assertEquals(Autocomplete.PHOTO, c.getAutocomplete());
    }
}
