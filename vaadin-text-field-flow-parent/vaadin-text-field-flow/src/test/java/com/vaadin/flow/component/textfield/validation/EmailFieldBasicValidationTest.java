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

    protected EmailField createTestField() {
        return new EmailField();
    }
}
