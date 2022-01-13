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

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.textfield.TextField;

/**
 * Tests for the {@link TextField}.
 */
public class TextFieldTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        TextField textField = new TextField();
        assertEquals("Value should be an empty string", "",
                textField.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        textField.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        TextField textField = new TextField();
        assertEquals(textField.getEmptyValue(),
                textField.getElement().getProperty("value"));
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        TextField textField = new TextField();

        assertClearButtonPropertyValueEquals(textField, true);
        assertClearButtonPropertyValueEquals(textField, false);
    }

    public void assertClearButtonPropertyValueEquals(TextField textField,
            Boolean value) {
        textField.setClearButtonVisible(value);
        assertEquals(value, textField.isClearButtonVisible());
        assertEquals(textField.isClearButtonVisible(), textField.getElement()
                .getProperty("clearButtonVisible", value));
    }

    @Test
    public void autoselectPropertyValue() {
        TextField textField = new TextField();

        assertAutoselectPropertyValueEquals(textField, true);
        assertAutoselectPropertyValueEquals(textField, false);
    }

    @Test
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo", TextField.class);
    }

    public void assertAutoselectPropertyValueEquals(TextField textField,
            Boolean value) {
        textField.setAutoselect(value);
        assertEquals(value, textField.isAutoselect());
        assertEquals(textField.isAutoselect(),
                textField.getElement().getProperty("autoselect", value));
    }

}
