/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.validation;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class TextFieldBasicValidationTest
        extends AbstractBasicValidationTest<TextField, String> {
    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue("AAA");
        testField.setValue("");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new TextField.TextFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue("AAA");
        testField.setValue("");
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void minLength_validate_emptyErrorMessageDisplayed() {
        testField.setMinLength(3);
        testField.setValue("AA");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void minLength_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMinLength(3);
        testField.setI18n(new TextField.TextFieldI18n()
                .setMinLengthErrorMessage("Value is too short"));
        testField.setValue("AA");
        Assert.assertEquals("Value is too short", testField.getErrorMessage());
    }

    @Test
    public void maxLength_validate_emptyErrorMessageDisplayed() {
        testField.setMaxLength(3);
        testField.setValue("AAAA");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void maxLength_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMaxLength(3);
        testField.setI18n(new TextField.TextFieldI18n()
                .setMaxLengthErrorMessage("Value is too long"));
        testField.setValue("AAAA");
        Assert.assertEquals("Value is too long", testField.getErrorMessage());
    }

    @Test
    public void pattern_validate_emptyErrorMessageDisplayed() {
        testField.setPattern("\\d+");
        testField.setValue("AAAA");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void pattern_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setPattern("\\d+");
        testField.setI18n(new TextField.TextFieldI18n()
                .setPatternErrorMessage("Value does not match the pattern"));
        testField.setValue("AAAA");
        Assert.assertEquals("Value does not match the pattern",
                testField.getErrorMessage());
    }

    @Override
    protected TextField createTestField() {
        return new TextField();
    }
}
