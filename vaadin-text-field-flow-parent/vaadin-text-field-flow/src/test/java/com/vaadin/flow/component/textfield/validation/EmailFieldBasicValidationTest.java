/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class EmailFieldBasicValidationTest
        extends AbstractBasicValidationTest<EmailField> {
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
