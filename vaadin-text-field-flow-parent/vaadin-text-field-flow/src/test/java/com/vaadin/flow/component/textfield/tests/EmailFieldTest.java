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

import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.dom.ThemeList;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link EmailField}.
 */
public class EmailFieldTest {
    private static final String[] VALID_EMAILS = { "email@example.com",
            "firstname.lastname@example.com", "email@subdomain.example.com",
            "firstname+lastname@example.com", "email@123.123.123.123",
            "1234567890@example.com", "email@example-one.com",
            "_______@example.com", "email@example.name", "email@example.museum",
            "email@example.co.jp", "firstname-lastname@example.com", };

    private static final String[] INVALID_EMAILS = { "plainaddress",
            "#@%^%#$@#$@#.com", "@example.com", "Joe Smith <email@example.com>",
            "email.example.com", "email@example@example.com",
            "あいうえお@example.com", "email@example.com (Joe Smith)",
            "email@example..com", "email@example", };

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        EmailField emailField = new EmailField();
        assertEquals("Value should be an empty string", "",
                emailField.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        emailField.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        EmailField emailField = new EmailField();
        assertEquals(emailField.getEmptyValue(),
                emailField.getElement().getProperty("value"));
    }

    @Test
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo@example.com", EmailField.class);
    }

    @Test
    public void addThemeVariant_themeAttributeContainsThemeVariant() {
        EmailField field = new EmailField();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        EmailField field = new EmailField();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.removeThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void implementsHasAllowedCharPattern() {
        assertTrue("EmailField should support char pattern",
                HasAllowedCharPattern.class
                        .isAssignableFrom(new EmailField().getClass()));
    }

    @Test
    public void implementsHasTooltip() {
        EmailField field = new EmailField();
        Assert.assertTrue(field instanceof HasTooltip);
    }

    @Test
    public void setInvalidEmail_fieldIsInvalid() {
        EmailField field = new EmailField();
        for (String email : INVALID_EMAILS) {
            field.setValue(email);
            Assert.assertTrue("Should be invalid when setting " + email,
                    field.isInvalid());
        }
    }

    @Test
    public void setValidEmail_fieldIsValid() {
        EmailField field = new EmailField();
        for (String email : VALID_EMAILS) {
            field.setValue(email);
            Assert.assertFalse("Should be valid when setting " + email,
                    field.isInvalid());
        }
    }
}
