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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.tests.ThemeVariantTestHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link EmailField}.
 */
public class EmailFieldTest {

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
        ThemeVariantTestHelper.addThemeVariant_themeNamesContainsThemeVariant(
                new EmailField(), TextFieldVariant.LUMO_SMALL);
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        ThemeVariantTestHelper
                .addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant(
                        new EmailField(), TextFieldVariant.LUMO_SMALL);
    }

    @Test
    public void implementsHasAllowedCharPattern() {
        assertTrue("EmailField should support char pattern",
                HasAllowedCharPattern.class
                        .isAssignableFrom(new EmailField().getClass()));
    }
}
