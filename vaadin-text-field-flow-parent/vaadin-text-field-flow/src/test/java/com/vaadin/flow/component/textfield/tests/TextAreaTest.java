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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.dom.ThemeList;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link TextArea}.
 */
public class TextAreaTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        TextArea textArea = new TextArea();
        assertEquals("Value should be an empty string", "",
                textArea.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        textArea.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        TextArea textArea = new TextArea();
        assertEquals(textArea.getEmptyValue(),
                textArea.getElement().getProperty("value"));
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        TextArea textArea = new TextArea();

        assertClearButtonPropertyValueEquals(textArea, true);
        assertClearButtonPropertyValueEquals(textArea, false);
    }

    public void assertClearButtonPropertyValueEquals(TextArea textArea,
            Boolean value) {
        textArea.setClearButtonVisible(value);
        assertEquals(value, textArea.isClearButtonVisible());
        assertEquals(textArea.isClearButtonVisible(),
                textArea.getElement().getProperty("clearButtonVisible", value));
    }

    @Test
    public void autoselectPropertyValue() {
        TextArea textArea = new TextArea();

        assertAutoselectPropertyValueEquals(textArea, true);
        assertAutoselectPropertyValueEquals(textArea, false);
    }

    @Test
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo", TextArea.class);
    }

    @Test
    public void patternPropertyValue() {
        String testPattern = "TEST";
        TextArea textArea = new TextArea();

        textArea.setPattern(testPattern);
        assertEquals(testPattern, textArea.getPattern());
        assertEquals(testPattern,
                textArea.getElement().getProperty("pattern", ""));
    }

    public void assertAutoselectPropertyValueEquals(TextArea textArea,
            Boolean value) {
        textArea.setAutoselect(value);
        assertEquals(value, textArea.isAutoselect());
        assertEquals(textArea.isAutoselect(),
                textArea.getElement().getProperty("autoselect", value));
    }

    @Test
    public void addThemeVariant_themeAttributeContainsThemeVariant() {
        TextArea textArea = new TextArea();
        textArea.addThemeVariants(TextAreaVariant.LUMO_SMALL);

        ThemeList themeNames = textArea.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(TextAreaVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        TextArea textArea = new TextArea();
        textArea.addThemeVariants(TextAreaVariant.LUMO_SMALL);
        textArea.removeThemeVariants(TextAreaVariant.LUMO_SMALL);

        ThemeList themeNames = textArea.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TextAreaVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void implementsHasAllowedCharPattern() {
        assertTrue("TextArea should support char pattern",
                HasAllowedCharPattern.class
                        .isAssignableFrom(new TextArea().getClass()));
    }

    @Test
    public void implementsHasTooltip() {
        TextArea textArea = new TextArea();
        Assert.assertTrue(textArea instanceof HasTooltip);
    }

    @Test
    public void implementHasAriaLabel() {
        TextArea field = new TextArea();
        Assert.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        TextArea field = new TextArea();

        field.setAriaLabel("aria-label");
        Assert.assertTrue(field.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assert.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        TextArea field = new TextArea();

        field.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(field.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assert.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    public void implementsInputField() {
        TextArea field = new TextArea();
        Assert.assertTrue(field instanceof InputField);
    }
}
