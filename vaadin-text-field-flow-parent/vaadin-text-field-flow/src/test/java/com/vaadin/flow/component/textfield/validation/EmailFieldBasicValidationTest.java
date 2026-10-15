/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.validation;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class EmailFieldBasicValidationTest
        extends AbstractBasicValidationTest<EmailField, String> {
    private static String[] VALID_EMAILS = { "email@example.com",
            "firstname.lastname@example.com", "email@subdomain.example.com",
            "firstname+lastname@example.com", "email@123.123.123.123",
            "1234567890@example.com", "email@example-one.com",
            "_______@example.com", "email@example.name", "email@example.museum",
            "email@example.co.jp", "firstname-lastname@example.com", };

    private static String[] INVALID_EMAILS = { "plainaddress",
            "#@%^%#$@#$@#.com", "@example.com", "Joe Smith <email@example.com>",
            "email.example.com", "email@example@example.com",
            "あいうえお@example.com", "email@example.com (Joe Smith)",
            "email@example..com", "email@example", };

    @Test
    public void setInvalidEmail_fieldIsInvalid() {
        for (String email : INVALID_EMAILS) {
            testField.setValue(email);
            Assert.assertTrue("Should be invalid when setting " + email,
                    testField.isInvalid());
        }
    }

    @Test
    public void setValidEmail_fieldIsValid() {
        for (String email : VALID_EMAILS) {
            testField.setValue(email);
            Assert.assertFalse("Should be valid when setting " + email,
                    testField.isInvalid());
        }
    }

    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue("john@vaadin.com");
        testField.setValue("");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new EmailField.EmailFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue("john@vaadin.com");
        testField.setValue("");
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void minLength_validate_emptyErrorMessageDisplayed() {
        testField.setMinLength(13);
        testField.setValue("a@vaadin.com");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void minLength_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMinLength(13);
        testField.setI18n(new EmailField.EmailFieldI18n()
                .setMinLengthErrorMessage("Value is too short"));
        testField.setValue("a@vaadin.com");
        Assert.assertEquals("Value is too short", testField.getErrorMessage());
    }

    @Test
    public void maxLength_validate_emptyErrorMessageDisplayed() {
        testField.setMaxLength(13);
        testField.setValue("aaa@vaadin.com");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void maxLength_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setMaxLength(13);
        testField.setI18n(new EmailField.EmailFieldI18n()
                .setMaxLengthErrorMessage("Value is too long"));
        testField.setValue("aaa@vaadin.com");
        Assert.assertEquals("Value is too long", testField.getErrorMessage());
    }

    @Test
    public void pattern_validate_emptyErrorMessageDisplayed() {
        testField.setValue("foobar");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void pattern_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setI18n(new EmailField.EmailFieldI18n()
                .setPatternErrorMessage("Value has incorrect format"));
        testField.setValue("foobar");
        Assert.assertEquals("Value has incorrect format",
                testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new EmailField.EmailFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue("john@vaadin.com");
        testField.setValue("");
        Assert.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new EmailField.EmailFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue("john@vaadin.com");
        testField.setValue("");
        testField.setErrorMessage("");
        testField.setValue("john@vaadin.com");
        testField.setValue("");
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Override
    protected EmailField createTestField() {
        return new EmailField();
    }
}
